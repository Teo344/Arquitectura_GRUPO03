package ec.edu.monster.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Controlador del menú principal; conecta las opciones a las vistas correspondientes. */
@WebServlet(urlPatterns = {"/menu", "/salir"})
public class MenuController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/salir".equals(req.getServletPath())) {
            req.getSession().invalidate();
            redirigir("/login", req, resp);
            return;
        }
        // Como en escritorio, volver al menú cierra el conversor y con él su historial.
        req.getSession().removeAttribute(ConversionController.ATRIBUTO_HISTORIAL);
        mostrar("menu", req, resp);
    }
}
