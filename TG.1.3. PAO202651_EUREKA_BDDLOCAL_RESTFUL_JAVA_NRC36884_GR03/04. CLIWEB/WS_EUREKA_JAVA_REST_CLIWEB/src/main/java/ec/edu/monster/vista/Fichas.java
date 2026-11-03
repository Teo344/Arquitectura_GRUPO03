package ec.edu.monster.vista;

import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.modelo.TransaccionModelo;
import java.util.List;

/**
 * Filas etiqueta / valor del resumen de confirmación y del comprobante, las
 * mismas de TransaccionVista del cliente de escritorio. La primera fila del
 * comprobante es la destacada (el nuevo saldo).
 */
public final class Fichas {

    private Fichas() {
    }

    public static List<String[]> comprobante(TransaccionModelo registrada, ResultadoTransaccionModelo resultado) {
        String origen = Formato.valorO(registrada.getCuenta()).trim();
        if (registrada.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            return List.of(
                    new String[]{"Nuevo saldo de origen", Formato.importe(resultado.getNuevoSaldo())},
                    new String[]{"Cuenta de origen", origen},
                    new String[]{"Cuenta de destino", Formato.valorO(registrada.getCuentaDestino()).trim()},
                    new String[]{"Importe", Formato.importe(registrada.getImporte())},
                    new String[]{"Movimiento en origen", "N.º " + numero(resultado.getNumeroMovimiento())},
                    new String[]{"Movimiento en destino", "N.º " + numero(resultado.getNumeroMovimientoDestino())});
        }
        return List.of(
                new String[]{"Nuevo saldo", Formato.importe(resultado.getNuevoSaldo())},
                new String[]{"Cuenta", origen},
                new String[]{"Importe", Formato.importe(registrada.getImporte())},
                new String[]{"Movimiento", "N.º " + numero(resultado.getNumeroMovimiento())});
    }

    public static List<String[]> resumen(TransaccionModelo modelo) {
        if (modelo.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            return List.of(
                    new String[]{"Cuenta de origen", Formato.valorO(modelo.getCuenta()).trim()},
                    new String[]{"Cuenta de destino", Formato.valorO(modelo.getCuentaDestino()).trim()},
                    new String[]{"Importe", Formato.importe(modelo.getImporte())});
        }
        return List.of(
                new String[]{"Operación", modelo.getTipo().getNombreVisible()},
                new String[]{"Cuenta", Formato.valorO(modelo.getCuenta()).trim()},
                new String[]{"Importe", Formato.importe(modelo.getImporte())});
    }

    private static String numero(Integer valor) {
        return valor == null ? "-" : valor.toString();
    }
}
