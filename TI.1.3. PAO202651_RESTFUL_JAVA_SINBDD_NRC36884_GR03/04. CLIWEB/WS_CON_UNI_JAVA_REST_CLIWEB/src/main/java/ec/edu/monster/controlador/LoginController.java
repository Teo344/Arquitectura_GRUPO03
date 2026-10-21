package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Sesion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Coordina la vista de acceso con el servicio REST. */
@WebServlet(urlPatterns = {"", "/login"})
public class LoginController extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (sesion(req) != null) {
            redirigir("/menu", req, resp);
            return;
        }
        if (req.getParameter("expirada") != null) {
            estado(req, "Tu sesión expiró. Inicia sesión nuevamente.", true);
        }
        mostrar("login", req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usuario = req.getParameter("usuario") == null ? "" : req.getParameter("usuario").trim();
        String clave = req.getParameter("clave") == null ? "" : req.getParameter("clave");
        req.setAttribute("usuario", usuario);
        if (usuario.isBlank() || clave.isEmpty()) {
            estado(req, "Ingresa usuario y contraseña.", true);
            mostrar("login", req, resp);
            return;
        }
        try {
            Sesion sesion = api().login(usuario, clave);
            req.getSession().setAttribute(ATRIBUTO_SESION, sesion);
            req.changeSessionId();
            redirigir("/menu", req, resp);
        } catch (Exception e) {
            estado(req, mensajeError(e, "No se pudo iniciar sesión."), true);
            mostrar("login", req, resp);
        }
    }

    private static void estado(HttpServletRequest req, String texto, boolean error) {
        req.setAttribute("estado", texto);
        req.setAttribute("estadoError", error);
    }
}
