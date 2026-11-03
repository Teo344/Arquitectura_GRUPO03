package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.servicio.ClienteWSEureka;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Comportamiento común de los controladores, como el ControladorBase del
 * cliente de escritorio: mensajes con el código del ERS y regreso al login
 * cuando el token caduca. En la web la llamada SOAP se hace dentro de la
 * petición, así que no hacen falta hilos: el navegador muestra la espera.
 */
public abstract class ControladorBase extends HttpServlet {

    /** El ClienteWSEureka de la sesión: sus puertos SOAP y la SesionModelo del empleado. */
    public static final String ATRIBUTO_CLIENTE = "clienteWs";
    private static final String ATRIBUTO_AVISO = "aviso";
    private static final String CARPETA_VISTAS = "/WEB-INF/vista/";

    /** Mensaje que sobrevive a una redirección (Post/Redirect/Get) y se muestra una sola vez. */
    private record Aviso(String texto, boolean esError) {
    }

    protected static ClienteWSEureka cliente(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        return sesion == null ? null : (ClienteWSEureka) sesion.getAttribute(ATRIBUTO_CLIENTE);
    }

    protected void mostrar(String vista, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher(CARPETA_VISTAS + vista + ".jsp").forward(req, resp);
    }

    protected void redirigir(String ruta, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + ruta);
    }

    /** Equivale a vista.mostrarMensaje(texto, esError) del escritorio. */
    protected static void mostrarMensaje(HttpServletRequest req, String texto, boolean esError) {
        req.setAttribute("mensaje", texto);
        req.setAttribute("mensajeError", esError);
    }

    protected static void avisar(HttpServletRequest req, String texto, boolean esError) {
        req.getSession().setAttribute(ATRIBUTO_AVISO, new Aviso(texto, esError));
    }

    /** Pasa a la vista el aviso pendiente de la petición anterior. */
    protected static void tomarAviso(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        if (sesion != null && sesion.getAttribute(ATRIBUTO_AVISO) instanceof Aviso aviso) {
            sesion.removeAttribute(ATRIBUTO_AVISO);
            mostrarMensaje(req, aviso.texto(), aviso.esError());
        }
    }

    /**
     * Termina la sesión web y vuelve al login con un mensaje (sesión cerrada
     * o caducada), como Ventana.abrirLogin(mensaje, esError).
     */
    protected void volverAlLogin(String mensaje, boolean esError, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        avisar(req, mensaje, esError);
        redirigir("/login", req, resp);
    }

    /** AUTH-003: el token ya no es válido y hay que volver a iniciar sesión. */
    protected static boolean requiereNuevoLogin(Throwable error) {
        return error instanceof ErrorServicioModelo servicio && servicio.requiereNuevoLogin();
    }

    /** Mensaje para el empleado con el código del ERS cuando el servidor lo envía. */
    protected static String mensajeDe(Throwable error) {
        String mensaje = error == null ? null : error.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            mensaje = "No se pudo completar la operación.";
        }
        if (error instanceof ErrorServicioModelo servicio && servicio.getCodigo() != null) {
            return mensaje + " (" + servicio.getCodigo() + ")";
        }
        return mensaje;
    }
}
