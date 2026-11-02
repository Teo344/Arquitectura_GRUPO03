package ec.edu.monster.vista;

import ec.edu.monster.controlador.CuentaControlador;
import ec.edu.monster.modelo.CuentaModelo;
import ec.edu.monster.modelo.MovimientoModelo;
import ec.edu.monster.util.EntradaUtil;
import java.util.List;

/** Consulta de cuenta (RQF-003) y de movimientos (RQF-004). */
public class CuentaVista {

    private static final String FORMATO_FILA = "%4s  %-10s  %-22s  %-8s  %14s  %-9s";

    private final MensajesVista mensajes = new MensajesVista();
    private final CuentaControlador cuentaControlador;

    public CuentaVista(CuentaControlador cuentaControlador) {
        this.cuentaControlador = cuentaControlador;
    }

    public void consultarCuenta() {
        mensajes.mostrarTitulo("CONSULTAR CUENTA");
        String codigo = EntradaUtil.leerTextoNoVacio("Numero de cuenta (8 digitos): ");

        CuentaModelo cuenta = cuentaControlador.consultarCuenta(codigo);
        mensajes.mostrarRecuadro(List.of(
                "Cuenta           : " + cuenta.getCodigo(),
                "Titular          : " + cuenta.getTitular(),
                "Moneda           : " + cuenta.getMoneda(),
                "Saldo            : " + MensajesVista.formatoImporte(cuenta.getSaldo()),
                "Estado           : " + cuenta.getEstado(),
                "Sucursal         : " + cuenta.getSucursal(),
                "Fecha de creacion: " + cuenta.getFechaCreacion()));
    }

    public void consultarMovimientos() {
        mensajes.mostrarTitulo("CONSULTAR MOVIMIENTOS");
        String codigo = EntradaUtil.leerTextoNoVacio("Numero de cuenta (8 digitos): ");

        List<MovimientoModelo> movimientos = cuentaControlador.consultarMovimientos(codigo);
        if (movimientos.isEmpty()) {
            mensajes.mostrarMensaje("La cuenta no registra movimientos.");
            return;
        }

        String encabezado = String.format(FORMATO_FILA, "N.", "Fecha", "Tipo", "Accion", "Importe", "Cta. ref.");
        mensajes.mostrarMensaje(encabezado);
        mensajes.mostrarMensaje("-".repeat(encabezado.length()));
        for (MovimientoModelo movimiento : movimientos) {
            mensajes.mostrarMensaje(String.format(FORMATO_FILA,
                    movimiento.getNumero(),
                    movimiento.getFecha(),
                    movimiento.getTipo(),
                    movimiento.getAccion(),
                    MensajesVista.formatoImporte(movimiento.getImporte()),
                    movimiento.getCuentaReferencia() == null ? "" : movimiento.getCuentaReferencia()));
        }
        mensajes.mostrarMensaje("-".repeat(encabezado.length()));
        mensajes.mostrarMensaje("Total de movimientos: " + movimientos.size());
    }
}
