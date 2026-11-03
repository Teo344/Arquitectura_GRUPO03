using System.Text.RegularExpressions;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    /// <summary>Consulta de cuenta (RQF-003) y de movimientos (RQF-004).</summary>
    public class CuentaControlador
    {
        private static readonly Regex FormatoCuenta = new Regex("^[0-9]{8}$");

        private readonly ClienteRESTEureka clienteRest;

        public CuentaControlador(ClienteRESTEureka clienteRest)
        {
            this.clienteRest = clienteRest;
        }

        public Task<CuentaModelo> ConsultarCuentaAsync(string cuenta)
        {
            return clienteRest.ConsultarCuentaAsync(ValidarCuenta(cuenta, "La cuenta"));
        }

        public Task<List<MovimientoModelo>> ConsultarMovimientosAsync(string cuenta)
        {
            return clienteRest.ConsultarMovimientosAsync(ValidarCuenta(cuenta, "La cuenta"));
        }

        /// <summary>Mismo formato que exige el servidor (DATA-001): 8 dígitos. Devuelve el código sin espacios.</summary>
        internal static string ValidarCuenta(string? cuenta, string nombre)
        {
            string codigo = cuenta == null ? "" : cuenta.Trim();
            if (!FormatoCuenta.IsMatch(codigo))
                throw new ArgumentException(nombre + " debe tener exactamente 8 digitos (por ejemplo 00100001).");
            return codigo;
        }
    }
}
