using System;
using System.Globalization;

namespace ec.edu.monster.vista
{
    /// <summary>Formato de datos para mostrar, igual que en el cliente móvil.</summary>
    public static class Formato
    {
        /// <summary>Importe con separador de miles y dos decimales: 6,900.00</summary>
        public static string Importe(decimal? importe)
        {
            return importe == null ? "-" : decimal.Round(importe.Value, 2).ToString("#,##0.00", CultureInfo.InvariantCulture);
        }

        /// <summary>Hora local (HH:mm) de una fecha ISO-8601, por ejemplo 2026-11-02T20:03:25.938Z.</summary>
        public static string HoraLocal(string? iso)
        {
            if (DateTimeOffset.TryParse(iso, CultureInfo.InvariantCulture, DateTimeStyles.AssumeUniversal, out DateTimeOffset fecha))
            {
                return fecha.ToLocalTime().ToString("HH:mm", CultureInfo.InvariantCulture);
            }
            return "-";
        }

        public static string ValorO(string? valor)
        {
            return string.IsNullOrWhiteSpace(valor) ? "-" : valor.Trim();
        }
    }
}
