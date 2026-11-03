using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    /// <summary>Consulta de cuenta (RQF-003) y de movimientos (RQF-004).</summary>
    public class CuentaControlador
    {
        private static readonly Regex FormatoCuenta = new Regex("^[0-9]{8}$");

        private readonly ClienteWSEureka clienteWs;

        public CuentaControlador(ClienteWSEureka clienteWs)
        {
            this.clienteWs = clienteWs;
        }

        public CuentaModelo ConsultarCuenta(string cuenta)
        {
            return clienteWs.ConsultarCuenta(ValidarCuenta(cuenta, "La cuenta"));
        }

        public List<MovimientoModelo> ConsultarMovimientos(string cuenta)
        {
            return clienteWs.ConsultarMovimientos(ValidarCuenta(cuenta, "La cuenta"));
        }

        /// <summary>Mismo formato que exige el servidor (DATA-001): 8 dígitos. Devuelve el código sin espacios.</summary>
        internal static string ValidarCuenta(string cuenta, string nombre)
        {
            string codigo = cuenta == null ? "" : cuenta.Trim();
            if (!FormatoCuenta.IsMatch(codigo))
                throw new ArgumentException(nombre + " debe tener exactamente 8 digitos (por ejemplo 00100001).");
            return codigo;
        }
    }
}
