package ec.edu.monster.controlador;

import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.vista.Formato;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mantenimiento del catálogo. GET /telefonos muestra el formulario y la lista
 * (?editar= carga un teléfono en el formulario, ?eliminar= pide confirmar la
 * baja); POST /telefonos/guardar crea o edita y POST /telefonos/eliminar da de
 * baja (el teléfono queda INACTIVO y las facturas antiguas no cambian).
 */
@WebServlet(urlPatterns = {"/telefonos", "/telefonos/*"})
public class TelefonoControlador extends ControladorBase {

    private static final int LARGO_MARCA = 50;
    private static final int LARGO_MODELO = 100;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        tomarAviso(req);
        Map<String, String> formulario = new HashMap<>();
        try {
            Integer editar = Validacion.entero(req.getParameter("editar"));
            if (editar != null) {
                TelefonoModelo telefono = cliente(req).telefono(editar);
                formulario.put("codTelefono", String.valueOf(telefono.getCodTelefono()));
                formulario.put("marca", telefono.getMarca());
                formulario.put("modelo", telefono.getModelo());
                formulario.put("precio", telefono.getPrecio().toPlainString());
            }
            Integer eliminar = Validacion.entero(req.getParameter("eliminar"));
            if (eliminar != null) {
                TelefonoModelo telefono = cliente(req).telefono(eliminar);
                req.setAttribute("porEliminar", telefono);
                req.setAttribute("filasEliminar", List.of(
                        new String[]{"Marca", telefono.getMarca()},
                        new String[]{"Modelo", telefono.getModelo()},
                        new String[]{"Precio", Formato.importe(telefono.getPrecio())}));
            }
        } catch (RuntimeException ex) {
            if (manejarSesion(ex, req, resp)) {
                return;
            }
            mostrarMensaje(req, mensajeDe(ex), true);
        }
        mostrarPantalla(req, resp, formulario);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String accion = req.getPathInfo() == null ? "" : req.getPathInfo();
        if ("/eliminar".equals(accion)) {
            eliminar(req, resp);
        } else if ("/guardar".equals(accion)) {
            guardar(req, resp);
        } else {
            redirigir("/telefonos", req, resp);
        }
    }

    private void guardar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String, String> formulario = new HashMap<>();
        for (String campo : new String[]{"codTelefono", "marca", "modelo", "precio"}) {
            formulario.put(campo, req.getParameter(campo) == null ? "" : req.getParameter(campo));
        }
        try {
            String marca = Validacion.texto(formulario.get("marca"), "la marca", LARGO_MARCA);
            String modelo = Validacion.texto(formulario.get("modelo"), "el modelo", LARGO_MODELO);
            BigDecimal precio = Validacion.importe(formulario.get("precio"), "precio");
            Integer codigo = Validacion.entero(formulario.get("codTelefono"));
            TelefonoModelo telefono = codigo == null
                    ? cliente(req).crearTelefono(marca, modelo, precio)
                    : cliente(req).actualizarTelefono(codigo, marca, modelo, precio);
            avisar(req, telefono.getNombre() + (codigo == null ? " se agregó al catálogo." : " se actualizó."), false);
            redirigir("/telefonos", req, resp);
        } catch (IllegalArgumentException ex) {
            mostrarMensaje(req, ex.getMessage(), true);
            mostrarPantalla(req, resp, formulario);
        } catch (RuntimeException ex) {
            if (manejarSesion(ex, req, resp)) {
                return;
            }
            mostrarMensaje(req, mensajeDe(ex), true);
            mostrarPantalla(req, resp, formulario);
        }
    }

    private void eliminar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer codigo = Validacion.entero(req.getParameter("codTelefono"));
        try {
            if (codigo != null) {
                TelefonoModelo telefono = cliente(req).telefono(codigo);
                cliente(req).eliminarTelefono(codigo);
                avisar(req, telefono.getNombre() + " se dio de baja: ya no aparece en el punto de venta.", false);
            }
        } catch (RuntimeException ex) {
            if (manejarSesion(ex, req, resp)) {
                return;
            }
            avisar(req, mensajeDe(ex), true);
        }
        redirigir("/telefonos", req, resp);
    }

    private void mostrarPantalla(HttpServletRequest req, HttpServletResponse resp, Map<String, String> formulario)
            throws ServletException, IOException {
        try {
            req.setAttribute("telefonos", cliente(req).telefonos());
        } catch (RuntimeException ex) {
            if (manejarSesion(ex, req, resp)) {
                return;
            }
            mostrarMensaje(req, mensajeDe(ex), true);
            req.setAttribute("telefonos", List.of());
        }
        req.setAttribute("formulario", formulario);
        mostrar("telefonos", req, resp);
    }

    private boolean manejarSesion(RuntimeException ex, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (requiereNuevoLogin(ex)) {
            volverAlLogin(mensajeDe(ex) + ". Inicia sesión nuevamente.", true, req, resp);
            return true;
        }
        return false;
    }
}
