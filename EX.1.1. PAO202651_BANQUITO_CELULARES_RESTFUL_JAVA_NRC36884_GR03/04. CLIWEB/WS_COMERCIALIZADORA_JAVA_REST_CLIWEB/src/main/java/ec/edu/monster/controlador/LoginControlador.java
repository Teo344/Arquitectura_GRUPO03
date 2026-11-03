package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Coordina la vista de acceso de los empleados y el inicio de sesión en la API. */
@WebServlet(urlPatterns = {"", "/login"})
public class LoginControlador extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ClienteRESTMonsterCell clienteRest = cliente(req);
        if (clienteRest != null && clienteRest.getSesion() != null) {
            redirigir(MenuControlador.INICIO, req, resp);
            return;
        }
        tomarAviso(req);
        mostrar("login", req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String usuario = req.getParameter("usuario");
        String clave = req.getParameter("clave");
        String usuarioLimpio = usuario == null ? "" : usuario.trim();
        req.setAttribute("usuario", usuarioLimpio);
        if (usuarioLimpio.isEmpty() || clave == null || clave.isEmpty()) {
            mostrarMensaje(req, "Ingresa tu usuario y contraseña.", true);
            mostrar("login", req, resp);
            return;
        }

        try {
            // Un cliente REST por sesión del navegador: guarda el token de este empleado
            ClienteRESTMonsterCell clienteRest = cliente(req);
            if (clienteRest == null) {
                clienteRest = new ClienteRESTMonsterCell();
                req.getSession().setAttribute(ATRIBUTO_CLIENTE, clienteRest);
            }
            clienteRest.iniciarSesion(usuarioLimpio, clave);
            // Nuevo identificador de sesión al autenticarse (fijación de sesión)
            req.changeSessionId();
            redirigir(MenuControlador.INICIO, req, resp);
        } catch (RuntimeException ex) {
            mostrarMensaje(req, mensajeDe(ex), true);
            mostrar("login", req, resp);
        }
    }
}
