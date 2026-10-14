package ec.edu.monster.controlador;

import ec.edu.monster.servicio.Categoria;
import ec.edu.monster.servicio.ConversionSoapService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/** Controla la consulta del catálogo y de unidades mediante el servicio SOAP protegido. */
@WebServlet("/catalogo")
public class CatalogoController extends ControladorBase {
    private final ConversionSoapService servicio = new ConversionSoapService();

    /** Sin parámetro muestra las categorías; con ?categoria=CODIGO también sus unidades. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String token = sesion(req).getToken();
        req.setAttribute("estado", "El catálogo se consultará al servidor.");
        try {
            List<Categoria> categorias = servicio.consultarCatalogo(token);
            req.setAttribute("categorias", categorias);
            req.setAttribute("estado", "Se cargaron " + categorias.size() + " categorías desde el servidor.");

            String codigo = req.getParameter("categoria");
            Categoria categoria = categorias.stream()
                    .filter(c -> Objects.equals(c.getCodigo(), codigo))
                    .findFirst().orElse(null);
            if (categoria != null) {
                req.setAttribute("seleccionada", categoria);
                req.setAttribute("unidades", servicio.consultarUnidades(token, categoria.getCodigo()));
                req.setAttribute("estado", "Unidades consultadas: " + categoria.getNombre() + ".");
            }
        } catch (Exception ex) {
            if (sesionVencida(ex)) {
                req.getSession().invalidate();
                redirigir("/login?expirada", req, resp);
                return;
            }
            req.setAttribute("estado", mensajeError(ex, "No se pudo consultar el catálogo SOAP."));
            req.setAttribute("estadoError", true);
        }
        mostrar("catalogo", req, resp);
    }
}
