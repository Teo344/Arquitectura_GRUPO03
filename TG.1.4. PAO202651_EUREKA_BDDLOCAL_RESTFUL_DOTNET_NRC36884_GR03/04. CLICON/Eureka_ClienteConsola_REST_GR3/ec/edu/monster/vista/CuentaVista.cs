using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.util;

namespace ec.edu.monster.vista
{
    /// <summary>Consulta de cuenta (RQF-003) y de movimientos (RQF-004).</summary>
    public class CuentaVista
    {
        private const string FormatoFila = "{0,4}  {1,-10}  {2,-22}  {3,-8}  {4,14}  {5,-9}";

        private readonly MensajesVista mensajes = new MensajesVista();
        private readonly CuentaControlador cuentaControlador;

        public CuentaVista(CuentaControlador cuentaControlador)
        {
            this.cuentaControlador = cuentaControlador;
        }

        public async Task ConsultarCuentaAsync()
        {
            mensajes.MostrarTitulo("CONSULTAR CUENTA");
            string codigo = EntradaUtil.LeerTextoNoVacio("Numero de cuenta (8 digitos): ");

            CuentaModelo cuenta = await cuentaControlador.ConsultarCuentaAsync(codigo);
            mensajes.MostrarRecuadro(new[]
            {
                "Cuenta           : " + cuenta.Codigo,
                "Titular          : " + cuenta.Titular,
                "Moneda           : " + cuenta.Moneda,
                "Saldo            : " + MensajesVista.FormatoImporte(cuenta.Saldo),
                "Estado           : " + cuenta.Estado,
                "Sucursal         : " + cuenta.Sucursal,
                "Fecha de creacion: " + cuenta.FechaCreacion
            });
        }

        public async Task ConsultarMovimientosAsync()
        {
            mensajes.MostrarTitulo("CONSULTAR MOVIMIENTOS");
            string codigo = EntradaUtil.LeerTextoNoVacio("Numero de cuenta (8 digitos): ");

            List<MovimientoModelo> movimientos = await cuentaControlador.ConsultarMovimientosAsync(codigo);
            if (movimientos.Count == 0)
            {
                mensajes.MostrarMensaje("La cuenta no registra movimientos.");
                return;
            }

            string encabezado = string.Format(FormatoFila, "N.", "Fecha", "Tipo", "Accion", "Importe", "Cta. ref.");
            string separador = new string('-', encabezado.Length);
            mensajes.MostrarMensaje(encabezado);
            mensajes.MostrarMensaje(separador);
            foreach (MovimientoModelo movimiento in movimientos)
            {
                mensajes.MostrarMensaje(string.Format(FormatoFila,
                    movimiento.Numero,
                    movimiento.Fecha,
                    movimiento.Tipo,
                    movimiento.Accion,
                    MensajesVista.FormatoImporte(movimiento.Importe),
                    movimiento.CuentaReferencia ?? ""));
            }
            mensajes.MostrarMensaje(separador);
            mensajes.MostrarMensaje("Total de movimientos: " + movimientos.Count);
        }
    }
}
