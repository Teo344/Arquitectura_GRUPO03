using System;
using System.Collections.Generic;
using System.Configuration;
using System.Globalization;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Channels;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Única capa que conoce SOAP: crea los canales de AutenticacionWS, CuentaWS y TransaccionWS,
    /// guarda la sesión activa y convierte las respuestas y los SOAP Fault en objetos del modelo.
    /// </summary>
    public class ClienteWSEureka
    {
        private const string PropiedadUrl = "servidor.url";
        private const string EstadoOk = "OK";
        private const string SesionInvalida = "AUTH-003";
        private static readonly TimeSpan TiempoEspera = TimeSpan.FromSeconds(10);

        private readonly string baseUrl;
        private readonly ChannelFactory<ContratoWSEureka.Autenticacion> fabricaAutenticacion;
        private readonly ChannelFactory<ContratoWSEureka.Cuenta> fabricaCuenta;
        private readonly ChannelFactory<ContratoWSEureka.Transaccion> fabricaTransaccion;

        public SesionModelo Sesion { get; private set; }

        public ClienteWSEureka()
        {
            baseUrl = ObtenerBaseConfigurada();
            fabricaAutenticacion = CrearFabrica<ContratoWSEureka.Autenticacion>("AutenticacionWS.svc");
            fabricaCuenta = CrearFabrica<ContratoWSEureka.Cuenta>("CuentaWS.svc");
            fabricaTransaccion = CrearFabrica<ContratoWSEureka.Transaccion>("TransaccionWS.svc");
        }

        // servidor.url de App.config (RNF-010)
        private static string ObtenerBaseConfigurada()
        {
            string url = ConfigurationManager.AppSettings[PropiedadUrl];
            if (string.IsNullOrWhiteSpace(url))
                throw new ArgumentException("La URL del servidor (" + PropiedadUrl + ") es obligatoria.");
            string normalizada = url.Trim().TrimEnd('/');
            Uri direccion;
            if (!Uri.TryCreate(normalizada, UriKind.Absolute, out direccion) ||
                (direccion.Scheme != Uri.UriSchemeHttp && direccion.Scheme != Uri.UriSchemeHttps))
                throw new ArgumentException("La URL del servidor debe usar HTTP o HTTPS.");
            return normalizada;
        }

        // Las fábricas se crean una vez y se reutilizan; cada operación usa un canal propio.
        private ChannelFactory<TContrato> CrearFabrica<TContrato>(string servicio)
        {
            var binding = new BasicHttpBinding(baseUrl.StartsWith(Uri.UriSchemeHttps + ":", StringComparison.OrdinalIgnoreCase)
                ? BasicHttpSecurityMode.Transport : BasicHttpSecurityMode.None)
            {
                MaxReceivedMessageSize = 1024 * 1024,
                OpenTimeout = TiempoEspera,
                CloseTimeout = TiempoEspera,
                SendTimeout = TiempoEspera,
                ReceiveTimeout = TiempoEspera
            };
            return new ChannelFactory<TContrato>(binding, new EndpointAddress(baseUrl + "/" + servicio));
        }

        public string EndpointActivo => baseUrl;

        // ---------------------------------------------------------------- Autenticación

        public SesionModelo IniciarSesion(string usuario, string clave)
        {
            Sesion = null;
            ContratoWSEureka.Respuesta respuesta = Invocar(fabricaAutenticacion, null,
                canal => canal.IniciarSesion(usuario, clave, Environment.MachineName));
            if (string.IsNullOrWhiteSpace(respuesta.Token))
                throw new ErrorServicioModelo(null, "El servidor no devolvio el token de sesion.");

            DateTimeOffset expiraEn;
            if (!DateTimeOffset.TryParse(respuesta.ExpiraEn, CultureInfo.InvariantCulture,
                DateTimeStyles.RoundtripKind, out expiraEn))
                throw new ErrorServicioModelo(null, "El servidor devolvio una expiracion de sesion invalida.");

            Sesion = new SesionModelo(respuesta.Token, expiraEn, respuesta.CodigoEmpleado,
                respuesta.NombreEmpleado, respuesta.CodigoSucursal, respuesta.Sucursal);
            return Sesion;
        }

        /// <summary>
        /// Invalida el token en el servidor (RQF-002). La sesión local se elimina siempre; si el
        /// token ya había caducado (AUTH-003) no se considera error.
        /// </summary>
        public void CerrarSesion()
        {
            if (Sesion == null) return;
            string token = Sesion.Token;
            Sesion = null;
            try
            {
                Invocar(fabricaAutenticacion, token, canal => canal.CerrarSesion());
            }
            catch (ErrorServicioModelo ex)
            {
                if (!ex.RequiereNuevoLogin) throw;
            }
        }

        // ---------------------------------------------------------------- Consultas

        public CuentaModelo ConsultarCuenta(string cuenta)
        {
            string token = TokenActivo();
            ContratoWSEureka.DatosCuenta datos = Invocar(fabricaCuenta, token, canal => canal.ConsultarCuenta(cuenta)).Cuenta;
            if (datos == null)
                throw new ErrorServicioModelo(null, "El servidor no devolvio los datos de la cuenta.");
            return new CuentaModelo(datos.Codigo, datos.Titular, datos.Moneda, datos.Saldo, datos.Estado,
                datos.Sucursal, datos.FechaCreacion);
        }

        /// <summary>Una cuenta sin movimientos devuelve una lista vacía.</summary>
        public List<MovimientoModelo> ConsultarMovimientos(string cuenta)
        {
            string token = TokenActivo();
            List<ContratoWSEureka.DatosMovimiento> datos
                = Invocar(fabricaCuenta, token, canal => canal.ConsultarMovimientos(cuenta)).Movimientos;
            if (datos == null) return new List<MovimientoModelo>();
            return datos.Select(m => new MovimientoModelo(m.Numero, m.Fecha, m.Tipo, m.Accion, m.Importe,
                m.CuentaReferencia)).ToList();
        }

        // ---------------------------------------------------------------- Transacciones

        public ResultadoTransaccionModelo Depositar(string cuenta, decimal importe)
        {
            string token = TokenActivo();
            return AResultado(Invocar(fabricaTransaccion, token, canal => canal.Depositar(cuenta, importe)));
        }

        public ResultadoTransaccionModelo Retirar(string cuenta, decimal importe)
        {
            string token = TokenActivo();
            return AResultado(Invocar(fabricaTransaccion, token, canal => canal.Retirar(cuenta, importe)));
        }

        public ResultadoTransaccionModelo Transferir(string cuentaOrigen, string cuentaDestino, decimal importe)
        {
            string token = TokenActivo();
            return AResultado(Invocar(fabricaTransaccion, token,
                canal => canal.Transferir(cuentaOrigen, cuentaDestino, importe)));
        }

        private static ResultadoTransaccionModelo AResultado(ContratoWSEureka.Respuesta respuesta)
        {
            return new ResultadoTransaccionModelo(respuesta.Mensaje, respuesta.NuevoSaldo, respuesta.NumeroMovimiento,
                respuesta.NumeroMovimientoDestino);
        }

        // ---------------------------------------------------------------- Soporte

        private string TokenActivo()
        {
            if (Sesion == null)
                throw new ErrorServicioModelo(SesionInvalida, "Debe iniciar sesion.");
            return Sesion.Token;
        }

        /// <summary>
        /// Ejecuta la llamada SOAP en un canal nuevo, con el token en la cabecera SOAP cuando la
        /// operación es protegida, y traduce sus errores: un SOAP Fault trae el detalle
        /// ErrorServicio (codigo, mensaje); cualquier otra falla es de comunicación con el servidor.
        /// </summary>
        private ContratoWSEureka.Respuesta Invocar<TContrato>(ChannelFactory<TContrato> fabrica, string token,
            Func<TContrato, ContratoWSEureka.Respuesta> llamada) where TContrato : class
        {
            TContrato canal = null;
            bool completado = false;
            ContratoWSEureka.Respuesta respuesta;
            try
            {
                canal = fabrica.CreateChannel();
                using (new OperationContextScope((IContextChannel)canal))
                {
                    if (token != null)
                        OperationContext.Current.OutgoingMessageHeaders.Add(
                            MessageHeader.CreateHeader("token", ContratoWSEureka.EspacioNombres, token));
                    respuesta = llamada(canal);
                }
                completado = true;
            }
            catch (FaultException ex)
            {
                ErrorServicioModelo error = LeerFault(ex);
                if (error.RequiereNuevoLogin) Sesion = null;
                throw error;
            }
            catch (TimeoutException)
            {
                throw new ErrorServicioModelo(null, "El servidor " + baseUrl + " tardo demasiado en responder.");
            }
            catch (Exception ex) when (ex is CommunicationException || ex is SerializationException)
            {
                throw new ErrorServicioModelo(null, "No se pudo comunicar con el servidor " + baseUrl
                    + ". Verifique que IIS Express este activo.");
            }
            finally
            {
                Cerrar(canal as ICommunicationObject, completado);
            }

            if (respuesta == null || !EstadoOk.Equals(respuesta.Estado))
                throw new ErrorServicioModelo(null, "El servidor devolvio una respuesta inesperada.");
            return respuesta;
        }

        // El inspector de token del servidor puede enviar el Fault sin la acción del FaultContract
        private static ErrorServicioModelo LeerFault(FaultException ex)
        {
            ContratoWSEureka.ErrorServicio detalle = (ex as FaultException<ContratoWSEureka.ErrorServicio>)?.Detail;
            if (detalle == null)
            {
                MessageFault fallo = ex.CreateMessageFault();
                if (fallo.HasDetail)
                {
                    try { detalle = fallo.GetDetail<ContratoWSEureka.ErrorServicio>(); }
                    catch (SerializationException) { }
                }
            }
            string mensaje = ex.Reason.ToString();
            return detalle == null
                ? new ErrorServicioModelo(null, mensaje)
                : new ErrorServicioModelo(detalle.Codigo, detalle.Mensaje ?? mensaje);
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
