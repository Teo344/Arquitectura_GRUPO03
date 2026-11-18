using System.Net;
using ec.edu.monster.config;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.modelo.api
{
    /// <summary>
    /// Cliente de la API pública de universidades de Hipolabs
    /// (http://universities.hipolabs.com/search?country=Ecuador). No requiere clave.
    /// </summary>
    public sealed class HipolabsCliente : IUniversidadApiCliente
    {
        private readonly HttpClient http;
        private readonly string urlBase;
        private readonly HipolabsMapper mapper;
        private readonly ILogger<HipolabsCliente> log;

        public HipolabsCliente(HttpClient http, Configuracion configuracion, HipolabsMapper mapper,
            ILogger<HipolabsCliente> log)
        {
            ArgumentNullException.ThrowIfNull(http);
            ArgumentNullException.ThrowIfNull(configuracion);
            this.http = http;
            this.http.Timeout = configuracion.ApiTimeout;
            urlBase = configuracion.UniversidadesUrlBase.Trim().TrimEnd('/');
            this.mapper = mapper ?? throw new ArgumentNullException(nameof(mapper));
            this.log = log ?? throw new ArgumentNullException(nameof(log));
        }

        public async Task<IReadOnlyList<Universidad>> BuscarPorPaisAsync(string pais)
        {
            if (string.IsNullOrWhiteSpace(pais))
            {
                throw new ArgumentException("El país a buscar no puede estar vacío");
            }
            string url = urlBase + "/search?country=" + Uri.EscapeDataString(pais.Trim());
            HttpResponseMessage respuesta;
            try
            {
                respuesta = await http.GetAsync(url);
            }
            catch (TaskCanceledException e)
            {
                throw new ExcepcionApiExterna("La API de universidades no respondió a tiempo.", e);
            }
            catch (HttpRequestException e)
            {
                throw new ExcepcionApiExterna(
                    "No se pudo conectar con la API de universidades. Verifique la conexión a Internet.", e);
            }

            using (respuesta)
            {
                string cuerpo = await respuesta.Content.ReadAsStringAsync();
                if (respuesta.StatusCode == HttpStatusCode.OK)
                {
                    return mapper.LeerLista(cuerpo);
                }
                int estado = (int)respuesta.StatusCode;
                log.LogWarning("Hipolabs respondió {Estado}", estado);
                throw new ExcepcionApiExterna(estado >= 500
                    ? "La API de universidades no está disponible en este momento (error " + estado + ")."
                    : "La API de universidades respondió con un error inesperado (" + estado + ").");
            }
        }
    }
}
