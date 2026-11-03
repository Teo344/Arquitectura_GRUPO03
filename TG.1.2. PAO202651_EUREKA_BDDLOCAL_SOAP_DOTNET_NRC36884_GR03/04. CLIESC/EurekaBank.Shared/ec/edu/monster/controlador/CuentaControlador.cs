using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador
{
    /// <summary>Consulta de cuenta (RQF-003) y de movimientos (RQF-004).</summary>
    public sealed class CuentaControlador : ControladorBase
    {
        private static readonly Regex FormatoCuenta = new(@"^\d{8}$");

        private readonly CuentaVista vista;
        private readonly CuentaSoapService servicio;
        private readonly SesionModelo sesion;

        public CuentaControlador(CuentaVista vista, CuentaSoapService servicio, SesionModelo sesion)
        {
            this.vista = vista;
            this.servicio = servicio;
            this.sesion = sesion;
        }

        public async Task ConsultarCuentaAsync(string? cuenta)
        {
            string codigo;
            try
            {
                codigo = ValidarCuenta(cuenta, "La cuenta");
            }
            catch (ArgumentException ex)
            {
                vista.MostrarMensaje(ex.Message, true);
                return;
            }
            vista.SetProcesando(true);
            try
            {
                CuentaModelo resultado = await servicio.ConsultarCuentaAsync(sesion.Token, codigo);
                if (Activo)
                {
                    vista.SetProcesando(false);
                    vista.MostrarCuenta(resultado);
                }
            }
            catch (Exception ex)
            {
                MostrarError(ex);
            }
        }

        public async Task ConsultarMovimientosAsync(string? cuenta)
        {
            string codigo;
            try
            {
                codigo = ValidarCuenta(cuenta, "La cuenta");
            }
            catch (ArgumentException ex)
            {
                vista.MostrarMensaje(ex.Message, true);
                return;
            }
            vista.SetProcesando(true);
            try
            {
                IReadOnlyList<MovimientoModelo> movimientos = await servicio.ConsultarMovimientosAsync(sesion.Token, codigo);
                if (Activo)
                {
                    vista.SetProcesando(false);
                    vista.MostrarMovimientos(codigo, movimientos);
                }
            }
            catch (Exception ex)
            {
                MostrarError(ex);
            }
        }

        /// <summary>
        /// Mismo formato que exige el servidor (DATA-001): 8 dígitos. Devuelve el código sin espacios.
        /// </summary>
        internal static string ValidarCuenta(string? cuenta, string nombre)
        {
            string codigo = cuenta?.Trim() ?? "";
            if (!FormatoCuenta.IsMatch(codigo))
            {
                throw new ArgumentException(nombre + " debe tener exactamente 8 dígitos (por ejemplo 00100001).");
            }
            return codigo;
        }

        private void MostrarError(Exception error)
        {
            if (!Activo)
            {
                return;
            }
            vista.SetProcesando(false);
            if (RequiereNuevoLogin(error))
            {
                vista.VolverAlLogin(MensajeDe(error));
                return;
            }
            vista.MostrarMensaje(MensajeDe(error), true);
        }
    }
}
