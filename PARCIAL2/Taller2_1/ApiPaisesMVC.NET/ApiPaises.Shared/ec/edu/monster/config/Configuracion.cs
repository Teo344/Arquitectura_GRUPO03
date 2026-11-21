using System.Text.Json;

namespace ec.edu.monster.config;

/// <summary>
/// Parámetros externos de la aplicación, leídos de <c>configuracion.json</c>. Sacarlos del código
/// evita credenciales escritas en las clases (el enunciado tenía root/12345 en Conexion) y permite
/// cambiar de entorno sin recompilar.
/// </summary>
public sealed record Configuracion(string CadenaConexion, string ApiUrlBase, string ApiClave, TimeSpan ApiTimeout)
{
    public const string Archivo = "configuracion.json";
    private const int TimeoutPorDefecto = 15;

    /// <summary>
    /// Lee el JSON:
    /// <c>{ "baseDatos": { "cadenaConexion": "…" }, "api": { "urlBase": "…", "clave": "…", "timeoutSegundos": 15 } }</c>
    /// </summary>
    /// <exception cref="InvalidOperationException">si el JSON no es válido o falta un dato obligatorio</exception>
    public static Configuracion Leer(Stream json)
    {
        try
        {
            using JsonDocument documento = JsonDocument.Parse(json);
            JsonElement raiz = documento.RootElement;
            return new Configuracion(
                Requerido(raiz, "baseDatos", "cadenaConexion"),
                Requerido(raiz, "api", "urlBase"),
                Requerido(raiz, "api", "clave"),
                TimeSpan.FromSeconds(Timeout(raiz)));
        }
        catch (JsonException e)
        {
            throw new InvalidOperationException(Archivo + " no es un JSON válido.", e);
        }
    }

    private static string Requerido(JsonElement raiz, string seccion, string clave)
    {
        if (raiz.TryGetProperty(seccion, out JsonElement grupo)
            && grupo.TryGetProperty(clave, out JsonElement valor)
            && valor.ValueKind == JsonValueKind.String
            && !string.IsNullOrWhiteSpace(valor.GetString()))
        {
            return valor.GetString()!.Trim();
        }
        throw new InvalidOperationException($"Falta {seccion}.{clave} en {Archivo}.");
    }

    private static int Timeout(JsonElement raiz)
    {
        if (raiz.TryGetProperty("api", out JsonElement api)
            && api.TryGetProperty("timeoutSegundos", out JsonElement valor)
            && valor.TryGetInt32(out int segundos))
        {
            return segundos > 0
                ? segundos
                : throw new InvalidOperationException($"api.timeoutSegundos debe ser positivo en {Archivo}.");
        }
        return TimeoutPorDefecto;
    }

    /// <summary>No muestra la clave de la API si el objeto llega a imprimirse en un log.</summary>
    public override string ToString() => $"Configuracion {{ ApiUrlBase = {ApiUrlBase}, ApiTimeout = {ApiTimeout} }}";
}
