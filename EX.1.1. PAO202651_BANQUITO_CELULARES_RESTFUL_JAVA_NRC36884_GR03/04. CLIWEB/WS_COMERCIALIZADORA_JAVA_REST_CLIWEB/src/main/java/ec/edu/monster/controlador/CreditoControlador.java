package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.vista.ResumenCredito;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Crédito BanQuito, todo por GET porque solo consulta:
 * <ul>
 * <li>/clientes?cedula= evaluación del comprador (sujeto de crédito y monto
 * máximo) y sus créditos;</li>
 * <li>/creditos/{codigo} tabla de amortización de un crédito otorgado;</li>
 * <li>/simulador?monto=&amp;cuotas= tabla de un crédito sin otorgarlo.</li>
 * </ul>
 */
@WebServlet(urlPatterns = {"/clientes", "/creditos/*", "/simulador"})
public class CreditoControlador extends ControladorBase {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        tomarAviso(req);
        try {
            switch (req.getServletPath()) {
                case "/clientes" -> cliente(req, resp);
                case "/simulador" -> simulador(req, resp);
                default -> tabla(req, resp);
            }
        } catch (RuntimeException ex) {
            if (requiereNuevoLogin(ex)) {
                volverAlLogin(mensajeDe(ex) + ". Inicia sesión nuevamente.", true, req, resp);
                return;
            }
            throw ex;
        }
    }

    private void cliente(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cedula = req.getParameter("cedula") == null ? "" : req.getParameter("cedula").trim();
        req.setAttribute("cedula", cedula);
        if (!cedula.isEmpty()) {
            try {
                EvaluacionClienteModelo evaluacion = cliente(req).evaluarCliente(Validacion.cedula(cedula));
                req.setAttribute("evaluacion", evaluacion);
                if (evaluacion.isCliente()) {
                    req.setAttribute("creditos", cliente(req).creditos(cedula));
                }
            } catch (IllegalArgumentException ex) {
                mostrarMensaje(req, ex.getMessage(), true);
            } catch (RuntimeException ex) {
                if (requiereNuevoLogin(ex)) {
                    throw ex;
                }
                mostrarMensaje(req, mensajeDe(ex), true);
            }
        }
        mostrar("clientes", req, resp);
    }

    private void tabla(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer codigo = req.getPathInfo() == null ? null : Validacion.entero(req.getPathInfo().substring(1));
        if (codigo == null) {
            redirigir("/clientes", req, resp);
            return;
        }
        req.setAttribute("codCredito", codigo);
        req.setAttribute("cedula", req.getParameter("cedula"));
        try {
            List<CuotaModelo> cuotas = cliente(req).cuotas(codigo);
            req.setAttribute("cuotas", cuotas);
            req.setAttribute("resumen", ResumenCredito.de(cuotas));
        } catch (RuntimeException ex) {
            if (requiereNuevoLogin(ex)) {
                throw ex;
            }
            mostrarMensaje(req, mensajeDe(ex), true);
        }
        mostrar("credito", req, resp);
    }

    private void simulador(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String monto = req.getParameter("monto");
        String cuotas = req.getParameter("cuotas");
        req.setAttribute("monto", monto == null ? "" : monto);
        req.setAttribute("numCuotas", Validacion.entero(cuotas) == null ? 6 : Validacion.entero(cuotas));
        if (monto != null) {
            try {
                BigDecimal valor = Validacion.importe(monto, "monto");
                Integer n = Validacion.entero(cuotas);
                if (n == null || n < 3 || n > 18) {
                    throw new IllegalArgumentException("Elige entre 3 y 18 cuotas.");
                }
                List<CuotaModelo> tabla = cliente(req).simular(valor, n);
                req.setAttribute("cuotas", tabla);
                req.setAttribute("resumen", ResumenCredito.de(tabla));
            } catch (IllegalArgumentException ex) {
                mostrarMensaje(req, ex.getMessage(), true);
            } catch (RuntimeException ex) {
                if (requiereNuevoLogin(ex)) {
                    throw ex;
                }
                mostrarMensaje(req, mensajeDe(ex), true);
            }
        }
        mostrar("simulador", req, resp);
    }
}
