package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Categoria;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/** Consulta categorías y unidades mediante los endpoints REST protegidos. */
@WebServlet("/catalogo")
public class CatalogoController extends ControladorBase {

    /** Sin parámetro muestra las categorías; con ?categoria=CODIGO también sus unidades. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = sesion(req).token();
        req.setAttribute("estado", "El catálogo se consultará al servidor.");
        try {
            List<Categoria> categorias = api().categorias(token);
            req.setAttribute("categorias", categorias);
            req.setAttribute("estado", "Se cargaron " + categorias.size() + " categorías desde el servidor.");

            Categoria categoria = categorias.stream()
                    .filter(c -> Objects.equals(c.codigo(), req.getParameter("categoria")))
                    .findFirst().orElse(null);
            if (categoria != null) {
                req.setAttribute("seleccionada", categoria);
                req.setAttribute("unidades", api().unidades(token, categoria.codigo()));
                req.setAttribute("estado", "Unidades consultadas: " + categoria.nombre() + ".");
            }
        } catch (Exception e) {
            if (sesionVencida(e)) {
                req.getSession().invalidate();
                redirigir("/login?expirada", req, resp);
                return;
            }
            req.setAttribute("estado", mensajeError(e, "No se pudo consultar el catálogo REST."));
            req.setAttribute("estadoError", true);
        }
        mostrar("catalogo", req, resp);
    }
}
