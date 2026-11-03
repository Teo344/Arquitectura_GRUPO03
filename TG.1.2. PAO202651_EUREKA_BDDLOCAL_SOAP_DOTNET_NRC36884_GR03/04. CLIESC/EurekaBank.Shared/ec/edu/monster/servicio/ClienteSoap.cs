using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using System.Xml;
using System.Xml.Linq;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Envía sobres SOAP 1.1 al servidor WS_EUREKA_DOTNET_SOAP (WCF) y lee sus respuestas. El XML
    /// se arma a mano, como en el cliente móvil de TG.1.1, para que la misma clase funcione en
    /// escritorio, Android y navegador. Es la única clase que conoce el protocolo; los servicios
    /// solo arman parámetros.
    /// </summary>
    public sealed class ClienteSoap
    {
        public static readonly XNamespace EspacioNombres = "http://wservicios.controladores.monster.edu.ec/";
        private static readonly XNamespace Sobre = "http://schemas.xmlsoap.org/soap/envelope/";

        private const string EstadoOk = "OK";

        private readonly HttpClient http;
        private readonly string urlBase;

        public ClienteSoap(HttpClient http, OpcionesCliente opciones)
        {
            this.http = http;
            urlBase = opciones.UrlServidor.TrimEnd('/');
        }

        /// <summary>
        /// Invoca una operación y devuelve el elemento return de una respuesta correcta (estado OK).
        /// </summary>
        /// <param name="servicio">AutenticacionWS, CuentaWS o TransaccionWS</param>
        /// <param name="operacion">nombre de la operación (iniciarSesion, depositar, ...)</param>
        /// <param name="token">token de sesión para la cabecera SOAP, o null si no aplica</param>
        /// <param name="parametros">parámetros de la operación, en orden</param>
        public async Task<XElement> InvocarAsync(string servicio, string operacion, string? token,
            params (string Nombre, string? Valor)[] parametros)
        {
            XDocument documento;
            int codigoHttp;
            try
            {
                using HttpRequestMessage solicitud = new(HttpMethod.Post, urlBase + "/" + servicio + ".svc");
                // WCF elige la operación por la SOAPAction: espacio de nombres + contrato + operación
                solicitud.Headers.Add("SOAPAction", $"\"{EspacioNombres}{servicio}/{operacion}\"");
                solicitud.Content = new StringContent(ConstruirSobre(operacion, token, parametros),
                    Encoding.UTF8, "text/xml");

                using HttpResponseMessage respuesta = await http.SendAsync(solicitud);
                codigoHttp = (int)respuesta.StatusCode;
                string contenido = await respuesta.Content.ReadAsStringAsync();
                documento = LeerXml(contenido);
            }
            catch (Exception ex) when (ex is HttpRequestException || ex is TaskCanceledException)
            {
                throw new ErrorServicioModelo(
                    $"No se pudo conectar con el servidor {urlBase}. Verifica que IIS Express esté iniciado.", ex);
            }
            catch (XmlException ex)
            {
                throw new ErrorServicioModelo("No se pudo leer la respuesta del servidor.", ex);
            }

            XElement? fault = PrimerElemento(documento.Root, "Fault");
            if (fault != null)
            {
                throw DesdeFault(fault);
            }

            XElement? retorno = PrimerElemento(documento.Root, "return");
            if (retorno == null || Texto(retorno, "estado") != EstadoOk)
            {
                throw new ErrorServicioModelo(null, codigoHttp >= 400
                    ? $"El servidor respondió con el código HTTP {codigoHttp}."
                    : "El servidor devolvió una respuesta inesperada.");
            }
            return retorno;
        }

        // El contrato WCF califica la operación y sus parámetros con el espacio de nombres del servicio
        private static string ConstruirSobre(string operacion, string? token, (string Nombre, string? Valor)[] parametros)
        {
            XElement sobre = new(Sobre + "Envelope",
                new XAttribute(XNamespace.Xmlns + "soapenv", Sobre),
                new XAttribute(XNamespace.Xmlns + "ws", EspacioNombres));
            if (token != null)
            {
                sobre.Add(new XElement(Sobre + "Header", new XElement(EspacioNombres + "token", token)));
            }
            sobre.Add(new XElement(Sobre + "Body", new XElement(EspacioNombres + operacion,
                parametros.Select(p => new XElement(EspacioNombres + p.Nombre, p.Valor ?? "")))));
            return sobre.ToString(SaveOptions.DisableFormatting);
        }

        // Sin DTD ni entidades externas: evita ataques XXE con respuestas manipuladas
        private static XDocument LeerXml(string contenido)
        {
            XmlReaderSettings ajustes = new() { DtdProcessing = DtdProcessing.Prohibit, XmlResolver = null };
            using XmlReader lector = XmlReader.Create(new StringReader(contenido), ajustes);
            return XDocument.Load(lector);
        }

        /// <summary>El servidor envía el detalle del error como ErrorServicio(codigo, mensaje).</summary>
        private static ErrorServicioModelo DesdeFault(XElement fault)
        {
            XElement? detalle = PrimerElemento(fault, "ErrorServicio");
            string? codigo = detalle == null ? null : Texto(detalle, "codigo");
            string? mensaje = detalle == null ? null : Texto(detalle, "mensaje");
            if (string.IsNullOrWhiteSpace(mensaje))
            {
                mensaje = Texto(fault, "faultstring");
            }
            return new ErrorServicioModelo(codigo, string.IsNullOrWhiteSpace(mensaje)
                ? "El servidor rechazó la solicitud." : mensaje);
        }

        /// <summary>Importe en el formato que espera el servidor: punto decimal, sin separador de miles.</summary>
        public static string Importe(decimal importe)
        {
            return importe.ToString(CultureInfo.InvariantCulture);
        }

        /// <summary>Texto del hijo directo con ese nombre, o null si no existe.</summary>
        public static string? Texto(XElement? padre, string nombre)
        {
            return Hijo(padre, nombre)?.Value;
        }

        public static decimal? Decimal(XElement? padre, string nombre)
        {
            string? valor = Texto(padre, nombre);
            if (string.IsNullOrWhiteSpace(valor))
            {
                return null;
            }
            if (decimal.TryParse(valor.Trim(), NumberStyles.Number, CultureInfo.InvariantCulture, out decimal numero))
            {
                return numero;
            }
            throw new ErrorServicioModelo(null, $"El servidor devolvió un valor no numérico en {nombre}.");
        }

        public static int? Entero(XElement? padre, string nombre)
        {
            string? valor = Texto(padre, nombre);
            if (string.IsNullOrWhiteSpace(valor))
            {
                return null;
            }
            if (int.TryParse(valor.Trim(), NumberStyles.Integer, CultureInfo.InvariantCulture, out int numero))
            {
                return numero;
            }
            throw new ErrorServicioModelo(null, $"El servidor devolvió un valor no numérico en {nombre}.");
        }

        /// <summary>Hijo directo con ese nombre local (sin importar el namespace).</summary>
        public static XElement? Hijo(XElement? padre, string nombre)
        {
            return Hijos(padre, nombre).FirstOrDefault();
        }

        /// <summary>Hijos directos con ese nombre local (sin importar el namespace).</summary>
        public static IEnumerable<XElement> Hijos(XElement? padre, string nombre)
        {
            return padre == null ? Enumerable.Empty<XElement>() : padre.Elements().Where(e => e.Name.LocalName == nombre);
        }

        private static XElement? PrimerElemento(XElement? raiz, string nombre)
        {
            return raiz?.Descendants().FirstOrDefault(e => e.Name.LocalName == nombre);
        }
    }
}
