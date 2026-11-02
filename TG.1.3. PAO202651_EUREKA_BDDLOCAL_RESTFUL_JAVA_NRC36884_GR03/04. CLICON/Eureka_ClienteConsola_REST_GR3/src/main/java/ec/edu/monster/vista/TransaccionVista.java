package ec.edu.monster.vista;

import ec.edu.monster.controlador.TransaccionControlador;
import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.modelo.TransaccionModelo;
import ec.edu.monster.util.EntradaUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Formularios de depósito (RQF-005), retiro (RQF-006) y transferencia (RQF-007). */
public class TransaccionVista {

    private final MensajesVista mensajes = new MensajesVista();
    private final TransaccionControlador transaccionControlador;

    public TransaccionVista(TransaccionControlador transaccionControlador) {
        this.transaccionControlador = transaccionControlador;
    }

    public void procesar(TipoTransaccion tipo) {
        mensajes.mostrarTitulo("REGISTRAR " + tipo.getNombreVisible().toUpperCase());

        TransaccionModelo modelo = tipo == TipoTransaccion.TRANSFERENCIA ? leerTransferencia() : leerOperacion(tipo);
        transaccionControlador.validar(modelo);

        mensajes.mostrarRecuadro(resumen(modelo));
        if (!EntradaUtil.leerConfirmacion("Confirma la operacion?")) {
            mensajes.mostrarMensaje("Operacion cancelada. No se registro ningun movimiento.");
            return;
        }

        ResultadoTransaccionModelo resultado = transaccionControlador.ejecutar(modelo);
        mensajes.mostrarExito(resultado.getMensaje());
        List<String> lineas = new ArrayList<>();
        if (tipo == TipoTransaccion.TRANSFERENCIA) {
            lineas.add("Nuevo saldo de la cuenta de origen : " + MensajesVista.formatoImporte(resultado.getNuevoSaldo()));
            lineas.add("Movimiento en la cuenta de origen  : No. " + resultado.getNumeroMovimiento());
            lineas.add("Movimiento en la cuenta de destino : No. " + resultado.getNumeroMovimientoDestino());
        } else {
            lineas.add("Nuevo saldo : " + MensajesVista.formatoImporte(resultado.getNuevoSaldo()));
            lineas.add("Movimiento  : No. " + resultado.getNumeroMovimiento());
        }
        mensajes.mostrarRecuadro(lineas);
    }

    private TransaccionModelo leerOperacion(TipoTransaccion tipo) {
        String cuenta = EntradaUtil.leerTextoNoVacio("Numero de cuenta (8 digitos): ");
        String verbo = tipo == TipoTransaccion.DEPOSITO ? "depositar" : "retirar";
        BigDecimal importe = EntradaUtil.leerImporte("Importe a " + verbo + ": ");
        return new TransaccionModelo(tipo, cuenta, importe);
    }

    private TransaccionModelo leerTransferencia() {
        String origen = EntradaUtil.leerTextoNoVacio("Cuenta de origen (8 digitos) : ");
        String destino = EntradaUtil.leerTextoNoVacio("Cuenta de destino (8 digitos): ");
        BigDecimal importe = EntradaUtil.leerImporte("Importe a transferir: ");
        return new TransaccionModelo(TipoTransaccion.TRANSFERENCIA, origen, destino, importe);
    }

    private List<String> resumen(TransaccionModelo modelo) {
        List<String> lineas = new ArrayList<>();
        lineas.add("Operacion : " + modelo.getTipo().getNombreVisible());
        if (modelo.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            lineas.add("Origen    : " + modelo.getCuenta().trim());
            lineas.add("Destino   : " + modelo.getCuentaDestino().trim());
        } else {
            lineas.add("Cuenta    : " + modelo.getCuenta().trim());
        }
        lineas.add("Importe   : " + MensajesVista.formatoImporte(modelo.getImporte()));
        return lineas;
    }
}
