using System.Globalization;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text.Json;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Única capa que conoce HTTP, JSON y las rutas de la API REST de EurekaBank (/api). Guarda la
    /// sesión activa, envía el token en la cabecera Authorization: Bearer y convierte las
    /// respuestas y los errores JSON en objetos del modelo.
    /// </summary>
    public class ClienteRESTEureka
    {
        private const string ArchivoConfiguracion = "appsettings.json";
        private const string PropiedadUrl = "servidor.url";
        private const string EstadoOk = "OK";
        private const string SesionInvalida = "AUTH-003";
        private const int TamanoMaximoRespuesta = 1024 * 1024;

        // camelCase al enviar y sin distinguir mayúsculas al leer, como el JSON del servidor
        private static readonly JsonSerializerOptions OpcionesJson = new JsonSerializerOptions(JsonSerializerDefaults.Web);

        // Un solo HttpClient reutiliza las conexiones; no sigue redirecciones para no enviar el token a otro host
        private static readonly HttpClient Http = new HttpClient(new SocketsHttpHandler
        {
            ConnectTimeout = TimeSpan.FromSeconds(8),
            PooledConnectionLifetime = TimeSpan.FromMinutes(2),
            AllowAutoRedirect = false
        })
        {
            Timeout = TimeSpan.FromSeconds(20),
            MaxResponseContentBufferSize = TamanoMaximoRespuesta
        };

        private readonly string baseUrl;

        public ClienteRESTEureka()
        {
            baseUrl = ObtenerBaseConfigurada();
        }

        public SesionModelo? Sesion { get; private set; }

        public string EndpointActivo => baseUrl;

        // URL base de la API, incluida la ruta /api (RNF-010)
        private static string ObtenerBaseConfigurada()
        {
            string archivo = Path.Combine(AppContext.BaseDirectory, ArchivoConfiguracion);
            string? configurada;
            try
            {
                using JsonDocument configuracion = JsonDocument.Parse(File.ReadAllText(archivo));
                configurada = configuracion.RootElement.ValueKind == JsonValueKind.Object &&
                        configuracion.RootElement.TryGetProperty(PropiedadUrl, out JsonElement propiedad) &&
                        propiedad.ValueKind == JsonValueKind.String
                    ? propiedad.GetString()
                    : null;
            }
            catch (Exception ex) when (ex is IOException || ex is UnauthorizedAccessException || ex is JsonException)
            {
                throw new ArgumentException("No se pudo leer " + ArchivoConfiguracion + ".", ex);
            }

            if (string.IsNullOrWhiteSpace(configurada))
                throw new ArgumentException("La URL del servidor (" + PropiedadUrl + ") es obligatoria.");
            string normalizada = configurada.Trim().TrimEnd('/');
            if (!Uri.TryCreate(normalizada, UriKind.Absolute, out Uri? direccion) ||
                (direccion.Scheme != Uri.UriSchemeHttp && direccion.Scheme != Uri.UriSchemeHttps) ||
                direccion.Query.Length > 0 || direccion.Fragment.Length > 0)
                throw new ArgumentException("La URL del servidor debe ser HTTP o HTTPS, sin consulta ni fragmento.");
            return normalizada;
        }

        // ---------------------------------------------------------------- Autenticación

        public async Task<SesionModelo> IniciarSesionAsync(string usuario, string clave)
        {
            Sesion = null;
            var datos = new { usuario, clave, equipo = Environment.MachineName };
            RespuestaDto respuesta = await SolicitarAsync(HttpMethod.Post, "/autenticacion/login", datos, null);

            string token = TextoObligatorio(respuesta.Token, "token");
            if (!DateTimeOffset.TryParse(respuesta.ExpiraEn, CultureInfo.InvariantCulture,
                    DateTimeStyles.RoundtripKind, out DateTimeOffset expiraEn))
                throw new ErrorServicioModelo(null, "El servidor devolvio una expiracion de sesion invalida.");

            Sesion = new SesionModelo(token, expiraEn, respuesta.CodigoEmpleado, respuesta.NombreEmpleado,
                respuesta.CodigoSucursal, respuesta.Sucursal);
            return Sesion;
        }

        /// <summary>
        /// Invalida el token en el servidor (RQF-002). La sesión local se elimina siempre; si el
        /// token ya había caducado (AUTH-003) no se considera error.
        /// </summary>
        public async Task CerrarSesionAsync()
        {
            if (Sesion == null) return;
            string token = Sesion.Token;
            Sesion = null;
            try
            {
                await SolicitarAsync(HttpMethod.Post, "/autenticacion/cerrar-sesion", null, token);
            }
            catch (ErrorServicioModelo ex) when (ex.RequiereNuevoLogin)
            {
                // El servidor ya había invalidado el token.
            }
        }

        // ---------------------------------------------------------------- Consultas

        public async Task<CuentaModelo> ConsultarCuentaAsync(string cuenta)
        {
            RespuestaDto respuesta = await SolicitarAsync(HttpMethod.Get, "/cuentas/" + Segmento(cuenta), null, TokenActivo());
            DatosCuenta datos = respuesta.Cuenta
                ?? throw new ErrorServicioModelo(null, "El servidor no devolvio cuenta.");
            return new CuentaModelo(TextoObligatorio(datos.Codigo, "codigo"), datos.Titular, datos.Moneda,
                datos.Saldo, datos.Estado, datos.Sucursal, datos.FechaCreacion);
        }

        /// <summary>Una cuenta sin movimientos devuelve una lista vacía.</summary>
        public async Task<List<MovimientoModelo>> ConsultarMovimientosAsync(string cuenta)
        {
            RespuestaDto respuesta = await SolicitarAsync(HttpMethod.Get,
                "/cuentas/" + Segmento(cuenta) + "/movimientos", null, TokenActivo());
            if (respuesta.Movimientos == null) return new List<MovimientoModelo>();
            return respuesta.Movimientos
                .Select(m => new MovimientoModelo(m.Numero, m.Fecha, m.Tipo, m.Accion, m.Importe, m.CuentaReferencia))
                .ToList();
        }

        // ---------------------------------------------------------------- Transacciones

        public async Task<ResultadoTransaccionModelo> DepositarAsync(string cuenta, decimal importe)
        {
            return AResultado(await SolicitarAsync(HttpMethod.Post, "/transacciones/depositos",
                new { cuenta, importe }, TokenActivo()));
        }

        public async Task<ResultadoTransaccionModelo> RetirarAsync(string cuenta, decimal importe)
        {
            return AResultado(await SolicitarAsync(HttpMethod.Post, "/transacciones/retiros",
                new { cuenta, importe }, TokenActivo()));
        }

        public async Task<ResultadoTransaccionModelo> TransferirAsync(string cuentaOrigen, string cuentaDestino, decimal importe)
        {
            return AResultado(await SolicitarAsync(HttpMethod.Post, "/transacciones/transferencias",
                new { cuentaOrigen, cuentaDestino, importe }, TokenActivo()));
        }

        private static ResultadoTransaccionModelo AResultado(RespuestaDto respuesta)
        {
            return new ResultadoTransaccionModelo(respuesta.Mensaje, respuesta.NuevoSaldo,
                respuesta.NumeroMovimiento, respuesta.NumeroMovimientoDestino);
        }

        // ---------------------------------------------------------------- HTTP

        private string TokenActivo()
        {
            return Sesion?.Token ?? throw new ErrorServicioModelo(SesionInvalida, "Debe iniciar sesion.");
        }

        /// <summary>
        /// Envía la petición (con el token si se indica) y devuelve el JSON de una respuesta correcta
        /// (estado OK). Un estado distinto de 2xx trae el JSON de error {estado, codigo, mensaje};
        /// una falla de red es un error de comunicación con el servidor.
        /// </summary>
        private async Task<RespuestaDto> SolicitarAsync(HttpMethod metodo, string ruta, object? cuerpo, string? token)
        {
            using var peticion = new HttpRequestMessage(metodo, baseUrl + ruta);
            peticion.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
            if (token != null)
                peticion.Headers.Authorization = new AuthenticationHeaderValue("Bearer", token);
            if (cuerpo != null)
                peticion.Content = JsonContent.Create(cuerpo, options: OpcionesJson);

            string contenido;
            int estado;
            try
            {
                using HttpResponseMessage respuesta = await Http.SendAsync(peticion);
                estado = (int)respuesta.StatusCode;
                contenido = await respuesta.Content.ReadAsStringAsync();
            }
            catch (OperationCanceledException)
            {
                throw new ErrorServicioModelo(null, "El servidor " + baseUrl + " tardo demasiado en responder.");
            }
            catch (HttpRequestException)
            {
                throw new ErrorServicioModelo(null, "No se pudo comunicar con el servidor " + baseUrl
                    + ". Verifique que Kestrel este activo.");
            }

            RespuestaDto? json = LeerJson(contenido);
            if (estado < 200 || estado >= 300)
            {
                if (json?.Codigo == SesionInvalida) Sesion = null;
                string mensaje = string.IsNullOrWhiteSpace(json?.Mensaje) ? "Error HTTP " + estado + "." : json.Mensaje;
                throw new ErrorServicioModelo(json?.Codigo, mensaje);
            }
            if (json == null || json.Estado != EstadoOk)
                throw new ErrorServicioModelo(null, "El servidor devolvio una respuesta inesperada.");
            return json;
        }

        // El controlador ya validó los 8 dígitos; se codifica igual para no alterar la ruta
        private static string Segmento(string valor) => Uri.EscapeDataString(valor);

        // ---------------------------------------------------------------- JSON

        private static RespuestaDto? LeerJson(string contenido)
        {
            try
            {
                return JsonSerializer.Deserialize<RespuestaDto>(contenido, OpcionesJson);
            }
            catch (JsonException)
            {
                return null;
            }
        }

        private static string TextoObligatorio(string? valor, string campo)
        {
            if (string.IsNullOrWhiteSpace(valor))
                throw new ErrorServicioModelo(null, "El servidor no devolvio " + campo + ".");
            return valor;
        }

        /// <summary>Respuesta JSON de todas las operaciones (sección 8.2.7 del ERS) y también del error.</summary>
        private sealed class RespuestaDto
        {
            public string? Estado { get; set; }
            public string? Codigo { get; set; }
            public string? Mensaje { get; set; }
            public string? Token { get; set; }
            public string? ExpiraEn { get; set; }
            public string? CodigoEmpleado { get; set; }
            public string? NombreEmpleado { get; set; }
            public string? CodigoSucursal { get; set; }
            public string? Sucursal { get; set; }
            public DatosCuenta? Cuenta { get; set; }
            public List<DatosMovimiento>? Movimientos { get; set; }
            public decimal? NuevoSaldo { get; set; }
            public int? NumeroMovimiento { get; set; }
            public int? NumeroMovimientoDestino { get; set; }
        }

        private sealed class DatosCuenta
        {
            public string? Codigo { get; set; }
            public string? Titular { get; set; }
            public string? Moneda { get; set; }
            public decimal Saldo { get; set; }
            public string? Estado { get; set; }
            public string? Sucursal { get; set; }
            public string? FechaCreacion { get; set; }
        }

        private sealed class DatosMovimiento
        {
            public int Numero { get; set; }
            public string? Fecha { get; set; }
            public string? Tipo { get; set; }
            public string? Accion { get; set; }
            public decimal Importe { get; set; }
            public string? CuentaReferencia { get; set; }
        }
    }
}
