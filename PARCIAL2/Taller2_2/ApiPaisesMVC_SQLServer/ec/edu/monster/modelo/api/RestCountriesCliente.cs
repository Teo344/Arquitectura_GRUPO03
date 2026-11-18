using System.Net;
using ec.edu.monster.config;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.modelo.api
{
    /// <summary>
    /// Cliente de REST Countries v5 (https://api.restcountries.com/countries/v5).
    /// Cambios respecto al enunciado, que usaba la v3.1 (ya retirada: hoy responde "This API version
    /// has been deprecated") con un HttpClient creado en el controlador:
    /// <list type="bullet">
    /// <item>HttpClient inyectado (AddHttpClient) con tiempo máximo de respuesta.</item>
    /// <item>Se revisa el código HTTP y cada error se traduce a un mensaje claro.</item>
    /// <item>La v5 exige API key (cabecera Authorization) y pagina de a 100 países en el plan gratuito,
    /// por eso <see cref="ObtenerTodosAsync"/> recorre las páginas.</item>
    /// <item>Solo se piden los campos que se usan (response_fields).</item>
    /// </list>
    /// </summary>
    public sealed class RestCountriesCliente : IPaisApiCliente
    {
        internal const string Campos = "names.common,codes.alpha_3,capitals,region,subregion,"
            + "area.kilometers,population,currencies";

        /// <summary>Máximo permitido por el plan gratuito.</summary>
        internal const int TamanoPagina = 100;

        /// <summary>Tope de seguridad (1000 países) por si la API indicara "hay más" sin fin.</summary>
        private const int MaxPaginas = 10;

        private const int MaxLogCuerpo = 300;

        /// <summary>Clave pública de REST Countries: no consume cuota, pero devuelve un solo país de muestra.</summary>
        internal const string ClaveDemo = "rc_live_demo";

        private readonly HttpClient http;
        private readonly string urlBase;
        private readonly string clave;
        private readonly RestCountriesMapper mapper;
        private readonly ILogger<RestCountriesCliente> log;

        public RestCountriesCliente(HttpClient http, Configuracion configuracion, RestCountriesMapper mapper,
            ILogger<RestCountriesCliente> log)
        {
            ArgumentNullException.ThrowIfNull(http);
            ArgumentNullException.ThrowIfNull(configuracion);
            if (string.IsNullOrWhiteSpace(configuracion.ApiClave))
            {
                throw new ArgumentException("Falta la API key de REST Countries (RestCountries:Clave)");
            }
            this.http = http;
            this.http.Timeout = configuracion.ApiTimeout;
            urlBase = configuracion.ApiUrlBase.Trim().TrimEnd('/');
            clave = configuracion.ApiClave.Trim();
            this.mapper = mapper ?? throw new ArgumentNullException(nameof(mapper));
            this.log = log ?? throw new ArgumentNullException(nameof(log));
        }

        public async Task<IReadOnlyList<Pais>> ObtenerTodosAsync()
        {
            List<Pais> todos = new List<Pais>();
            int offset = 0;
            for (int numero = 0; numero < MaxPaginas; numero++)
            {
                RestCountriesMapper.Pagina pagina = mapper.LeerPagina(await ConsultarAsync(
                    urlBase + "?limit=" + TamanoPagina + "&offset=" + offset + "&response_fields=" + Campos));
                AvisarSiEsDemo(pagina);
                todos.AddRange(pagina.Paises);
                if (!pagina.HayMas || pagina.Recibidos == 0)
                {
                    return todos;
                }
                offset += pagina.Recibidos;
            }
            log.LogWarning("Se alcanzó el tope de {MaxPaginas} páginas; la importación puede estar incompleta", MaxPaginas);
            return todos;
        }

        public async Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string nombre)
        {
            // Una consulta vacía responde 400, y muchos 4xx pueden suspender la cuenta gratuita
            if (string.IsNullOrWhiteSpace(nombre))
            {
                throw new ArgumentException("El nombre a buscar no puede estar vacío");
            }
            string consulta = Uri.EscapeDataString(nombre.Trim());
            // Agregado "name": busca en el nombre común, oficial, alternativos y nativos
            RestCountriesMapper.Pagina pagina = mapper.LeerPagina(await ConsultarAsync(
                urlBase + "/name?q=" + consulta + "&limit=" + TamanoPagina + "&response_fields=" + Campos));
            AvisarSiEsDemo(pagina);
            return pagina.Paises;
        }

        public bool EsModoDemostracion => clave == ClaveDemo;

        private async Task<string> ConsultarAsync(string url)
        {
            using HttpRequestMessage solicitud = new HttpRequestMessage(HttpMethod.Get, url);
            solicitud.Headers.Add("Authorization", "Bearer " + clave);
            solicitud.Headers.Add("Accept", "application/json");
            HttpResponseMessage respuesta;
            try
            {
                respuesta = await http.SendAsync(solicitud);
            }
            catch (TaskCanceledException e)
            {
                throw new ExcepcionApiExterna("La API REST Countries no respondió a tiempo.", e);
            }
            catch (HttpRequestException e)
            {
                throw new ExcepcionApiExterna(
                    "No se pudo conectar con la API REST Countries. Verifique la conexión a Internet.", e);
            }

            using (respuesta)
            {
                string cuerpo = await respuesta.Content.ReadAsStringAsync();
                int estado = (int)respuesta.StatusCode;
                if (respuesta.StatusCode == HttpStatusCode.OK)
                {
                    return cuerpo;
                }
                log.LogWarning("REST Countries respondió {Estado}: {Cuerpo}", estado, Recortar(cuerpo));
                throw new ExcepcionApiExterna(MensajePorEstado(estado));
            }
        }

        /// <summary>Mensaje para el usuario según los códigos que documenta la API.</summary>
        internal static string MensajePorEstado(int estado)
        {
            return estado switch
            {
                400 => "La API REST Countries rechazó la consulta por datos no válidos.",
                401 => "La API key de REST Countries falta o no es válida (revise RestCountries:Clave en appsettings.json).",
                403 => "La cuenta de REST Countries agotó su cuota mensual o no tiene acceso a ese dato.",
                404 => "La API REST Countries no reconoce la ruta consultada.",
                410 => "La versión configurada de la API REST Countries ya no está activa.",
                429 => "Demasiadas consultas seguidas a la API. Espere unos segundos e intente de nuevo.",
                >= 500 => "La API REST Countries no está disponible en este momento (error " + estado + ").",
                _ => "La API REST Countries respondió con un error inesperado (" + estado + ")."
            };
        }

        private void AvisarSiEsDemo(RestCountriesMapper.Pagina pagina)
        {
            if (pagina.EsDemo)
            {
                log.LogWarning("Se está usando la clave demo de REST Countries: la API solo devuelve un país de "
                    + "muestra. Configure una API key real en RestCountries:Clave para obtener todos los datos.");
            }
        }

        private static string Recortar(string? texto)
        {
            if (texto == null)
            {
                return "";
            }
            return texto.Length <= MaxLogCuerpo ? texto : texto[..MaxLogCuerpo] + "...";
        }
    }
}
