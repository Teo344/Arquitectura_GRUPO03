package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CuentaModelo;
import ec.edu.monster.modelo.MovimientoModelo;
import ec.edu.monster.servicio.ClienteWSEureka;
import ec.edu.monster.vista.CuentaVista;
import java.util.List;
import java.util.regex.Pattern;

/** Consulta de cuenta (RQF-003) y de movimientos (RQF-004). */
public class CuentaControlador extends ControladorBase {

    private static final Pattern FORMATO_CUENTA = Pattern.compile("\\d{8}");

    private final CuentaVista vista;
    private final ClienteWSEureka clienteWs;

    public CuentaControlador(CuentaVista vista, ClienteWSEureka clienteWs) {
        this.vista = vista;
        this.clienteWs = clienteWs;
    }

    public void consultarCuenta(String cuenta) {
        final String codigo;
        try {
            codigo = validarCuenta(cuenta, "La cuenta");
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
            return;
        }
        vista.setProcesando(true);
        enSegundoPlano(() -> {
            try {
                CuentaModelo resultado = clienteWs.consultarCuenta(codigo);
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.mostrarCuenta(resultado);
                });
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });
    }

    public void consultarMovimientos(String cuenta) {
        final String codigo;
        try {
            codigo = validarCuenta(cuenta, "La cuenta");
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
            return;
        }
        vista.setProcesando(true);
        enSegundoPlano(() -> {
            try {
                List<MovimientoModelo> movimientos = clienteWs.consultarMovimientos(codigo);
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.mostrarMovimientos(codigo, movimientos);
                });
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });
    }

    /**
     * Mismo formato que exige el servidor (DATA-001): 8 dígitos. Devuelve el
     * código sin espacios.
     */
    static String validarCuenta(String cuenta, String nombre) {
        String codigo = cuenta == null ? "" : cuenta.trim();
        if (!FORMATO_CUENTA.matcher(codigo).matches()) {
            throw new IllegalArgumentException(nombre + " debe tener exactamente 8 dígitos (por ejemplo 00100001).");
        }
        return codigo;
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
