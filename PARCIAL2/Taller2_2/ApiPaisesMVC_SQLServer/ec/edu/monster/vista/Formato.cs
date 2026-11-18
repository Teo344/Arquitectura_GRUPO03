using System.Globalization;

namespace ec.edu.monster.vista
{
    /// <summary>
    /// Formato de números y textos para mostrar (es-EC: 18.001.000). Igual que Formato.java del
    /// Taller 2.1; los datos se guardan sin formato.
    /// </summary>
    public static class Formato
    {
        private static readonly CultureInfo Ecuador = CultureInfo.GetCultureInfo("es-EC");
        private const string SinDato = "—";

        public static string Entero(long valor)
        {
            return valor.ToString("N0", Ecuador);
        }

        public static string Decimal(double valor)
        {
            return valor.ToString(valor % 1 == 0 ? "N0" : "N1", Ecuador);
        }

        public static string Area(double km2)
        {
            return km2 <= 0 ? SinDato : Decimal(km2) + " km²";
        }

        public static string Porcentaje(double fraccion)
        {
            return (fraccion * 100).ToString("N1", Ecuador) + " %";
        }

        public static string Texto(object? valor)
        {
            string? texto = valor?.ToString();
            return string.IsNullOrWhiteSpace(texto) ? SinDato : texto;
        }

        public static string Paises(long cantidad)
        {
            return Entero(cantidad) + (cantidad == 1 ? " país" : " países");
        }

        public static string Universidades(long cantidad)
        {
            return Entero(cantidad) + (cantidad == 1 ? " universidad" : " universidades");
        }
    }
}
