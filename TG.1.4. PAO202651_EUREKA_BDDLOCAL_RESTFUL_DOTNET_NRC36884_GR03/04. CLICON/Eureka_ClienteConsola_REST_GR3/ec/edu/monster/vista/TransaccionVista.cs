using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.util;

namespace ec.edu.monster.vista
{
    /// <summary>Formularios de depósito (RQF-005), retiro (RQF-006) y transferencia (RQF-007).</summary>
    public class TransaccionVista
    {
        private readonly MensajesVista mensajes = new MensajesVista();
        private readonly TransaccionControlador transaccionControlador;

        public TransaccionVista(TransaccionControlador transaccionControlador)
        {
            this.transaccionControlador = transaccionControlador;
        }

        public async Task ProcesarAsync(TipoTransaccion tipo)
        {
            mensajes.MostrarTitulo("REGISTRAR " + tipo.ToString().ToUpperInvariant());

            TransaccionModelo modelo = tipo == TipoTransaccion.Transferencia ? LeerTransferencia() : LeerOperacion(tipo);
            transaccionControlador.Validar(modelo);

            mensajes.MostrarRecuadro(Resumen(modelo));
            if (!EntradaUtil.LeerConfirmacion("Confirma la operacion?"))
            {
                mensajes.MostrarMensaje("Operacion cancelada. No se registro ningun movimiento.");
                return;
            }

            ResultadoTransaccionModelo resultado = await transaccionControlador.EjecutarAsync(modelo);
            mensajes.MostrarExito(resultado.Mensaje ?? "Operacion registrada.");
            var lineas = new List<string>();
            if (tipo == TipoTransaccion.Transferencia)
            {
                lineas.Add("Nuevo saldo de la cuenta de origen : " + MensajesVista.FormatoImporte(resultado.NuevoSaldo));
                lineas.Add("Movimiento en la cuenta de origen  : No. " + resultado.NumeroMovimiento);
                lineas.Add("Movimiento en la cuenta de destino : No. " + resultado.NumeroMovimientoDestino);
            }
            else
            {
                lineas.Add("Nuevo saldo : " + MensajesVista.FormatoImporte(resultado.NuevoSaldo));
                lineas.Add("Movimiento  : No. " + resultado.NumeroMovimiento);
            }
            mensajes.MostrarRecuadro(lineas);
        }

        private TransaccionModelo LeerOperacion(TipoTransaccion tipo)
        {
            string cuenta = EntradaUtil.LeerTextoNoVacio("Numero de cuenta (8 digitos): ");
            string verbo = tipo == TipoTransaccion.Deposito ? "depositar" : "retirar";
            decimal importe = EntradaUtil.LeerImporte("Importe a " + verbo + ": ");
            return new TransaccionModelo(tipo, cuenta, importe);
        }

        private TransaccionModelo LeerTransferencia()
        {
            string origen = EntradaUtil.LeerTextoNoVacio("Cuenta de origen (8 digitos) : ");
            string destino = EntradaUtil.LeerTextoNoVacio("Cuenta de destino (8 digitos): ");
            decimal importe = EntradaUtil.LeerImporte("Importe a transferir: ");
            return new TransaccionModelo(TipoTransaccion.Transferencia, origen, destino, importe);
        }

        private static List<string> Resumen(TransaccionModelo modelo)
        {
            var lineas = new List<string> { "Operacion : " + modelo.Tipo };
            if (modelo.Tipo == TipoTransaccion.Transferencia)
            {
                lineas.Add("Origen    : " + modelo.Cuenta.Trim());
                lineas.Add("Destino   : " + modelo.CuentaDestino?.Trim());
            }
            else
            {
                lineas.Add("Cuenta    : " + modelo.Cuenta.Trim());
            }
            lineas.Add("Importe   : " + MensajesVista.FormatoImporte(modelo.Importe));
            return lineas;
        }
    }
}
