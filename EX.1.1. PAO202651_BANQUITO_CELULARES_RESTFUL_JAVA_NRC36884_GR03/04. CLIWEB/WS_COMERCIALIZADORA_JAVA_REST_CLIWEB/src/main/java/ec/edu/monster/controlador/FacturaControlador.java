package ec.edu.monster.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Facturas: GET /facturas lista las ventas (filtro por cédula) y
 * GET /facturas/{numero} muestra una factura lista para imprimir.
 */
@WebServlet(urlPatterns = {"/facturas", "/facturas/*"})
public class FacturaControlador extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        tomarAviso(req);
        String ruta = req.getPathInfo();
        try {
            if (ruta == null || ruta.equals("/")) {
                listar(req, resp);
                return;
            }
            Integer numero = Validacion.entero(ruta.substring(1));
            if (numero == null) {
                redirigir("/facturas", req, resp);
                return;
            }
            req.setAttribute("factura", cliente(req).factura(numero));
            mostrar("factura", req, resp);
        } catch (RuntimeException ex) {
            if (requiereNuevoLogin(ex)) {
                volverAlLogin(mensajeDe(ex) + ". Inicia sesión nuevamente.", true, req, resp);
                return;
            }
            avisar(req, mensajeDe(ex), true);
            redirigir("/facturas", req, resp);
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cedula = req.getParameter("cedula") == null ? "" : req.getParameter("cedula").trim();
        req.setAttribute("cedula", cedula);
        try {
            if (!cedula.isEmpty()) {
                Validacion.cedula(cedula);
            }
            req.setAttribute("facturas", cliente(req).facturas(cedula));
        } catch (IllegalArgumentException ex) {
            mostrarMensaje(req, ex.getMessage(), true);
            req.setAttribute("facturas", List.of());
        }
        mostrar("facturas", req, resp);
    }
}
