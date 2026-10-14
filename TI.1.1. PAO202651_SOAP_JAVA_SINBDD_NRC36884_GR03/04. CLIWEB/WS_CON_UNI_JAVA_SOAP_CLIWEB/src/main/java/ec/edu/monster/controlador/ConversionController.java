package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RegistroConversion;
import ec.edu.monster.servicio.Categoria;
import ec.edu.monster.servicio.ConversionSoapService;
import ec.edu.monster.servicio.Respuesta;
import ec.edu.monster.servicio.Unidad;
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

/** Controla la carga de catálogo y las conversiones solicitadas desde la vista. */
@WebServlet("/conversion")
public class ConversionController extends ControladorBase {
    static final String ATRIBUTO_HISTORIAL = "historial";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final ConversionSoapService servicio = new ConversionSoapService();

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
        String token = sesion(req).getToken();
        req.setAttribute("valor", req.getParameter("valor") == null ? "0" : req.getParameter("valor"));
        req.setAttribute(ATRIBUTO_HISTORIAL, historial(req));
        mostrarEstado(req, "Selecciona una categoría para comenzar.", false);
        try {
            List<Categoria> categorias = servicio.consultarCategorias(token);
            Categoria categoria = buscarCategoria(categorias, req.getParameter("categoria"));
            List<Unidad> unidades = categoria == null ? List.of()
                    : servicio.consultarUnidades(token, categoria.getCodigo());
            Unidad origen = buscarUnidad(unidades, req.getParameter("origen"));
            Unidad destino = buscarUnidad(unidades, req.getParameter("destino"));
            req.setAttribute("categorias", categorias);
            req.setAttribute("categoria", categoria);
            req.setAttribute("unidades", unidades);
            req.setAttribute("origen", origen);
            req.setAttribute("destino", destino);
            if (convertir) {
                convertir(req, token, categoria, origen, destino);
            }
        } catch (Exception ex) {
            if (sesionVencida(ex)) {
                req.getSession().invalidate();
                redirigir("/login?expirada", req, resp);
                return;
            }
            mostrarEstado(req, mensajeError(ex, "No se pudo completar la operación SOAP."), true);
        }
        mostrar("conversion", req, resp);
    }

    private void convertir(HttpServletRequest req, String token, Categoria categoria, Unidad origen, Unidad destino)
            throws Exception {
        if (categoria == null || origen == null || destino == null) {
            mostrarEstado(req, "No hay categoría o unidades disponibles.", true);
            return;
        }
        final double valor;
        try {
            valor = Double.parseDouble(req.getParameter("valor").trim().replace(',', '.'));
            if (!Double.isFinite(valor)) throw new NumberFormatException();
        } catch (NumberFormatException | NullPointerException ex) {
            mostrarEstado(req, "Ingresa un valor numérico válido.", true);
            return;
        }

        Respuesta respuesta = servicio.convertir(token, categoria.getCodigo(), valor,
                origen.getCodigo(), destino.getCodigo());
        if (respuesta.getResultado() == null) {
            mostrarEstado(req, "El servidor no devolvió un resultado.", true);
            return;
        }
        String resultado = formatear(respuesta.getResultado());
        historial(req).add(0, new RegistroConversion(LocalDateTime.now().format(FORMATO_FECHA),
                formatear(valor) + " " + origen.getSimbolo() + " → " + resultado + " " + destino.getSimbolo()));
        mostrarEstado(req, "Resultado: " + resultado + " " + destino.getSimbolo(), false);
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

    /** La categoría pedida o, como el combo de escritorio, la primera. */
    private static Categoria buscarCategoria(List<Categoria> categorias, String codigo) {
        return categorias.stream().filter(c -> Objects.equals(c.getCodigo(), codigo)).findFirst()
                .orElse(categorias.isEmpty() ? null : categorias.get(0));
    }

    private static Unidad buscarUnidad(List<Unidad> unidades, String codigo) {
        return unidades.stream().filter(u -> Objects.equals(u.getCodigo(), codigo)).findFirst()
                .orElse(unidades.isEmpty() ? null : unidades.get(0));
    }

    private static void mostrarEstado(HttpServletRequest req, String texto, boolean error) {
        req.setAttribute("estado", texto);
        req.setAttribute("estadoError", error);
    }

    private static String formatear(double valor) {
        DecimalFormat formato = new DecimalFormat("0.########", DecimalFormatSymbols.getInstance(Locale.US));
        return formato.format(valor);
    }
}
