using SistemaConversorUI.Shared.Interfaces;
using SistemaConversorUI.Shared.Modelos;
using System;
using System.Collections.Generic;
using System.Text;
using System.Xml.Linq;

namespace SistemaConversorUI.Shared.Adaptadores
{
    public sealed class AdaptadorAutenticacionSoap : IAutenticacionSoap
    {
        private static readonly XNamespace Soap =
            "http://schemas.xmlsoap.org/soap/envelope/";

        private static readonly XNamespace Servicio =
            "http://wservicios.controladores.monster.edu.ec/";

        private readonly HttpClient _http;

        public AdaptadorAutenticacionSoap(HttpClient http)
        {
            _http = http;
        }

        public async Task<SesionCliente> IniciarSesionAsync(
            string usuario,
            string clave)
        {
            var documento = await EnviarAsync(
                "login",
                ("usuario", usuario),
                ("clave", clave));

            var token = documento.Descendants()
                .FirstOrDefault(x => x.Name.LocalName == "token")?.Value;

            if (string.IsNullOrWhiteSpace(token))
            {
                var mensaje = documento.Descendants()
                    .FirstOrDefault(x => x.Name.LocalName == "mensaje")?.Value;

                throw new InvalidOperationException(
                    mensaje ?? "El servicio no devolvió un token de sesión.");
            }

            var expiraEn = documento.Descendants()
                .FirstOrDefault(x => x.Name.LocalName == "expiraEn")?.Value;

            return new SesionCliente(usuario, token, expiraEn);
        }

        public async Task CambiarClaveAsync(
            string usuario,
            string claveActual,
            string claveNueva)
        {
            var documento = await EnviarAsync(
                "cambiarClave",
                ("usuario", usuario),
                ("claveActual", claveActual),
                ("claveNueva", claveNueva));

            var estado = documento.Descendants()
                .FirstOrDefault(x => x.Name.LocalName == "estado")?.Value;

            if (string.Equals(estado, "ERROR", StringComparison.OrdinalIgnoreCase))
            {
                var mensaje = documento.Descendants()
                    .FirstOrDefault(x => x.Name.LocalName == "mensaje")?.Value;

                throw new InvalidOperationException(
                    mensaje ?? "El servicio rechazó el cambio de clave.");
            }
        }

        private async Task<XDocument> EnviarAsync(
            string operacion,
            params (string Nombre, string Valor)[] parametros)
        {
            var cuerpoOperacion = new XElement(
                Servicio + operacion,
                parametros.Select(p => new XElement(Servicio + p.Nombre, p.Valor)));

            var sobre = new XDocument(
                new XElement(Soap + "Envelope",
                    new XAttribute(XNamespace.Xmlns + "soap", Soap),
                    new XAttribute(XNamespace.Xmlns + "svc", Servicio),
                    new XElement(Soap + "Header"),
                    new XElement(Soap + "Body", cuerpoOperacion)));

            using var solicitud = new HttpRequestMessage(
                HttpMethod.Post,
                $"http://localhost:8095/AutenticacionWS.svc");

            solicitud.Headers.Add(
                "SOAPAction",
                $"\"{Servicio}AutenticacionWS/{operacion}\"");

            solicitud.Content = new StringContent(
                sobre.ToString(SaveOptions.DisableFormatting),
                System.Text.Encoding.UTF8,
                "text/xml");

            using var respuesta = await _http.SendAsync(solicitud);
            var contenido = await respuesta.Content.ReadAsStringAsync();

            XDocument resultado;
            try
            {
                resultado = XDocument.Parse(contenido);
            }
            catch (System.Xml.XmlException)
            {
                throw new HttpRequestException(
                    $"El servidor devolvió una respuesta no XML (HTTP {(int)respuesta.StatusCode}).");
            }

            if (!respuesta.IsSuccessStatusCode)
            {
                var mensajeError = resultado.Descendants()
                    .FirstOrDefault(x =>
                        x.Name.LocalName is "faultstring" or "mensaje")?.Value;

                throw new HttpRequestException(
                    mensajeError ?? $"Error HTTP {(int)respuesta.StatusCode} del servicio SOAP.");
            }

            return resultado;
        }
    }
}
