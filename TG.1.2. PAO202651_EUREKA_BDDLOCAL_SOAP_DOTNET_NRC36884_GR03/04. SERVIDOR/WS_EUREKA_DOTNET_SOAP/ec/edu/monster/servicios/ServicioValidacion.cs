using System.Text.RegularExpressions;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicios
{
    /// <summary>
    /// Valida el formato de las cuentas (DATA-001) y los importes (RN-01, MOV-001) antes de tocar
    /// la base de datos.
    /// </summary>
    public class ServicioValidacion
    {
        /// <summary>[0-9] y no \d: en .NET \d también acepta dígitos de otros alfabetos.</summary>
        private static readonly Regex FormatoCuenta = new Regex("^[0-9]{8}$");

        /// <summary>Mayor valor que admite la columna DECIMAL(12,2) de saldos e importes.</summary>
        public const decimal Maximo = 9999999999.99m;

        /// <summary>Devuelve el código sin espacios si tiene 8 dígitos.</summary>
        public string ValidarCuenta(string cuenta)
        {
            string codigo = cuenta == null ? "" : cuenta.Trim();
            if (!FormatoCuenta.IsMatch(codigo))
            {
                throw new ExcepcionServicio(CodigoError.Data001);
            }
            return codigo;
        }

        /// <summary>Importe mayor que cero, con máximo dos decimales y dentro de la columna.</summary>
        public decimal ValidarImporte(decimal? importe)
        {
            if (importe == null || importe.Value <= 0 || decimal.Round(importe.Value, 2) != importe.Value)
            {
                throw new ExcepcionServicio(CodigoError.Mov001);
            }
            ValidarMaximo(importe.Value);
            return importe.Value;
        }

        /// <summary>Un saldo que no cabe en DECIMAL(12,2) se rechaza como importe inválido.</summary>
        public void ValidarMaximo(decimal valor)
        {
            if (valor > Maximo)
            {
                throw new ExcepcionServicio(CodigoError.Mov001);
            }
        }
    }
}
