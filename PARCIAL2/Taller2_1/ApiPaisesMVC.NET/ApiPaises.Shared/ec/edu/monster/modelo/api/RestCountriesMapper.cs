using System.Text.Json;
using System.Text.RegularExpressions;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;

namespace ec.edu.monster.modelo.api;

/// <summary>
/// Convierte el JSON de REST Countries v5 en objetos <see cref="Pais"/>. Es la única clase que
/// conoce la estructura de la respuesta; en el enunciado este trabajo lo hacía el controlador.
/// <code>
/// { "data": {
///     "objects": [ { "names": {"common": "Ecuador"}, "codes": {"alpha_3": "ECU"},
///                    "capitals": [ {"name": "Quito", "attributes": {"primary": true}} ],
///                    "region": "Americas", "population": 18000000,
///                    "currencies": [ {"code": "USD", "name": "United States dollar"} ] } ],
///     "meta": { "total": 249, "more": true } } }
/// </code>
/// Los países sin nombre, código ISO o región conocida se descartan (y se registran en el log); los
/// que no tienen capital o moneda se aceptan con ese dato vacío.
/// </summary>
public sealed partial class RestCountriesMapper(ILogger<RestCountriesMapper>? logger = null)
{
    private readonly ILogger log = (ILogger?)logger ?? NullLogger.Instance;

    /// <summary>Una página de resultados.</summary>
    /// <param name="Paises">países válidos de la página</param>
    /// <param name="Recibidos">objetos recibidos, válidos o no (sirve para avanzar el offset)</param>
    /// <param name="HayMas">la API indica que existen más páginas</param>
    /// <param name="EsDemo">la respuesta proviene de la clave demo (datos de muestra)</param>
    public sealed record Pagina(IReadOnlyList<Pais> Paises, int Recibidos, bool HayMas, bool EsDemo);

    [GeneratedRegex("^[A-Za-z]{3}$")]
    private static partial Regex CodigoIso();

    /// <exception cref="ExcepcionApiExterna">si el JSON no es válido o no tiene la forma esperada</exception>
    public Pagina LeerPagina(string? json)
    {
        JsonDocument documento;
        try
        {
            documento = JsonDocument.Parse(json ?? "");
        }
        catch (JsonException e)
        {
            throw new ExcepcionApiExterna("La API devolvió un JSON no válido.", e);
        }
        using (documento)
        {
            JsonElement raiz = documento.RootElement;
            if (raiz.ValueKind != JsonValueKind.Object || !Objeto(raiz, "data", out JsonElement data))
            {
                throw new ExcepcionApiExterna("La API devolvió una respuesta con un formato inesperado.");
            }
            if (!data.TryGetProperty("objects", out JsonElement objetos) || objetos.ValueKind != JsonValueKind.Array)
            {
                throw new ExcepcionApiExterna("La respuesta de la API no contiene la lista de países.");
            }

            List<Pais> paises = [];
            foreach (JsonElement elemento in objetos.EnumerateArray())
            {
                if (elemento.ValueKind == JsonValueKind.Object && APais(elemento) is Pais pais)
                {
                    paises.Add(pais);
                }
            }
            bool hayMas = Objeto(data, "meta", out JsonElement meta) && Booleano(meta, "more");
            return new Pagina(paises, objetos.GetArrayLength(), hayMas, data.TryGetProperty("_demo", out _));
        }
    }

    private Pais? APais(JsonElement json)
    {
        string? nombre = Objeto(json, "names", out JsonElement nombres) ? Texto(nombres, "common") : null;
        string? codigo = Objeto(json, "codes", out JsonElement codigos) ? Texto(codigos, "alpha_3") : null;
        string? valorRegion = Texto(json, "region");
        Region? region = Regiones.DesdeApi(valorRegion);

        if (nombre is null || codigo is null || !CodigoIso().IsMatch(codigo) || region is null)
        {
            log.LogWarning("Se descarta un país incompleto de la API: nombre={Nombre}, código={Codigo}, región={Region}",
                nombre, codigo, valorRegion);
            return null;
        }
        return Pais.Nuevo(codigo, nombre, CapitalPrincipal(json), region, Poblacion(json), MonedaPrincipal(json));
    }

    /// <summary>La capital marcada como principal; si ninguna lo está, la primera. Acepta también textos simples.</summary>
    private static string? CapitalPrincipal(JsonElement json)
    {
        if (!json.TryGetProperty("capitals", out JsonElement capitales) || capitales.ValueKind != JsonValueKind.Array)
        {
            return null;
        }
        string? primera = null;
        foreach (JsonElement capital in capitales.EnumerateArray())
        {
            string? nombre;
            bool principal = false;
            if (capital.ValueKind == JsonValueKind.Object)
            {
                nombre = Texto(capital, "name");
                principal = Objeto(capital, "attributes", out JsonElement atributos) && Booleano(atributos, "primary");
            }
            else
            {
                nombre = ComoTexto(capital);
            }
            if (nombre != null && principal)
            {
                return nombre;
            }
            primera ??= nombre;
        }
        return primera;
    }

    /// <summary>Nombre de la primera moneda. La v5 envía una lista; también se acepta un mapa código → objeto.</summary>
    private static string? MonedaPrincipal(JsonElement json)
    {
        if (!json.TryGetProperty("currencies", out JsonElement monedas))
        {
            return null;
        }
        JsonElement? primera = monedas.ValueKind switch
        {
            JsonValueKind.Array when monedas.GetArrayLength() > 0 => monedas[0],
            JsonValueKind.Object => monedas.EnumerateObject().Select(p => (JsonElement?)p.Value).FirstOrDefault(),
            _ => null
        };
        if (primera is not JsonElement moneda)
        {
            return null;
        }
        return moneda.ValueKind == JsonValueKind.Object ? Texto(moneda, "name") : ComoTexto(moneda);
    }

    private static long Poblacion(JsonElement json)
    {
        if (json.TryGetProperty("population", out JsonElement valor) && valor.ValueKind == JsonValueKind.Number
            && valor.TryGetDecimal(out decimal numero))
        {
            return Math.Max(0, (long)numero);
        }
        return 0;
    }

    // ---------------------------------------------- lectura segura del árbol

    private static bool Objeto(JsonElement padre, string campo, out JsonElement valor)
    {
        return padre.TryGetProperty(campo, out valor) && valor.ValueKind == JsonValueKind.Object;
    }

    private static string? Texto(JsonElement padre, string campo)
    {
        return padre.TryGetProperty(campo, out JsonElement valor) ? ComoTexto(valor) : null;
    }

    private static string? ComoTexto(JsonElement valor)
    {
        if (valor.ValueKind != JsonValueKind.String)
        {
            return null;
        }
        string? texto = valor.GetString()?.Trim();
        return string.IsNullOrEmpty(texto) ? null : texto;
    }

    private static bool Booleano(JsonElement padre, string campo)
    {
        return padre.TryGetProperty(campo, out JsonElement valor) && valor.ValueKind == JsonValueKind.True;
    }
}
