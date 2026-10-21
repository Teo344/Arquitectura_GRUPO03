package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ApiRest;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Crea una sola ApiRest para toda la aplicación con la URL base de web.xml
 * (api.base), equivalente al -Dapi.base del cliente de escritorio.
 */
@WebListener
public class ConfiguracionCliente implements ServletContextListener {
    private static final String URL_POR_DEFECTO = "http://localhost:8080/WS_CON_UNI_JAVA_REST/api";

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        String base = evento.getServletContext().getInitParameter("api.base");
        evento.getServletContext().setAttribute(ControladorBase.ATRIBUTO_API,
                new ApiRest(base == null || base.isBlank() ? URL_POR_DEFECTO : base));
    }
}
