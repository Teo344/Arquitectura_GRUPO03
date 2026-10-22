using System.Collections.Generic;
using System.Linq;

namespace WS_CON_UNI_DOTNET_REST.Modelo
{
    /// <summary>
    /// Categorías del catálogo (RQF-002). Solo las convertibles tienen operación de conversión.
    /// </summary>
    public sealed class Categoria
    {
        public static readonly Categoria Longitud = new Categoria("LONGITUD", "Longitud", true);
        public static readonly Categoria Masa = new Categoria("MASA", "Peso (masa)", true);
        public static readonly Categoria Volumen = new Categoria("VOLUMEN", "Volumen", false);
        public static readonly Categoria Tiempo = new Categoria("TIEMPO", "Tiempo", false);
        public static readonly Categoria Temperatura = new Categoria("TEMPERATURA", "Temperatura", true);
        public static readonly Categoria Area = new Categoria("AREA", "Área", false);

        public static readonly IReadOnlyList<Categoria> Todas =
            new[] { Longitud, Masa, Volumen, Tiempo, Temperatura, Area };

        public string Codigo { get; }
        public string Nombre { get; }
        public bool Convertible { get; }

        private Categoria(string codigo, string nombre, bool convertible)
        {
            Codigo = codigo;
            Nombre = nombre;
            Convertible = convertible;
        }

        /// <summary>Busca la categoría por código, ignorando espacios y mayúsculas. Null si no existe.</summary>
        public static Categoria DesdeCodigo(string codigo)
        {
            if (string.IsNullOrWhiteSpace(codigo)) return null;
            string normalizado = codigo.Trim().ToUpperInvariant();
            return Todas.FirstOrDefault(c => c.Codigo == normalizado);
        }
    }
}
