package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CuentaModelo;
import ec.edu.monster.modelo.MovimientoModelo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Consulta de cuenta (RQF-003) y de movimientos (RQF-004). Es una consulta,
 * así que va por GET (/consultas?modo=cuenta&cuenta=00100001). Como en
 * CuentaVista, el número escrito y el último resultado de cada modo se
 * conservan mientras no se vuelva al menú.
 */
@WebServlet("/consultas")
public class CuentaControlador extends ControladorBase {

    private static final Pattern FORMATO_CUENTA = Pattern.compile("\\d{8}");

    static final String MODO_CUENTA = "cuenta";
    static final String MODO_MOVIMIENTOS = "movimientos";

    private static final String CUENTA = "consultas.cuenta";
    private static final String DATOS = "consultas.datos";
    private static final String MOVIMIENTOS = "consultas.movimientos";
    private static final String CUENTA_MOVIMIENTOS = "consultas.cuentaMovimientos";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession sesion = req.getSession();
        String modo = MODO_MOVIMIENTOS.equals(req.getParameter("modo")) ? MODO_MOVIMIENTOS : MODO_CUENTA;
        String cuenta = req.getParameter("cuenta");

        if (cuenta != null) {
            sesion.setAttribute(CUENTA, cuenta);
            try {
                String codigo = validarCuenta(cuenta, "La cuenta");
                if (MODO_CUENTA.equals(modo)) {
                    CuentaModelo resultado = cliente(req).consultarCuenta(codigo);
                    sesion.setAttribute(DATOS, resultado);
                } else {
                    List<MovimientoModelo> movimientos = cliente(req).consultarMovimientos(codigo);
                    sesion.setAttribute(MOVIMIENTOS, movimientos);
                    sesion.setAttribute(CUENTA_MOVIMIENTOS, codigo);
                }
            } catch (IllegalArgumentException ex) {
                mostrarMensaje(req, ex.getMessage(), true);
            } catch (RuntimeException ex) {
                if (requiereNuevoLogin(ex)) {
                    volverAlLogin(mensajeDe(ex) + ". Inicia sesión nuevamente.", true, req, resp);
                    return;
                }
                mostrarMensaje(req, mensajeDe(ex), true);
            }
        }

        req.setAttribute("modo", modo);
        req.setAttribute("cuenta", sesion.getAttribute(CUENTA));
        req.setAttribute("datosCuenta", sesion.getAttribute(DATOS));
        req.setAttribute("movimientos", sesion.getAttribute(MOVIMIENTOS));
        req.setAttribute("cuentaMovimientos", sesion.getAttribute(CUENTA_MOVIMIENTOS));
        mostrar("consultas", req, resp);
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

    /** Al volver al menú la pantalla de consultas se cierra y olvida lo que mostraba. */
    static void limpiar(HttpSession sesion) {
        sesion.removeAttribute(CUENTA);
        sesion.removeAttribute(DATOS);
        sesion.removeAttribute(MOVIMIENTOS);
        sesion.removeAttribute(CUENTA_MOVIMIENTOS);
    }
}
