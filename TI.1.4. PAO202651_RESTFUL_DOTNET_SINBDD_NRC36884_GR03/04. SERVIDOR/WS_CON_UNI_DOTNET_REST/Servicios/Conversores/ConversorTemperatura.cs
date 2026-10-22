using System;
using WS_CON_UNI_DOTNET_REST.Modelo;

namespace WS_CON_UNI_DOTNET_REST.Servicios.Conversores
{
    /// <summary>Conversión de temperatura con fórmulas, pasando por Celsius (RQF-006).</summary>
    public class ConversorTemperatura : IConversor
    {
        public bool Soporta(Categoria categoria) => categoria == Categoria.Temperatura;

        public double Convertir(double valor, Unidad origen, Unidad destino)
        {
            return DesdeCelsius(ACelsius(valor, origen), destino);
        }

        private static double ACelsius(double v, Unidad unidad)
        {
            switch (unidad.Codigo)
            {
                case "C": return v;
                case "K": return v - 273.15;
                case "F": return (v - 32) * 5 / 9;
                case "N": return v * 100 / 33;
                case "RE": return v * 5 / 4;
                default: throw new ArgumentException("No es unidad de temperatura: " + unidad.Codigo);
            }
        }

        private static double DesdeCelsius(double c, Unidad unidad)
        {
            switch (unidad.Codigo)
            {
                case "C": return c;
                case "K": return c + 273.15;
                case "F": return c * 9 / 5 + 32;
                case "N": return c * 33 / 100;
                case "RE": return c * 4 / 5;
                default: throw new ArgumentException("No es unidad de temperatura: " + unidad.Codigo);
            }
        }
    }
}
