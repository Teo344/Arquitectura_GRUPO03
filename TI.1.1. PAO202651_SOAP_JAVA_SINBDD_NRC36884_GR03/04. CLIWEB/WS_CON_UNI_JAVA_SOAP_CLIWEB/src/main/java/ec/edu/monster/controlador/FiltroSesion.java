package ec.edu.monster.controlador;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Las vistas que requieren sesión redirigen al login si no hay usuario autenticado. */
@WebFilter({"/menu", "/conversion", "/catalogo"})
public class FiltroSesion extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain cadena)
            throws IOException, ServletException {
        HttpSession sesion = req.getSession(false);
        if (sesion == null || sesion.getAttribute(ControladorBase.ATRIBUTO_SESION) == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        cadena.doFilter(req, resp);
    }
}
