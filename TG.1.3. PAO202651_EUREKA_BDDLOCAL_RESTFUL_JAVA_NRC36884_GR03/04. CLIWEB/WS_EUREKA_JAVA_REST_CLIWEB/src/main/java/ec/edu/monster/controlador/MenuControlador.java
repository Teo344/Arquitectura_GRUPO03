package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ClienteRESTEureka;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Controlador del menú principal: abre cada operación y cierra la sesión (RQF-002). */
@WebServlet(urlPatterns = {"/menu", "/salir"})
public class MenuControlador extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/salir".equals(req.getServletPath())) {
            // Cerrar sesión cambia el estado: solo por POST
            redirigir("/menu", req, resp);
            return;
        }
        // Como en escritorio, volver al menú cierra la pantalla anterior y lo que mostraba
        CuentaControlador.limpiar(req.getSession());
        TransaccionControlador.limpiar(req.getSession());
        req.setAttribute("sesion", cliente(req).getSesion());
        mostrar("menu", req, resp);
    }

    /**
     * Invalida el token en el servidor. Aunque el servidor no responda o el
     * token ya haya caducado, la sesión local termina y se vuelve al login.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ClienteRESTEureka clienteRest = cliente(req);
        try {
            if (clienteRest != null) {
                clienteRest.cerrarSesion();
            }
        } catch (RuntimeException ignorada) {
            // La sesión local se descarta de todas formas (RQF-002)
        }
        volverAlLogin("Sesión cerrada correctamente.", false, req, resp);
    }
}
