package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.servicio.AutenticacionSoapService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Coordina la vista de acceso y el servicio remoto de autenticación. */
@WebServlet(urlPatterns = {"", "/login"})
public class LoginController extends ControladorBase {
    private final AutenticacionSoapService autenticacion = new AutenticacionSoapService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (sesion(req) != null) {
            redirigir("/menu", req, resp);
            return;
        }
        if (req.getParameter("expirada") != null) {
            mostrarMensaje(req, "Tu sesión expiró. Inicia sesión nuevamente.", true);
        }
        mostrar("login", req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usuario = req.getParameter("usuario") == null ? "" : req.getParameter("usuario").trim();
        String clave = req.getParameter("clave") == null ? "" : req.getParameter("clave");
        req.setAttribute("usuario", usuario);
        if (usuario.isEmpty() || clave.isEmpty()) {
            mostrarMensaje(req, "Ingresa tu usuario y contraseña.", true);
            mostrar("login", req, resp);
            return;
        }
        try {
            SesionUsuario sesion = autenticacion.iniciarSesion(usuario, clave);
            req.getSession().setAttribute(ATRIBUTO_SESION, sesion);
            req.changeSessionId();
            redirigir("/menu", req, resp);
        } catch (Exception ex) {
            mostrarMensaje(req, mensajeError(ex, "Ocurrió un error al iniciar sesión."), true);
            mostrar("login", req, resp);
        }
    }

    private void mostrarMensaje(HttpServletRequest req, String texto, boolean error) {
        req.setAttribute("mensaje", texto);
        req.setAttribute("mensajeError", error);
    }
}
