package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Las pantallas que requieren sesión redirigen al login si el empleado no ha
 * iniciado sesión. Tampoco se guardan en la caché del navegador: después de
 * salir, el botón Atrás no vuelve a mostrar facturas ni datos de clientes.
 */
@WebFilter({"/menu", "/salir", "/venta", "/venta/*", "/facturas", "/facturas/*", "/telefonos", "/telefonos/*",
    "/clientes", "/creditos/*", "/simulador"})
public class FiltroSesion extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain cadena)
            throws IOException, ServletException {
        HttpSession sesion = req.getSession(false);
        ClienteRESTMonsterCell clienteRest = sesion == null ? null
                : (ClienteRESTMonsterCell) sesion.getAttribute(ControladorBase.ATRIBUTO_CLIENTE);
        if (clienteRest == null || clienteRest.getSesion() == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        resp.setHeader("Cache-Control", "no-store");
        cadena.doFilter(req, resp);
    }
}
