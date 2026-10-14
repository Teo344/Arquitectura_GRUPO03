package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.servicio.ExcepcionServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.io.IOException;
import org.w3c.dom.NodeList;

/** Comportamiento común de los controladores: sesión, navegación y mensajes de error. */
abstract class ControladorBase extends HttpServlet {
    static final String ATRIBUTO_SESION = "sesion";
    private static final String CARPETA_VISTAS = "/WEB-INF/vista/";

    protected SesionUsuario sesion(HttpServletRequest req) {
        return (SesionUsuario) req.getSession().getAttribute(ATRIBUTO_SESION);
    }

    protected void mostrar(String vista, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher(CARPETA_VISTAS + vista + ".jsp").forward(req, resp);
    }

    protected void redirigir(String ruta, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + ruta);
    }

    /** Toma el mensaje del SOAPFault cuando existe, igual que el cliente de escritorio. */
    protected static String mensajeError(Throwable error, String porDefecto) {
        if (error instanceof ExcepcionServicio soapError && soapError.getFaultInfo() != null
                && soapError.getFaultInfo().getMensaje() != null && !soapError.getFaultInfo().getMensaje().isBlank()) {
            return soapError.getFaultInfo().getMensaje();
        }
        if (sinConexion(error)) {
            return "No se pudo conectar con el servidor. Verifica que esté iniciado.";
        }
        String mensaje = error.getMessage();
        return mensaje == null || mensaje.isBlank() ? porDefecto : mensaje;
    }

    /** AUTH-001 o AUTH-002: el token ya no es válido y hay que volver a iniciar sesión. */
    protected static boolean sesionVencida(Throwable error) {
        String codigo = codigoFalla(error);
        return "AUTH-001".equals(codigo) || "AUTH-002".equals(codigo);
    }

    /**
     * Código del detalle ErrorServicio. Llega como ExcepcionServicio si la operación declara
     * la falla en el WSDL, o como SOAPFaultException si no la declara (consultarCatalogo).
     */
    private static String codigoFalla(Throwable error) {
        if (error instanceof ExcepcionServicio soapError && soapError.getFaultInfo() != null) {
            return soapError.getFaultInfo().getCodigo();
        }
        if (error instanceof SOAPFaultException falla && falla.getFault() != null && falla.getFault().getDetail() != null) {
            NodeList codigos = falla.getFault().getDetail().getElementsByTagName("codigo");
            return codigos.getLength() > 0 ? codigos.item(0).getTextContent() : null;
        }
        return null;
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
