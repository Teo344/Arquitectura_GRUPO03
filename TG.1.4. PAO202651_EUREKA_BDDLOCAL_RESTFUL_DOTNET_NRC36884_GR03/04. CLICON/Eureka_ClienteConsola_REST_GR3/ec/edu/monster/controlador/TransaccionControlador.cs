using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Depósitos (RQF-005), retiros (RQF-006) y transferencias (RQF-007). Valida los datos antes de
    /// enviarlos para orientar al empleado; el servidor vuelve a validar todas las reglas de negocio.
    /// </summary>
    public class TransaccionControlador
    {
        // Mayor valor que admite DECIMAL(12,2) en el servidor (RN-01)
        private const decimal ImporteMaximo = 9999999999.99m;

        private readonly ClienteRESTEureka clienteRest;

        public TransaccionControlador(ClienteRESTEureka clienteRest)
        {
            this.clienteRest = clienteRest;
        }

        /// <summary>Permite a la vista avisar de un dato inválido antes de pedir la confirmación.</summary>
        public void Validar(TransaccionModelo modelo)
        {
            if (modelo == null)
                throw new ArgumentException("Debe seleccionar el tipo de transaccion.");
            if (modelo.Tipo == TipoTransaccion.Transferencia)
            {
                string origen = CuentaControlador.ValidarCuenta(modelo.Cuenta, "La cuenta de origen");
                string destino = CuentaControlador.ValidarCuenta(modelo.CuentaDestino, "La cuenta de destino");
                if (origen == destino)
                    throw new ArgumentException("La cuenta de destino debe ser diferente de la cuenta de origen.");
            }
            else
            {
                CuentaControlador.ValidarCuenta(modelo.Cuenta, "La cuenta");
            }
            ValidarImporte(modelo.Importe);
        }

        public Task<ResultadoTransaccionModelo> EjecutarAsync(TransaccionModelo modelo)
        {
            Validar(modelo);
            string cuenta = modelo.Cuenta.Trim();
            switch (modelo.Tipo)
            {
                case TipoTransaccion.Deposito: return clienteRest.DepositarAsync(cuenta, modelo.Importe);
                case TipoTransaccion.Retiro: return clienteRest.RetirarAsync(cuenta, modelo.Importe);
                default: return clienteRest.TransferirAsync(cuenta, modelo.CuentaDestino!.Trim(), modelo.Importe);
            }
        }

        // RN-01: mayor que cero, máximo dos decimales y dentro de DECIMAL(12,2)
        private static void ValidarImporte(decimal importe)
        {
            if (importe <= 0 || decimal.Round(importe, 2) != importe)
                throw new ArgumentException("El importe debe ser mayor que cero y tener como maximo dos decimales.");
            if (importe > ImporteMaximo)
                throw new ArgumentException("El importe no puede superar 9999999999.99.");
        }
    }
}
