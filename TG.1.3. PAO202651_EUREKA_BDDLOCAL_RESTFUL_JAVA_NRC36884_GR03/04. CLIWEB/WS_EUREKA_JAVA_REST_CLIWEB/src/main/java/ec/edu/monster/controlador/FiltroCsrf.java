package ec.edu.monster.controlador;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Protección contra CSRF: cada sesión tiene un token aleatorio que las JSP
 * ponen en un campo oculto (${sessionScope.csrf}); un POST sin ese token
 * (por ejemplo, desde otra página) se rechaza con 403 y no llega al
 * controlador. Las consultas (GET) no cambian datos y no lo necesitan.
 */
@WebFilter("/*")
public class FiltroCsrf extends HttpFilter {

    public static final String ATRIBUTO = "csrf";
    private static final SecureRandom ALEATORIO = new SecureRandom();

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain cadena)
            throws IOException, ServletException {
        String ruta = req.getServletPath();
        if (ruta.startsWith("/css/") || ruta.startsWith("/js/") || ruta.startsWith("/fuentes/")
                || ruta.startsWith("/img/")) {
            cadena.doFilter(req, resp);
            return;
        }
        if ("POST".equalsIgnoreCase(req.getMethod())) {
            HttpSession sesion = req.getSession(false);
            String esperado = sesion == null ? null : (String) sesion.getAttribute(ATRIBUTO);
            String recibido = req.getParameter(ATRIBUTO);
            if (esperado == null || recibido == null || !MessageDigest.isEqual(
                    esperado.getBytes(StandardCharsets.UTF_8), recibido.getBytes(StandardCharsets.UTF_8))) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "La página caducó. Vuelve a abrirla e intenta de nuevo.");
                return;
            }
        } else {
            HttpSession sesion = req.getSession();
            if (sesion.getAttribute(ATRIBUTO) == null) {
                byte[] bytes = new byte[32];
                ALEATORIO.nextBytes(bytes);
                sesion.setAttribute(ATRIBUTO, Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
            }
        }
        cadena.doFilter(req, resp);
    }
}
