using System.Globalization;
using System.Text;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.modelo.entidad
{
    /// <summary>
    /// Regiones que maneja REST Countries. En la base de datos se guarda el nombre de la constante
    /// (p. ej. AMERICAS), que no cambia aunque cambie la traducción.
    /// </summary>
    public enum Region
    {
        AFRICA,
        AMERICAS,
        ANTARTIDA,
        ASIA,
        EUROPA,
        OCEANIA
    }

    /// <summary>
    /// Valor en inglés que envía la API y nombre en español que ve el usuario de cada región.
    /// </summary>
    public static class RegionExtensiones
    {
        public static string ValorApi(this Region region) => region switch
        {
            Region.AFRICA => "Africa",
            Region.AMERICAS => "Americas",
            Region.ANTARTIDA => "Antarctic",
            Region.ASIA => "Asia",
            Region.EUROPA => "Europe",
            Region.OCEANIA => "Oceania",
            _ => region.ToString()
        };

        public static string Nombre(this Region region) => region switch
        {
            Region.AFRICA => "África",
            Region.AMERICAS => "América",
            Region.ANTARTIDA => "Antártida",
            Region.ASIA => "Asia",
            Region.EUROPA => "Europa",
            Region.OCEANIA => "Oceanía",
            _ => region.ToString()
        };

        public static IReadOnlyList<Region> Todas { get; } = Enum.GetValues<Region>();

        /// <summary>
        /// Traduce el valor de la API. Devuelve null si la API envía una región desconocida, para que
        /// quien llama decida si descarta el registro.
        /// </summary>
        public static Region? DesdeApi(string? valor)
        {
            if (valor == null)
            {
                return null;
            }
            string buscado = valor.Trim();
            foreach (Region region in Todas)
            {
                if (string.Equals(region.ValorApi(), buscado, StringComparison.OrdinalIgnoreCase))
                {
                    return region;
                }
            }
            return null;
        }

        /// <summary>
        /// Interpreta lo que escribe o elige el usuario: acepta el nombre en español (con o sin
        /// tildes), el valor de la API o el nombre de la constante.
        /// </summary>
        /// <exception cref="ExcepcionValidacion">si el texto no corresponde a ninguna región</exception>
        public static Region DesdeTexto(string? texto)
        {
            if (string.IsNullOrWhiteSpace(texto))
            {
                throw new ExcepcionValidacion("La región es obligatoria.");
            }
            string buscado = Normalizar(texto);
            foreach (Region region in Todas)
            {
                if (Normalizar(region.Nombre()) == buscado || Normalizar(region.ValorApi()) == buscado
                    || Normalizar(region.ToString()) == buscado)
                {
                    return region;
                }
            }
            throw new ExcepcionValidacion("Región desconocida: " + texto.Trim());
        }

        /// <summary>Quita tildes y pasa a mayúsculas: "Oceanía" y "OCEANIA" son lo mismo.</summary>
        public static string Normalizar(string texto)
        {
            string descompuesto = texto.Trim().Normalize(NormalizationForm.FormD);
            StringBuilder sinTildes = new StringBuilder(descompuesto.Length);
            foreach (char caracter in descompuesto)
            {
                if (CharUnicodeInfo.GetUnicodeCategory(caracter) != UnicodeCategory.NonSpacingMark)
                {
                    sinTildes.Append(caracter);
                }
            }
            return sinTildes.ToString().ToUpperInvariant();
        }
    }
}
