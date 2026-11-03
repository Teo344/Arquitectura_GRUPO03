package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Inicio de la tienda y cierre de sesión. La pantalla de inicio es la nueva
 * venta (la barra lateral lleva a las demás secciones); /menu se conserva para
 * los enlaces antiguos.
 */
@WebServlet(urlPatterns = {"/menu", "/salir"})
public class MenuControlador extends ControladorBase {

    /** Pantalla de inicio después del login. */
    public static final String INICIO = "/venta";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Cerrar sesión cambia el estado: solo por POST
        redirigir(INICIO, req, resp);
    }

    /**
     * Invalida el token en el servidor. Aunque el servidor no responda o el
     * token ya haya caducado, la sesión local termina y se vuelve al login.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ClienteRESTMonsterCell clienteRest = cliente(req);
        try {
            if (clienteRest != null) {
                clienteRest.cerrarSesion();
            }
        } catch (RuntimeException ignorada) {
            // La sesión local se descarta de todas formas
        }
        volverAlLogin("Sesión cerrada correctamente.", false, req, resp);
    }
}
