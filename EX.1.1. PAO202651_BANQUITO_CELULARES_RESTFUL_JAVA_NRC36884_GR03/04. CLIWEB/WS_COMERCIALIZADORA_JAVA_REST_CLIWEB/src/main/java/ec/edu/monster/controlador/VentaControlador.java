package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.Formato;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Punto de venta: catálogo a la izquierda y carrito a la derecha. El carrito
 * vive en la sesión; cada acción es un POST que redirige otra vez a GET /venta
 * (Post/Redirect/Get). Facturar tiene dos pasos, como las transacciones de
 * EurekaBank: /venta/preparar valida y pide confirmación, /venta/facturar
 * llama a la API.
 */
@WebServlet(urlPatterns = {"/venta", "/venta/*"})
public class VentaControlador extends ControladorBase {

    private static final String CARRITO = "venta.carrito";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        tomarAviso(req);
        mostrarPantalla(req, resp, false);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accion = req.getPathInfo() == null ? "" : req.getPathInfo();
        CarritoModelo carrito = carrito(req.getSession());
        String volver = "/venta" + busqueda(req);
        try {
            switch (accion) {
                case "/agregar" -> {
                    Integer codigo = Validacion.entero(req.getParameter("codTelefono"));
                    if (codigo != null) {
                        TelefonoModelo telefono = cliente(req).telefono(codigo);
                        if (!telefono.isActivo()) {
                            throw new IllegalArgumentException(telefono.getNombre() + " ya no está a la venta.");
                        }
                        carrito.agregar(telefono);
                    }
                    volver += "#producto-" + codigo;
                }
                case "/cantidad" -> {
                    Integer codigo = Validacion.entero(req.getParameter("codTelefono"));
                    Integer cantidad = Validacion.entero(req.getParameter("cantidad"));
                    if (codigo != null && cantidad != null) {
                        carrito.cambiarCantidad(codigo, cantidad);
                    }
                    volver += "#carrito";
                }
                case "/quitar" -> {
                    Integer codigo = Validacion.entero(req.getParameter("codTelefono"));
                    if (codigo != null) {
                        carrito.quitar(codigo);
                    }
                    volver += "#carrito";
                }
                case "/comprador" -> {
                    carrito.setCedula(req.getParameter("cedula"));
                    String cedula = Validacion.cedula(req.getParameter("cedula"));
                    EvaluacionClienteModelo evaluacion = cliente(req).evaluarCliente(cedula);
                    carrito.setEvaluacion(evaluacion);
                    if (!evaluacion.isCliente()) {
                        avisar(req, "La cédula " + cedula + " no es de un cliente de BanQuito: solo los clientes del banco"
                                + " pueden comprar.", true);
                    }
                    volver += "#carrito";
                }
                case "/pago" -> {
                    carrito.setFormaPago("CREDITO".equals(req.getParameter("formaPago")) ? FormaPago.CREDITO : FormaPago.EFECTIVO);
                    Integer cuotas = Validacion.entero(req.getParameter("numCuotas"));
                    if (cuotas != null) {
                        carrito.setNumCuotas(cuotas);
                    }
                    volver += "#carrito";
                }
                case "/vaciar" -> carrito.vaciar();
                case "/preparar" -> {
                    validarParaFacturar(carrito);
                    mostrarPantalla(req, resp, true);
                    return;
                }
                case "/facturar" -> {
                    facturar(req, resp, carrito);
                    return;
                }
                default -> {
                    // Ruta desconocida: se vuelve al punto de venta
                }
            }
        } catch (IllegalArgumentException ex) {
            avisar(req, ex.getMessage(), true);
        } catch (RuntimeException ex) {
            if (requiereNuevoLogin(ex)) {
                volverAlLogin(mensajeDe(ex) + ". Inicia sesión nuevamente.", true, req, resp);
                return;
            }
            avisar(req, mensajeDe(ex), true);
        }
        redirigir(volver, req, resp);
    }

    /**
     * La API vuelve a validar todo (precios del catálogo, reglas del banco):
     * si rechaza la venta, el carrito se conserva y se muestra el motivo.
     */
    private void facturar(HttpServletRequest req, HttpServletResponse resp, CarritoModelo carrito) throws IOException {
        try {
            validarParaFacturar(carrito);
            FacturaModelo factura = cliente(req).facturar(carrito.getCedula(), carrito.getFormaPago(),
                    carrito.getNumCuotas(), carrito.getLineas());
            carrito.vaciar();
            avisar(req, "Factura N.º " + factura.getNumeroVisible() + " registrada"
                    + (factura.isCredito() ? " con el crédito BanQuito N.º " + factura.getCodCreditoBanco() : "") + ".", false);
            redirigir("/facturas/" + factura.getNumFactura(), req, resp);
        } catch (IllegalArgumentException ex) {
            avisar(req, ex.getMessage(), true);
            redirigir("/venta#carrito", req, resp);
        } catch (RuntimeException ex) {
            if (requiereNuevoLogin(ex)) {
                volverAlLogin(mensajeDe(ex) + ". Inicia sesión nuevamente.", true, req, resp);
                return;
            }
            avisar(req, mensajeDe(ex), true);
            redirigir("/venta#carrito", req, resp);
        }
    }

    /** Lo que el vendedor puede corregir antes de llamar a la API. */
    private static void validarParaFacturar(CarritoModelo carrito) {
        if (carrito.isVacio()) {
            throw new IllegalArgumentException("Agrega al menos un teléfono del catálogo.");
        }
        EvaluacionClienteModelo evaluacion = carrito.getEvaluacion();
        if (evaluacion == null) {
            throw new IllegalArgumentException("Verifica la cédula del comprador en BanQuito antes de facturar.");
        }
        if (!evaluacion.isCliente()) {
            throw new IllegalArgumentException("El comprador debe ser cliente de BanQuito.");
        }
        if (carrito.isCredito() && !evaluacion.isSujetoCredito()) {
            throw new IllegalArgumentException("El comprador no es sujeto de crédito: "
                    + evaluacion.getMotivos().stream().map(m -> m.getDescripcion().toLowerCase(Locale.ROOT))
                            .collect(Collectors.joining(", ")) + ". Puede pagar en efectivo.");
        }
        if (carrito.isSuperaMontoMaximo()) {
            throw new IllegalArgumentException("El total supera el monto máximo de crédito del comprador. Quita"
                    + " teléfonos o cambia a pago en efectivo.");
        }
    }

    private void mostrarPantalla(HttpServletRequest req, HttpServletResponse resp, boolean confirmar)
            throws ServletException, IOException {
        ClienteRESTMonsterCell clienteRest = cliente(req);
        CarritoModelo carrito = carrito(req.getSession());
        String busqueda = req.getParameter("q") == null ? "" : req.getParameter("q").trim();
        try {
            List<TelefonoModelo> catalogo = clienteRest.telefonos();
            Map<Integer, TelefonoModelo> porCodigo = new LinkedHashMap<>();
            catalogo.forEach(t -> porCodigo.put(t.getCodTelefono(), t));
            List<String> retirados = carrito.sincronizar(porCodigo);
            if (!retirados.isEmpty()) {
                mostrarMensaje(req, "Se quitaron del carrito porque ya no están a la venta: "
                        + String.join(", ", retirados) + ".", true);
            }
            String filtro = busqueda.toLowerCase(Locale.ROOT);
            req.setAttribute("catalogo", catalogo.stream()
                    .filter(t -> filtro.isEmpty() || t.getNombre().toLowerCase(Locale.ROOT).contains(filtro))
                    .toList());
            req.setAttribute("totalCatalogo", catalogo.size());
            if (carrito.isCredito() && !carrito.isVacio()) {
                simular(req, clienteRest, carrito);
            }
        } catch (RuntimeException ex) {
            if (requiereNuevoLogin(ex)) {
                volverAlLogin(mensajeDe(ex) + ". Inicia sesión nuevamente.", true, req, resp);
                return;
            }
            mostrarMensaje(req, mensajeDe(ex), true);
            req.setAttribute("catalogo", List.of());
        }
        req.setAttribute("q", busqueda);
        req.setAttribute("carrito", carrito);
        if (confirmar) {
            req.setAttribute("resumenFactura", resumen(carrito, (BigDecimal) req.getAttribute("cuotaEstimada")));
        }
        mostrar("venta", req, resp);
    }

    /** Filas del diálogo "Confirmar venta"; la última (el total) va destacada. */
    private static List<String[]> resumen(CarritoModelo carrito, BigDecimal cuotaEstimada) {
        List<String[]> filas = new ArrayList<>();
        filas.add(new String[]{"Comprador", carrito.getEvaluacion().getNombre()});
        filas.add(new String[]{"Cédula", carrito.getCedula()});
        filas.add(new String[]{"Teléfonos", carrito.getArticulos() + (carrito.getArticulos() == 1 ? " unidad" : " unidades")});
        if (carrito.isCredito()) {
            filas.add(new String[]{"Forma de pago", "Crédito BanQuito a " + carrito.getNumCuotas() + " cuotas"});
            if (cuotaEstimada != null) {
                filas.add(new String[]{"Cuota mensual", Formato.importe(cuotaEstimada)});
            }
        } else {
            filas.add(new String[]{"Forma de pago", "Efectivo (42 % de descuento)"});
            filas.add(new String[]{"Descuento", "− " + Formato.importe(carrito.getDescuento())});
        }
        filas.add(new String[]{"Total a facturar", Formato.importe(carrito.getTotal())});
        return filas;
    }

    /**
     * Cuota estimada del crédito y, si se pidió "Ver tabla", la tabla completa.
     * Es solo informativa: si el banco no responde, la venta sigue.
     */
    private static void simular(HttpServletRequest req, ClienteRESTMonsterCell clienteRest, CarritoModelo carrito) {
        try {
            List<CuotaModelo> tabla = clienteRest.simular(carrito.getTotal(), carrito.getNumCuotas());
            if (!tabla.isEmpty()) {
                req.setAttribute("cuotaEstimada", tabla.get(0).getValorCuota());
            }
            if (req.getParameter("tabla") != null) {
                req.setAttribute("tablaSimulacion", tabla);
            }
        } catch (RuntimeException ex) {
            if (requiereNuevoLogin(ex)) {
                throw ex;
            }
            req.setAttribute("simulacionNoDisponible", mensajeDe(ex));
        }
    }

    private static CarritoModelo carrito(HttpSession sesion) {
        CarritoModelo carrito = (CarritoModelo) sesion.getAttribute(CARRITO);
        if (carrito == null) {
            carrito = new CarritoModelo();
            sesion.setAttribute(CARRITO, carrito);
        }
        return carrito;
    }

    /** Conserva la búsqueda del catálogo al volver de una acción. */
    private static String busqueda(HttpServletRequest req) {
        String q = req.getParameter("q");
        return q == null || q.isBlank() ? "" : "?q=" + URLEncoder.encode(q.trim(), StandardCharsets.UTF_8);
    }
}
