using System.Globalization;
using System.Text;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.modelo.entidad;

/// <summary>
/// Regiones que maneja REST Countries. En la base de datos se guarda el nombre de la constante en
/// mayúsculas (AFRICA, AMERICAS…), que no cambia aunque cambie la traducción.
/// </summary>
public enum Region
{
    Africa,
    Americas,
    Antartida,
    Asia,
    Europa,
    Oceania
}

/// <summary>Traducciones de <see cref="Region"/>: valor de la API, nombre en español y valor de la base.</summary>
public static class Regiones
{
    private static readonly IReadOnlyDictionary<Region, (string Api, string Nombre)> Datos =
        new Dictionary<Region, (string, string)>
        {
            [Region.Africa] = ("Africa", "África"),
            [Region.Americas] = ("Americas", "América"),
            [Region.Antartida] = ("Antarctic", "Antártida"),
            [Region.Asia] = ("Asia", "Asia"),
            [Region.Europa] = ("Europe", "Europa"),
            [Region.Oceania] = ("Oceania", "Oceanía")
        };

    public static IReadOnlyList<Region> Todas { get; } = Enum.GetValues<Region>();

    /// <summary>Nombre en español para mostrar.</summary>
    public static string Nombre(this Region region) => Datos[region].Nombre;

    /// <summary>Valor que se guarda en la columna region (AFRICA, AMERICAS…).</summary>
    public static string ValorBase(this Region region) => region.ToString().ToUpperInvariant();

    /// <summary>Traduce el valor de la API; null si la API envía una región desconocida.</summary>
    public static Region? DesdeApi(string? valor)
    {
        if (string.IsNullOrWhiteSpace(valor))
        {
            return null;
        }
        foreach ((Region region, (string api, _)) in Datos)
        {
            if (string.Equals(api, valor.Trim(), StringComparison.OrdinalIgnoreCase))
            {
                return region;
            }
        }
        return null;
    }

    /// <summary>Lee el valor guardado en la base de datos.</summary>
    public static Region DesdeBase(string valor)
    {
        if (Enum.TryParse(valor, ignoreCase: true, out Region region) && Enum.IsDefined(region))
        {
            return region;
        }
        throw new InvalidOperationException("Región no válida en la base de datos: " + valor);
    }

    /// <summary>
    /// Interpreta lo que escribe o elige el usuario: acepta el nombre en español (con o sin tildes),
    /// el valor de la API o el nombre de la constante.
    /// </summary>
    /// <exception cref="ExcepcionValidacion">si el texto no corresponde a ninguna región</exception>
    public static Region DesdeTexto(string? texto)
    {
        if (string.IsNullOrWhiteSpace(texto))
        {
            throw new ExcepcionValidacion("La región es obligatoria.");
        }
        string buscado = Normalizar(texto);
        foreach ((Region region, (string api, string nombre)) in Datos)
        {
            if (Normalizar(nombre) == buscado || Normalizar(api) == buscado || Normalizar(region.ToString()) == buscado)
            {
                return region;
            }
        }
        throw new ExcepcionValidacion("Región desconocida: " + texto.Trim());
    }

    /// <summary>Quita tildes y pasa a mayúsculas para comparar sin importar cómo se escribió.</summary>
    internal static string Normalizar(string texto)
    {
        string descompuesto = texto.Trim().Normalize(NormalizationForm.FormD);
        StringBuilder limpio = new(descompuesto.Length);
        foreach (char caracter in descompuesto)
        {
            if (CharUnicodeInfo.GetUnicodeCategory(caracter) != UnicodeCategory.NonSpacingMark)
            {
                limpio.Append(caracter);
            }
        }
        return limpio.ToString().ToUpperInvariant();
    }
}
