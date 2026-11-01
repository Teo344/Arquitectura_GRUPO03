package ec.edu.monster.seguridad;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Permite que páginas web de los orígenes configurados (cors.origenes) llamen
 * al servicio desde el navegador. No reemplaza la autenticación: el token lo
 * sigue validando ManejadorAutenticacion.
 *
 * @author carlo
 */
@WebFilter("/*")
public class FiltroCors implements Filter {

    private List<String> origenes;

    @Override
    public void init(FilterConfig configuracion) {
        origenes = ConfigProvider.getConfig().getValues("cors.origenes", String.class);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest peticion = (HttpServletRequest) req;
        HttpServletResponse respuesta = (HttpServletResponse) res;

        String origen = peticion.getHeader("Origin");
        if (origen != null && origenes.contains(origen)) {
            respuesta.setHeader("Access-Control-Allow-Origin", origen);
            respuesta.setHeader("Vary", "Origin");
            respuesta.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            respuesta.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            respuesta.setHeader("Access-Control-Max-Age", "3600");
        }

        // La consulta previa (preflight) del navegador se responde aquí.
        if ("OPTIONS".equalsIgnoreCase(peticion.getMethod())) {
            respuesta.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        cadena.doFilter(req, res);
    }
}
