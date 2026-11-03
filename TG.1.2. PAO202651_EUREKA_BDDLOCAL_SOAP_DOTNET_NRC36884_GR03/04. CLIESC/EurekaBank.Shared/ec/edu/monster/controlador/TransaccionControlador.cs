using System;
using System.Globalization;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Depósitos (RQF-005), retiros (RQF-006) y transferencias (RQF-007). Valida los datos antes de
    /// enviarlos para orientar al empleado; el servidor vuelve a validar todas las reglas de negocio.
    /// </summary>
    public sealed class TransaccionControlador : ControladorBase
    {
        // Mayor valor que admite DECIMAL(12,2) en el servidor (RN-01)
        private const decimal ImporteMaximo = 9999999999.99m;

        private readonly TransaccionVista vista;
        private readonly TransaccionSoapService servicio;
        private readonly SesionModelo sesion;

        public TransaccionControlador(TransaccionVista vista, TransaccionSoapService servicio, SesionModelo sesion)
        {
            this.vista = vista;
            this.servicio = servicio;
            this.sesion = sesion;
        }

        /// <summary>
        /// Valida los datos escritos y, si son correctos, pide a la vista la confirmación del
        /// empleado antes de registrar nada.
        /// </summary>
        public void Preparar(TipoTransaccion tipo, string? cuenta, string? cuentaDestino, string? textoImporte)
        {
            try
            {
                TransaccionModelo modelo = new(tipo, cuenta,
                    tipo == TipoTransaccion.Transferencia ? cuentaDestino : null, LeerImporte(textoImporte));
                Validar(modelo);
                vista.PedirConfirmacion(modelo);
            }
            catch (ArgumentException ex)
            {
                vista.MostrarMensaje(ex.Message, true);
            }
        }

        public async Task EjecutarAsync(TransaccionModelo modelo)
        {
            try
            {
                Validar(modelo);
            }
            catch (ArgumentException ex)
            {
                vista.MostrarMensaje(ex.Message, true);
                return;
            }
            string cuenta = modelo.Cuenta!.Trim();
            decimal importe = modelo.Importe!.Value;
            vista.SetProcesando(true);
            try
            {
                ResultadoTransaccionModelo resultado = modelo.Tipo switch
                {
                    TipoTransaccion.Deposito => await servicio.DepositarAsync(sesion.Token, cuenta, importe),
                    TipoTransaccion.Retiro => await servicio.RetirarAsync(sesion.Token, cuenta, importe),
                    _ => await servicio.TransferirAsync(sesion.Token, cuenta, modelo.CuentaDestino!.Trim(), importe)
                };
                if (Activo)
                {
                    vista.SetProcesando(false);
                    vista.MostrarResultado(modelo, resultado);
                }
            }
            catch (Exception ex)
            {
                MostrarError(ex);
            }
        }

        /// <summary>RN-01 y RN-04, con los mismos mensajes que los demás clientes.</summary>
        private static void Validar(TransaccionModelo modelo)
        {
            if (modelo.Tipo == TipoTransaccion.Transferencia)
            {
                string origen = CuentaControlador.ValidarCuenta(modelo.Cuenta, "La cuenta de origen");
                string destino = CuentaControlador.ValidarCuenta(modelo.CuentaDestino, "La cuenta de destino");
                if (origen == destino)
                {
                    throw new ArgumentException("La cuenta de destino debe ser diferente de la cuenta de origen.");
                }
            }
            else
            {
                CuentaControlador.ValidarCuenta(modelo.Cuenta, "La cuenta");
            }
            decimal? importe = modelo.Importe;
            if (importe == null || importe <= 0 || decimal.Round(importe.Value, 2) != importe.Value)
            {
                throw new ArgumentException("El importe debe ser mayor que cero y tener como máximo dos decimales.");
            }
            if (importe > ImporteMaximo)
            {
                throw new ArgumentException("El importe no puede superar 9999999999.99.");
            }
        }

        /// <summary>Acepta coma o punto decimal; el dinero se maneja siempre con decimal.</summary>
        private static decimal LeerImporte(string? texto)
        {
            string valor = texto?.Trim().Replace(',', '.') ?? "";
            if (valor.Length == 0)
            {
                throw new ArgumentException("Ingresa el importe de la operación.");
            }
            if (!decimal.TryParse(valor, NumberStyles.AllowLeadingSign | NumberStyles.AllowDecimalPoint,
                    CultureInfo.InvariantCulture, out decimal importe))
            {
                throw new ArgumentException("Ingresa un importe numérico válido, por ejemplo 150.50");
            }
            return importe;
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
