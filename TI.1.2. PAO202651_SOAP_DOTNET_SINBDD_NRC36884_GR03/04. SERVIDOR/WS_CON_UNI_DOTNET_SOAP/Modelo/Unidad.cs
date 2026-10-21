using System.Collections.Generic;
using System.Linq;

namespace WS_CON_UNI_DOTNET_SOAP.Modelo
{
    /// <summary>
    /// Unidades de todas las categorías (RQF-003). El factor lleva la unidad a la base de su
    /// categoría; en temperatura es null porque se usan fórmulas. Los códigos son únicos.
    /// </summary>
    public sealed class Unidad
    {
        // Longitud (base: metro)
        public static readonly Unidad M = new Unidad("M", "Metro", "m", Categoria.Longitud, 1.0);
        public static readonly Unidad CM = new Unidad("CM", "Centímetro", "cm", Categoria.Longitud, 0.01);
        public static readonly Unidad KM = new Unidad("KM", "Kilómetro", "km", Categoria.Longitud, 1000.0);
        public static readonly Unidad FT = new Unidad("FT", "Pie", "ft", Categoria.Longitud, 0.3048);
        public static readonly Unidad YD = new Unidad("YD", "Yarda", "yd", Categoria.Longitud, 0.9144);
        public static readonly Unidad MI = new Unidad("MI", "Milla", "mi", Categoria.Longitud, 1609.344);

        // Masa (base: kilogramo)
        public static readonly Unidad G = new Unidad("G", "Gramo", "g", Categoria.Masa, 0.001);
        public static readonly Unidad KG = new Unidad("KG", "Kilogramo", "kg", Categoria.Masa, 1.0);
        public static readonly Unidad OZ = new Unidad("OZ", "Onza", "oz", Categoria.Masa, 0.028349523125);
        public static readonly Unidad LB = new Unidad("LB", "Libra", "lb", Categoria.Masa, 0.45359237);
        public static readonly Unidad QQ = new Unidad("QQ", "Quintal", "qq", Categoria.Masa, 45.359237);

        // Temperatura (vía Celsius, sin factor)
        public static readonly Unidad C = new Unidad("C", "Celsius", "°C", Categoria.Temperatura, null);
        public static readonly Unidad K = new Unidad("K", "Kelvin", "K", Categoria.Temperatura, null);
        public static readonly Unidad F = new Unidad("F", "Fahrenheit", "°F", Categoria.Temperatura, null);
        public static readonly Unidad N = new Unidad("N", "Newton", "°N", Categoria.Temperatura, null);
        public static readonly Unidad RE = new Unidad("RE", "Réaumur", "°Ré", Categoria.Temperatura, null);

        // Volumen (base: litro), solo catálogo
        public static readonly Unidad L = new Unidad("L", "Litro", "L", Categoria.Volumen, 1.0);
        public static readonly Unidad ML = new Unidad("ML", "Mililitro", "mL", Categoria.Volumen, 0.001);
        public static readonly Unidad M3 = new Unidad("M3", "Metro cúbico", "m³", Categoria.Volumen, 1000.0);
        public static readonly Unidad GAL = new Unidad("GAL", "Galón (US)", "gal", Categoria.Volumen, 3.785411784);
        public static readonly Unidad FT3 = new Unidad("FT3", "Pie cúbico", "ft³", Categoria.Volumen, 28.316846592);

        // Tiempo (base: segundo), solo catálogo
        public static readonly Unidad S = new Unidad("S", "Segundo", "s", Categoria.Tiempo, 1.0);
        public static readonly Unidad MIN = new Unidad("MIN", "Minuto", "min", Categoria.Tiempo, 60.0);
        public static readonly Unidad H = new Unidad("H", "Hora", "h", Categoria.Tiempo, 3600.0);
        public static readonly Unidad D = new Unidad("D", "Día", "d", Categoria.Tiempo, 86400.0);
        public static readonly Unidad SEM = new Unidad("SEM", "Semana", "sem", Categoria.Tiempo, 604800.0);

        // Área (base: metro cuadrado), solo catálogo
        public static readonly Unidad M2 = new Unidad("M2", "Metro cuadrado", "m²", Categoria.Area, 1.0);
        public static readonly Unidad CM2 = new Unidad("CM2", "Centímetro cuadrado", "cm²", Categoria.Area, 0.0001);
        public static readonly Unidad KM2 = new Unidad("KM2", "Kilómetro cuadrado", "km²", Categoria.Area, 1000000.0);
        public static readonly Unidad HA = new Unidad("HA", "Hectárea", "ha", Categoria.Area, 10000.0);
        public static readonly Unidad FT2 = new Unidad("FT2", "Pie cuadrado", "ft²", Categoria.Area, 0.09290304);

        public static readonly IReadOnlyList<Unidad> Todas = new[]
        {
            M, CM, KM, FT, YD, MI, G, KG, OZ, LB, QQ, C, K, F, N, RE,
            L, ML, M3, GAL, FT3, S, MIN, H, D, SEM, M2, CM2, KM2, HA, FT2
        };

        public string Codigo { get; }
        public string Nombre { get; }
        public string Simbolo { get; }
        public Categoria Categoria { get; }
        public double? Factor { get; }

        private Unidad(string codigo, string nombre, string simbolo, Categoria categoria, double? factor)
        {
            Codigo = codigo;
            Nombre = nombre;
            Simbolo = simbolo;
            Categoria = categoria;
            Factor = factor;
        }

        /// <summary>Busca la unidad por código, ignorando espacios y mayúsculas. Null si no existe.</summary>
        public static Unidad DesdeCodigo(string codigo)
        {
            if (string.IsNullOrWhiteSpace(codigo)) return null;
            string normalizado = codigo.Trim().ToUpperInvariant();
            return Todas.FirstOrDefault(u => u.Codigo == normalizado);
        }

        public static IList<Unidad> DeCategoria(Categoria categoria)
        {
            return Todas.Where(u => u.Categoria == categoria).ToList();
        }
    }
}
