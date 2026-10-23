using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicio
{
    /// <summary>Única capa que conoce HTTP, JSON y las rutas del servidor REST.</summary>
    public class ClienteRESTConversor
    {
        // Un solo HttpClient para toda la aplicación, como recomienda .NET.
        private static readonly HttpClient Http = new HttpClient(new SocketsHttpHandler
        {
            ConnectTimeout = TimeSpan.FromSeconds(8),
            PooledConnectionLifetime = TimeSpan.FromMinutes(2),
            AllowAutoRedirect = false
        })
        {
            Timeout = TimeSpan.FromSeconds(20)
        };

        private readonly Uri baseUri;
        private SesionModelo? sesion;

        public ClienteRESTConversor() : this(ObtenerBaseConfigurada())
        {
        }

        public ClienteRESTConversor(string baseUrl)
        {
            if (string.IsNullOrWhiteSpace(baseUrl) ||
                !Uri.TryCreate(baseUrl.Trim().TrimEnd('/') + "/", UriKind.Absolute, out Uri? direccion) ||
                (direccion.Scheme != Uri.UriSchemeHttp && direccion.Scheme != Uri.UriSchemeHttps) ||
                !string.IsNullOrEmpty(direccion.Query) || !string.IsNullOrEmpty(direccion.Fragment))
                throw new ArgumentException("Configure una URL REST absoluta HTTP o HTTPS, sin consulta ni fragmento.");
            baseUri = direccion;
        }

        // Variable de entorno REST_BASE_URL o, en su defecto, rest.base.url de appsettings.json.
        private static string ObtenerBaseConfigurada()
        {
            string? variable = Environment.GetEnvironmentVariable("REST_BASE_URL");
            if (!string.IsNullOrWhiteSpace(variable))
                return variable;

            string archivo = Path.Combine(AppContext.BaseDirectory, "appsettings.json");
            try
            {
                using JsonDocument configuracion = JsonDocument.Parse(File.ReadAllText(archivo));
                if (configuracion.RootElement.ValueKind == JsonValueKind.Object &&
                    configuracion.RootElement.TryGetProperty("rest.base.url", out JsonElement propiedad) &&
                    propiedad.ValueKind == JsonValueKind.String &&
                    !string.IsNullOrWhiteSpace(propiedad.GetString()))
                    return propiedad.GetString()!;
            }
            catch (Exception ex) when (ex is IOException or UnauthorizedAccessException or JsonException)
            {
                throw new ArgumentException("No se pudo leer appsettings.json. Configure rest.base.url o REST_BASE_URL.", ex);
            }
            throw new ArgumentException("Configure rest.base.url en appsettings.json o la variable REST_BASE_URL.");
        }

        public string EndpointActivo => baseUri.AbsoluteUri;

        public async Task<SesionModelo> LoginAsync(string usuario, string clave)
        {
            CerrarSesion();
            RespuestaDto respuesta = await SolicitarAsync(HttpMethod.Post, "autenticacion/login",
                new CredencialesDto { Usuario = usuario, Clave = clave }, false);
            string token = TextoObligatorio(respuesta.Token, "token");
            if (!respuesta.ExpiraEn.HasValue)
                throw RespuestaInvalida("El servidor no devolvió la expiración de la sesión.");
            sesion = new SesionModelo(usuario, token, respuesta.ExpiraEn.Value);
            return sesion;
        }

        // La API no tiene logout: se elimina el token de este proceso.
        public void CerrarSesion() => sesion = null;

        public async Task<List<CategoriaModelo>> ConsultarCatalogoAsync()
        {
            RespuestaDto respuesta = await SolicitarAsync(HttpMethod.Get, "catalogo/categorias", null, true);
            if (respuesta.Categorias == null)
                throw RespuestaInvalida("El servidor no devolvió las categorías.");

            var categorias = new List<CategoriaModelo>();
            foreach (CategoriaDto? categoria in respuesta.Categorias)
            {
                if (categoria == null || !categoria.Convertible.HasValue)
                    throw RespuestaInvalida("El servidor devolvió una categoría incompleta.");
                categorias.Add(new CategoriaModelo(
                    TextoObligatorio(categoria.Codigo, "codigo"),
                    TextoObligatorio(categoria.Nombre, "nombre"),
                    categoria.Convertible.Value));
            }
            return categorias;
        }

        public async Task<List<UnidadModelo>> ConsultarUnidadesAsync(string categoria)
        {
            string codigo = Uri.EscapeDataString(categoria);
            RespuestaDto respuesta = await SolicitarAsync(HttpMethod.Get,
                "catalogo/categorias/" + codigo + "/unidades", null, true);
            if (respuesta.Unidades == null)
                throw RespuestaInvalida("El servidor no devolvió las unidades.");

            var unidades = new List<UnidadModelo>();
            foreach (UnidadDto? unidad in respuesta.Unidades)
            {
                if (unidad == null)
                    throw RespuestaInvalida("El servidor devolvió una unidad incompleta.");
                unidades.Add(new UnidadModelo(
                    TextoObligatorio(unidad.Codigo, "codigo"),
                    TextoObligatorio(unidad.Nombre, "nombre"),
                    TextoObligatorio(unidad.Simbolo, "simbolo")));
            }
            return unidades;
        }

        public Task<double> ConvertirLongitudAsync(double valor, string origen, string destino)
            => ConvertirAsync("longitud", valor, origen, destino);

        public Task<double> ConvertirMasaAsync(double valor, string origen, string destino)
            => ConvertirAsync("masa", valor, origen, destino);

        public Task<double> ConvertirTemperaturaAsync(double valor, string origen, string destino)
            => ConvertirAsync("temperatura", valor, origen, destino);

        private async Task<double> ConvertirAsync(string tipo, double valor, string origen, string destino)
        {
            RespuestaDto respuesta = await SolicitarAsync(HttpMethod.Post, "conversiones/" + tipo,
                new SolicitudConversionDto { Valor = valor, UnidadOrigen = origen, UnidadDestino = destino }, true);
            if (!respuesta.Resultado.HasValue || !double.IsFinite(respuesta.Resultado.Value))
                throw RespuestaInvalida("El servidor no devolvió un resultado numérico finito.");
            return respuesta.Resultado.Value;
        }

        private async Task<RespuestaDto> SolicitarAsync(HttpMethod metodo, string ruta, object? cuerpo, bool protegido)
        {
            using var peticion = new HttpRequestMessage(metodo, new Uri(baseUri, ruta));
            peticion.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
            if (protegido)
            {
                if (sesion == null)
                    throw new ErrorRESTModelo(401, "AUTH-001", "Debe iniciar sesión.");
                peticion.Headers.Authorization = new AuthenticationHeaderValue("Bearer", sesion.Token);
            }
            if (cuerpo != null)
                peticion.Content = new StringContent(JsonSerializer.Serialize(cuerpo), Encoding.UTF8, "application/json");

            try
            {
                using HttpResponseMessage respuesta = await Http.SendAsync(peticion);
                string contenido = await respuesta.Content.ReadAsStringAsync();
                RespuestaDto? datos = LeerRespuesta(contenido);
                int estadoHttp = (int)respuesta.StatusCode;

                if (!respuesta.IsSuccessStatusCode)
                {
                    var error = new ErrorRESTModelo(estadoHttp, datos?.Codigo,
                        string.IsNullOrWhiteSpace(datos?.Mensaje) ? $"Error HTTP {estadoHttp} del servidor REST." : datos.Mensaje);
                    if (error.RequiereNuevoLogin)
                        CerrarSesion();
                    throw error;
                }
                if (datos == null)
                    throw new ErrorRESTModelo("La respuesta REST no contiene JSON válido.", null);
                if (datos.Estado != "OK")
                    throw new ErrorRESTModelo("El servidor devolvió un estado inesperado.", null);
                return datos;
            }
            catch (OperationCanceledException ex)
            {
                throw new ErrorRESTModelo("El servidor REST tardó demasiado en responder: " + EndpointActivo, ex);
            }
            catch (HttpRequestException ex)
            {
                throw new ErrorRESTModelo("No se pudo conectar con el servidor REST: " + EndpointActivo +
                    ". Verifique que esté iniciado y que la URL sea correcta.", ex);
            }
            catch (IOException ex)
            {
                throw new ErrorRESTModelo("No se pudo completar la comunicación con el servidor REST: " + EndpointActivo, ex);
            }
        }

        private static RespuestaDto? LeerRespuesta(string contenido)
        {
            try
            {
                return JsonSerializer.Deserialize<RespuestaDto>(contenido);
            }
            catch (JsonException)
            {
                return null;
            }
        }

        private static string TextoObligatorio(string? valor, string campo)
        {
            if (string.IsNullOrWhiteSpace(valor))
                throw RespuestaInvalida("El servidor no devolvió un valor válido para " + campo + ".");
            return valor;
        }

        private static ErrorRESTModelo RespuestaInvalida(string mensaje) => new ErrorRESTModelo(mensaje, null);
    }
}
