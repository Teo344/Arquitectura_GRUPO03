using System.Net;
using System.Net.Http.Headers;
using ec.edu.monster.config;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;

namespace ec.edu.monster.modelo.api;

/// <summary>
/// Cliente de REST Countries v5 (https://api.restcountries.com/countries/v5).
/// Cambios respecto al enunciado, que usaba la v3.1 (ya retirada) sin control de errores:
/// <list type="bullet">
/// <item>HttpClient con tiempo máximo: una API lenta no congela la aplicación.</item>
/// <item>Se revisa el código HTTP y cada error se traduce a un mensaje claro.</item>
/// <item>La v5 exige API key (cabecera Authorization) y pagina de a 100 países en el plan
/// gratuito, por eso <see cref="ObtenerTodosAsync"/> recorre las páginas.</item>
/// <item>Solo se piden los campos que se usan (<c>response_fields</c>).</item>
/// </list>
/// </summary>
public sealed class RestCountriesCliente : IPaisApiCliente
{
    internal const string Campos = "names.common,codes.alpha_3,capitals,region,population,currencies";
    /// <summary>Máximo permitido por el plan gratuito.</summary>
    internal const int TamanoPagina = 100;
    /// <summary>Tope de seguridad (1000 países) por si la API indicara "hay más" sin fin.</summary>
    private const int MaxPaginas = 10;
    /// <summary>Clave pública: no consume cuota, pero devuelve un solo país de muestra.</summary>
    internal const string ClaveDemo = "rc_live_demo";
    private const int MaxLogCuerpo = 300;

    private readonly HttpClient http;
    private readonly string urlBase;
    private readonly string clave;
    private readonly RestCountriesMapper mapper;
    private readonly ILogger log;

    public RestCountriesCliente(HttpClient http, Configuracion configuracion, RestCountriesMapper mapper,
        ILogger<RestCountriesCliente>? logger = null)
    {
        ArgumentNullException.ThrowIfNull(configuracion);
        if (string.IsNullOrWhiteSpace(configuracion.ApiClave))
        {
            throw new ArgumentException("Falta la API key de REST Countries (api.clave).", nameof(configuracion));
        }
        this.http = http ?? throw new ArgumentNullException(nameof(http));
        this.mapper = mapper ?? throw new ArgumentNullException(nameof(mapper));
        urlBase = configuracion.ApiUrlBase.Trim().TrimEnd('/');
        clave = configuracion.ApiClave.Trim();
        log = (ILogger?)logger ?? NullLogger.Instance;
    }

    public bool EsModoDemostracion => clave == ClaveDemo;

    public async Task<IReadOnlyList<Pais>> ObtenerTodosAsync()
    {
        List<Pais> todos = [];
        int offset = 0;
        for (int numero = 0; numero < MaxPaginas; numero++)
        {
            RestCountriesMapper.Pagina pagina = mapper.LeerPagina(await ConsultarAsync(
                $"{urlBase}?limit={TamanoPagina}&offset={offset}&response_fields={Campos}"));
            AvisarSiEsDemo(pagina);
            todos.AddRange(pagina.Paises);
            if (!pagina.HayMas || pagina.Recibidos == 0)
            {
                return todos;
            }
            offset += pagina.Recibidos;
        }
        log.LogWarning("Se alcanzó el tope de {Paginas} páginas; la importación puede estar incompleta", MaxPaginas);
        return todos;
    }

    public async Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string nombre)
    {
        // Una consulta vacía responde 400, y muchos 4xx pueden suspender la cuenta gratuita
        if (string.IsNullOrWhiteSpace(nombre))
        {
            throw new ArgumentException("El nombre a buscar no puede estar vacío.", nameof(nombre));
        }
        string consulta = Uri.EscapeDataString(nombre.Trim());
        // Agregado "name": busca en el nombre común, oficial, alternativos y nativos
        RestCountriesMapper.Pagina pagina = mapper.LeerPagina(await ConsultarAsync(
            $"{urlBase}/name?q={consulta}&limit={TamanoPagina}&response_fields={Campos}"));
        AvisarSiEsDemo(pagina);
        return pagina.Paises;
    }

    private async Task<string> ConsultarAsync(string url)
    {
        using HttpRequestMessage solicitud = new(HttpMethod.Get, url);
        solicitud.Headers.Authorization = new AuthenticationHeaderValue("Bearer", clave);
        solicitud.Headers.Accept.Add(new MediaTypeWithQualityHeaderValue("application/json"));
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
            if (respuesta.StatusCode == HttpStatusCode.OK)
            {
                return cuerpo;
            }
            log.LogWarning("REST Countries respondió {Estado}: {Cuerpo}", (int)respuesta.StatusCode,
                cuerpo.Length <= MaxLogCuerpo ? cuerpo : cuerpo[..MaxLogCuerpo] + "...");
            throw new ExcepcionApiExterna(MensajePorEstado((int)respuesta.StatusCode));
        }
    }

    /// <summary>Mensaje para el usuario según los códigos que documenta la API.</summary>
    internal static string MensajePorEstado(int estado) => estado switch
    {
        400 => "La API REST Countries rechazó la consulta por datos no válidos.",
        401 => "La API key de REST Countries falta o no es válida (revise api.clave en configuracion.json).",
        403 => "La cuenta de REST Countries agotó su cuota mensual o no tiene acceso a ese dato.",
        404 => "La API REST Countries no reconoce la ruta consultada.",
        410 => "La versión configurada de la API REST Countries ya no está activa.",
        429 => "Demasiadas consultas seguidas a la API. Espere unos segundos e intente de nuevo.",
        >= 500 => $"La API REST Countries no está disponible en este momento (error {estado}).",
        _ => $"La API REST Countries respondió con un error inesperado ({estado})."
    };

    private void AvisarSiEsDemo(RestCountriesMapper.Pagina pagina)
    {
        if (pagina.EsDemo)
        {
            log.LogWarning("Se está usando la clave demo de REST Countries: la API solo devuelve un país de muestra.");
        }
    }
}
