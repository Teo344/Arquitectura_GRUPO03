package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.modelo.TransaccionModelo;
import ec.edu.monster.servicio.ClienteRESTEureka;
import java.math.BigDecimal;

/**
 * Depósitos (RQF-005), retiros (RQF-006) y transferencias (RQF-007). Valida los
 * datos antes de enviarlos para orientar al empleado; el servidor vuelve a
 * validar todas las reglas de negocio.
 */
public class TransaccionControlador {

    // Mayor valor que admite DECIMAL(12,2) en el servidor (RN-01)
    private static final BigDecimal IMPORTE_MAXIMO = new BigDecimal("9999999999.99");

    private final ClienteRESTEureka clienteRest;

    public TransaccionControlador(ClienteRESTEureka clienteRest) {
        this.clienteRest = clienteRest;
    }

    /** Permite a la vista avisar de un dato inválido antes de pedir la confirmación. */
    public void validar(TransaccionModelo modelo) {
        if (modelo == null || modelo.getTipo() == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de transaccion.");
        }
        if (modelo.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            String origen = CuentaControlador.validarCuenta(modelo.getCuenta(), "La cuenta de origen");
            String destino = CuentaControlador.validarCuenta(modelo.getCuentaDestino(), "La cuenta de destino");
            if (origen.equals(destino)) {
                throw new IllegalArgumentException("La cuenta de destino debe ser diferente de la cuenta de origen.");
            }
        } else {
            CuentaControlador.validarCuenta(modelo.getCuenta(), "La cuenta");
        }
        validarImporte(modelo.getImporte());
    }

    public ResultadoTransaccionModelo ejecutar(TransaccionModelo modelo) {
        validar(modelo);
        String cuenta = modelo.getCuenta().trim();
        return switch (modelo.getTipo()) {
            case DEPOSITO -> clienteRest.depositar(cuenta, modelo.getImporte());
            case RETIRO -> clienteRest.retirar(cuenta, modelo.getImporte());
            case TRANSFERENCIA -> clienteRest.transferir(cuenta, modelo.getCuentaDestino().trim(), modelo.getImporte());
        };
    }

    // RN-01: mayor que cero, máximo dos decimales y dentro de DECIMAL(12,2)
    private static void validarImporte(BigDecimal importe) {
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero y tener como maximo dos decimales.");
        }
        if (importe.compareTo(IMPORTE_MAXIMO) > 0) {
            throw new IllegalArgumentException("El importe no puede superar 9999999999.99.");
        }
    }
}
