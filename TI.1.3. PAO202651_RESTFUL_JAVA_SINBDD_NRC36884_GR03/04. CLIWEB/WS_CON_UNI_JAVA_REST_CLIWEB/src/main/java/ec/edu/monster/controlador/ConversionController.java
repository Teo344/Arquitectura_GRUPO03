package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.RegistroConversion;
import ec.edu.monster.modelo.Unidad;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Coordina carga del catálogo y conversión de unidades. */
@WebServlet("/conversion")
public class ConversionController extends ControladorBase {
    static final String ATRIBUTO_HISTORIAL = "historial";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    /** Muestra el formulario; cambiar de categoría vuelve a pedir las unidades. */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        atender(req, resp, false);
    }

    /** Convierte con los datos del formulario. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        atender(req, resp, true);
    }

    private void atender(HttpServletRequest req, HttpServletResponse resp, boolean convertir)
            throws ServletException, IOException {
        String token = sesion(req).token();
        req.setAttribute("valor", req.getParameter("valor") == null ? "0" : req.getParameter("valor"));
        req.setAttribute(ATRIBUTO_HISTORIAL, historial(req));
        estado(req, "Catálogo cargado.", false);
        try {
            List<Categoria> categorias = api().categorias(token).stream().filter(Categoria::convertible).toList();
            Categoria categoria = categorias.stream()
                    .filter(c -> Objects.equals(c.codigo(), req.getParameter("categoria")))
                    .findFirst().orElse(categorias.isEmpty() ? null : categorias.get(0));
            List<Unidad> unidades = categoria == null ? List.of() : api().unidades(token, categoria.codigo());
            Unidad origen = buscar(unidades, req.getParameter("origen"), 0);
            Unidad destino = buscar(unidades, req.getParameter("destino"), 1);
            req.setAttribute("categorias", categorias);
            req.setAttribute("categoria", categoria);
            req.setAttribute("unidades", unidades);
            req.setAttribute("origen", origen);
            req.setAttribute("destino", destino);
            if (convertir) {
                convertir(req, token, categoria, origen, destino);
            }
        } catch (Exception e) {
            if (sesionVencida(e)) {
                req.getSession().invalidate();
                redirigir("/login?expirada", req, resp);
                return;
            }
            estado(req, mensajeError(e, "Error al comunicarse con el servicio."), true);
        }
        mostrar("conversion", req, resp);
    }

    private void convertir(HttpServletRequest req, String token, Categoria c, Unidad o, Unidad d) throws Exception {
        if (c == null || o == null || d == null) {
            estado(req, "Selecciona categoría y unidades.", true);
            return;
        }
        final double n;
        try {
            n = Double.parseDouble(req.getParameter("valor").trim().replace(',', '.'));
            if (!Double.isFinite(n)) throw new NumberFormatException();
        } catch (NumberFormatException | NullPointerException e) {
            estado(req, "Ingresa un valor numérico válido.", true);
            return;
        }
        String resultado = formatear(api().convertir(token, c.codigo(), n, o.codigo(), d.codigo()));
        historial(req).add(0, new RegistroConversion(LocalDateTime.now().format(FORMATO_FECHA),
                formatear(n) + " " + o.simbolo() + " → " + resultado + " " + d.simbolo()));
        estado(req, formatear(n) + " " + o.simbolo() + " = " + resultado + " " + d.simbolo(), false);
    }

    @SuppressWarnings("unchecked")
    private List<RegistroConversion> historial(HttpServletRequest req) {
        List<RegistroConversion> historial = (List<RegistroConversion>) req.getSession().getAttribute(ATRIBUTO_HISTORIAL);
        if (historial == null) {
            historial = new ArrayList<>();
            req.getSession().setAttribute(ATRIBUTO_HISTORIAL, historial);
        }
        return historial;
    }

    /** La unidad pedida o, como los combos de escritorio, la de la posición indicada (destino: la segunda). */
    private static Unidad buscar(List<Unidad> unidades, String codigo, int posicion) {
        return unidades.stream().filter(u -> Objects.equals(u.codigo(), codigo)).findFirst()
                .orElse(unidades.isEmpty() ? null : unidades.get(Math.min(posicion, unidades.size() - 1)));
    }

    private static void estado(HttpServletRequest req, String texto, boolean error) {
        req.setAttribute("estado", texto);
        req.setAttribute("estadoError", error);
    }

    private static String formatear(double n) {
        return new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.US)).format(n);
    }
}
