package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CuentaModelo;
import ec.edu.monster.modelo.MovimientoModelo;
import ec.edu.monster.servicio.ClienteWSEureka;
import java.util.List;
import java.util.regex.Pattern;

/** Consulta de cuenta (RQF-003) y de movimientos (RQF-004). */
public class CuentaControlador {

    private static final Pattern FORMATO_CUENTA = Pattern.compile("\\d{8}");

    private final ClienteWSEureka clienteWs;

    public CuentaControlador(ClienteWSEureka clienteWs) {
        this.clienteWs = clienteWs;
    }

    public CuentaModelo consultarCuenta(String cuenta) {
        return clienteWs.consultarCuenta(validarCuenta(cuenta, "La cuenta"));
    }

    public List<MovimientoModelo> consultarMovimientos(String cuenta) {
        return clienteWs.consultarMovimientos(validarCuenta(cuenta, "La cuenta"));
    }

    /**
     * Mismo formato que exige el servidor (DATA-001): 8 dígitos. Devuelve el
     * código sin espacios.
     */
    static String validarCuenta(String cuenta, String nombre) {
        String codigo = cuenta == null ? "" : cuenta.trim();
        if (!FORMATO_CUENTA.matcher(codigo).matches()) {
            throw new IllegalArgumentException(nombre + " debe tener exactamente 8 digitos (por ejemplo 00100001).");
        }
        return codigo;
    }
}
