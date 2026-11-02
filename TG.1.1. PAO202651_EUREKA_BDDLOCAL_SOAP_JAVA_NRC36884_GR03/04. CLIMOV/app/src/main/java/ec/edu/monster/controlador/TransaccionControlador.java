package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.modelo.TransaccionModelo;
import ec.edu.monster.servicio.TransaccionSoapService;
import ec.edu.monster.vista.TransaccionActivity;
import java.math.BigDecimal;

/**
 * Depósitos (RQF-005), retiros (RQF-006) y transferencias (RQF-007). Valida los
 * datos antes de enviarlos para orientar al empleado; el servidor vuelve a
 * validar todas las reglas de negocio.
 */
public class TransaccionControlador extends ControladorBase {

    // Mayor valor que admite DECIMAL(12,2) en el servidor (RN-01)
    private static final BigDecimal IMPORTE_MAXIMO = new BigDecimal("9999999999.99");

    private final TransaccionActivity vista;
    private final TransaccionSoapService servicio;
    private final SesionModelo sesion;

    public TransaccionControlador(TransaccionActivity vista, TransaccionSoapService servicio, SesionModelo sesion) {
        this.vista = vista;
        this.servicio = servicio;
        this.sesion = sesion;
    }

    /**
     * Valida los datos escritos y, si son correctos, pide a la vista la
     * confirmación del empleado antes de registrar nada.
     */
    public void preparar(TipoTransaccion tipo, String cuenta, String cuentaDestino, String textoImporte) {
        try {
            TransaccionModelo modelo = new TransaccionModelo(tipo, cuenta,
                    tipo == TipoTransaccion.TRANSFERENCIA ? cuentaDestino : null, leerImporte(textoImporte));
            validar(modelo);
            vista.pedirConfirmacion(modelo);
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
        }
    }

    public void ejecutar(TransaccionModelo modelo) {
        try {
            validar(modelo);
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
            return;
        }
        String cuenta = modelo.getCuenta().trim();
        vista.setProcesando(true);
        enSegundoPlano(() -> {
            try {
                ResultadoTransaccionModelo resultado;
                switch (modelo.getTipo()) {
                    case DEPOSITO:
                        resultado = servicio.depositar(sesion.getToken(), cuenta, modelo.getImporte());
                        break;
                    case RETIRO:
                        resultado = servicio.retirar(sesion.getToken(), cuenta, modelo.getImporte());
                        break;
                    default:
                        resultado = servicio.transferir(sesion.getToken(), cuenta,
                                modelo.getCuentaDestino().trim(), modelo.getImporte());
                        break;
                }
                ResultadoTransaccionModelo registrado = resultado;
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.mostrarResultado(modelo, registrado);
                });
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });
    }

    /** RN-01 y RN-04, con los mismos mensajes que las consolas. */
    private static void validar(TransaccionModelo modelo) {
        if (modelo == null || modelo.getTipo() == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de transacción.");
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
        BigDecimal importe = modelo.getImporte();
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero y tener como máximo dos decimales.");
        }
        if (importe.compareTo(IMPORTE_MAXIMO) > 0) {
            throw new IllegalArgumentException("El importe no puede superar 9999999999.99.");
        }
    }

    /** Acepta coma o punto decimal; el dinero se maneja siempre con BigDecimal. */
    private static BigDecimal leerImporte(String texto) {
        String valor = texto == null ? "" : texto.trim().replace(',', '.');
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el importe de la operación.");
        }
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingresa un importe numérico válido, por ejemplo 150.50");
        }
    }

    private void mostrarError(RuntimeException error) {
        enPantalla(() -> {
            vista.setProcesando(false);
            if (requiereNuevoLogin(error)) {
                vista.volverAlLogin(mensajeDe(error));
                return;
            }
            vista.mostrarMensaje(mensajeDe(error), true);
        });
    }
}
