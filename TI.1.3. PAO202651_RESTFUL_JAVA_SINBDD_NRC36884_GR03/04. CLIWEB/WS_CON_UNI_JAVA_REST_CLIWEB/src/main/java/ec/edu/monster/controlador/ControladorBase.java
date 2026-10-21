package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.servicio.ApiRest;
import ec.edu.monster.servicio.ErrorApi;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Comportamiento común de los controladores: sesión, servicio REST, navegación y mensajes de error. */
abstract class ControladorBase extends HttpServlet {
    static final String ATRIBUTO_SESION = "sesion";
    static final String ATRIBUTO_API = "api";
    private static final String CARPETA_VISTAS = "/WEB-INF/vista/";

    protected Sesion sesion(HttpServletRequest req) {
        return (Sesion) req.getSession().getAttribute(ATRIBUTO_SESION);
    }

    protected ApiRest api() {
        return (ApiRest) getServletContext().getAttribute(ATRIBUTO_API);
    }

    protected void mostrar(String vista, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher(CARPETA_VISTAS + vista + ".jsp").forward(req, resp);
    }

    protected void redirigir(String ruta, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + ruta);
    }

    /** Mensaje para la vista, como en los controladores del cliente de escritorio REST. */
    protected static String mensajeError(Exception error, String porDefecto) {
        if (error instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        if (sinConexion(error)) {
            return "No se pudo conectar. Inicia el servidor y revisa la URL base.";
        }
        String mensaje = error.getMessage();
        return mensaje == null || mensaje.isBlank() ? porDefecto : mensaje;
    }

    /** 401 con AUTH-001 o AUTH-002: el token ya no es válido y hay que volver a iniciar sesión. */
    protected static boolean sesionVencida(Exception error) {
        return error instanceof ErrorApi api && api.getEstadoHttp() == 401
                && ("AUTH-001".equals(api.getCodigo()) || "AUTH-002".equals(api.getCodigo()));
    }

    private static boolean sinConexion(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            String mensaje = causa.getMessage();
            if (causa instanceof java.net.ConnectException
                    || (mensaje != null && (mensaje.contains("Connection refused") || mensaje.contains("ConnectException")))) {
                return true;
            }
        }
        return false;
    }
}
