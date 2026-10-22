using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    public class ClienteSOAPConversor
    {
        private const string ServicioAutenticacion = "AutenticacionWS.svc";
        private const string ServicioConversion = "ConversionUnidadesWS.svc";
        private static readonly TimeSpan TiempoEspera = TimeSpan.FromSeconds(10);

        private readonly Uri baseUri;
        private readonly ChannelFactory<IAutenticacionWS> fabricaAutenticacion;
        private readonly ChannelFactory<IConversionUnidadesWS> fabricaConversion;
        private string token;

        public ClienteSOAPConversor() : this(ConfigurationManager.AppSettings["ws.base.url"])
        {
        }

        public ClienteSOAPConversor(string baseUrl)
        {
            Uri direccion;
            if (string.IsNullOrWhiteSpace(baseUrl) ||
                !Uri.TryCreate(baseUrl.Trim().TrimEnd('/') + "/", UriKind.Absolute, out direccion) ||
                (direccion.Scheme != Uri.UriSchemeHttp && direccion.Scheme != Uri.UriSchemeHttps))
                throw new ArgumentException("Configure una direccion HTTP valida en ws.base.url de App.config.");
            baseUri = direccion;

            // Las fábricas se comparten; cada operación usa un canal independiente.
            fabricaAutenticacion = CrearFabrica<IAutenticacionWS>(ServicioAutenticacion);
            fabricaConversion = CrearFabrica<IConversionUnidadesWS>(ServicioConversion);
            fabricaConversion.Endpoint.EndpointBehaviors.Add(new ManejadorTokenSOAP(() => token));
        }

        private ChannelFactory<TContrato> CrearFabrica<TContrato>(string servicio)
        {
            var binding = new BasicHttpBinding(baseUri.Scheme == Uri.UriSchemeHttps
                ? BasicHttpSecurityMode.Transport : BasicHttpSecurityMode.None)
            {
                MaxReceivedMessageSize = 1024 * 1024,
                OpenTimeout = TiempoEspera,
                CloseTimeout = TiempoEspera,
                SendTimeout = TiempoEspera,
                ReceiveTimeout = TiempoEspera
            };
            return new ChannelFactory<TContrato>(binding, new EndpointAddress(new Uri(baseUri, servicio)));
        }

        public string EndpointActivo => baseUri.AbsoluteUri;

        public SesionModelo Login(string usuario, string clave)
        {
            CerrarSesion();
            RespuestaDto respuesta = Invocar(fabricaAutenticacion, false, canal => canal.Login(usuario, clave));
            if (string.IsNullOrWhiteSpace(respuesta.Token))
                throw new ErrorSOAPModelo("El servidor no devolvió el token de sesión.");
            token = respuesta.Token;
            return new SesionModelo(usuario, respuesta.Token, respuesta.ExpiraEn);
        }

        public void CerrarSesion()
        {
            token = null;
        }

        public List<CategoriaModelo> ConsultarCatalogo()
        {
            RespuestaDto respuesta = Invocar(fabricaConversion, true, canal => canal.ConsultarCatalogo());
            return (respuesta.Categorias ?? new List<CategoriaDto>())
                .Select(c => new CategoriaModelo(c.Codigo, c.Nombre, c.Convertible))
                .ToList();
        }

        public List<UnidadModelo> ConsultarUnidades(string categoria)
        {
            RespuestaDto respuesta = Invocar(fabricaConversion, true, canal => canal.ConsultarUnidades(categoria));
            return (respuesta.Unidades ?? new List<UnidadDto>())
                .Select(u => new UnidadModelo(u.Codigo, u.Nombre, u.Simbolo))
                .ToList();
        }

        public double ConvertirLongitud(double valor, string origen, string destino)
            => Convertir(canal => canal.ConvertirLongitud(Solicitud(valor, origen, destino)));

        public double ConvertirMasa(double valor, string origen, string destino)
            => Convertir(canal => canal.ConvertirMasa(Solicitud(valor, origen, destino)));

        public double ConvertirTemperatura(double valor, string origen, string destino)
            => Convertir(canal => canal.ConvertirTemperatura(Solicitud(valor, origen, destino)));

        private static SolicitudConversionDto Solicitud(double valor, string origen, string destino)
            => new SolicitudConversionDto { Valor = valor, UnidadOrigen = origen, UnidadDestino = destino };

        private double Convertir(Func<IConversionUnidadesWS, RespuestaDto> operacion)
        {
            RespuestaDto respuesta = Invocar(fabricaConversion, true, operacion);
            if (!respuesta.Resultado.HasValue)
                throw new ErrorSOAPModelo("El servidor no devolvió un resultado.");
            return respuesta.Resultado.Value;
        }

        private RespuestaDto Invocar<TContrato>(ChannelFactory<TContrato> fabrica, bool requiereToken,
            Func<TContrato, RespuestaDto> operacion) where TContrato : class
        {
            if (requiereToken && string.IsNullOrEmpty(token))
                throw new ErrorSOAPModelo("Debe iniciar sesión.");

            TContrato canal = null;
            bool completado = false;
            try
            {
                canal = fabrica.CreateChannel();
                RespuestaDto respuesta = operacion(canal);
                completado = true;
                if (respuesta == null)
                    throw new ErrorSOAPModelo("El servidor devolvió una respuesta vacía.");
                if (!"OK".Equals(respuesta.Estado, StringComparison.OrdinalIgnoreCase))
                    throw new ErrorSOAPModelo(respuesta.Codigo,
                        respuesta.Mensaje ?? "El servidor no pudo completar la operación.");
                return respuesta;
            }
            catch (FaultException<ErrorServicio> ex)
            {
                throw ErrorDelServidor(ex.Detail, ex);
            }
            catch (FaultException ex)
            {
                // Los fallos emitidos por el inspector del servidor pueden llegar sin la acción del FaultContract.
                ErrorServicio detalle = null;
                var fallo = ex.CreateMessageFault();
                if (fallo.HasDetail)
                {
                    try { detalle = fallo.GetDetail<ErrorServicio>(); }
                    catch (SerializationException) { }
                }
                throw ErrorDelServidor(detalle, ex);
            }
            catch (TimeoutException)
            {
                throw new ErrorSOAPModelo("El servidor tardó demasiado en responder. Intente nuevamente.");
            }
            catch (CommunicationException)
            {
                throw new ErrorSOAPModelo("No se pudo conectar con el servidor. Verifique que esté iniciado.");
            }
            catch (SerializationException)
            {
                throw new ErrorSOAPModelo("La respuesta SOAP no coincide con el contrato esperado.");
            }
            finally
            {
                Cerrar(canal as ICommunicationObject, completado);
            }
        }

        private ErrorSOAPModelo ErrorDelServidor(ErrorServicio detalle, FaultException ex)
        {
            if (detalle == null) return new ErrorSOAPModelo(ex.Reason.ToString());
            var error = new ErrorSOAPModelo(detalle.Codigo, detalle.Mensaje ?? ex.Reason.ToString());
            if (error.RequiereNuevoLogin) CerrarSesion();
            return error;
        }

        private static void Cerrar(ICommunicationObject recurso, bool completado)
        {
            if (recurso == null) return;
            if (!completado || recurso.State == CommunicationState.Faulted)
            {
                recurso.Abort();
                return;
            }
            try { recurso.Close(); }
            catch (CommunicationException) { recurso.Abort(); }
            catch (TimeoutException) { recurso.Abort(); }
        }
    }
}
