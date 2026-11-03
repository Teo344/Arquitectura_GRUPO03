package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.modelo.TransaccionModelo;
import ec.edu.monster.servicio.ClienteRESTEureka;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Depósitos (RQF-005), retiros (RQF-006) y transferencias (RQF-007). Valida los
 * datos antes de enviarlos para orientar al empleado; el servidor vuelve a
 * validar todas las reglas de negocio.
 *
 * Igual que en escritorio son dos pasos: POST /transacciones/preparar valida y
 * pide la confirmación; POST /transacciones/registrar ejecuta la operación que
 * quedó guardada en la sesión (no la que llegue en el formulario) y redirige a
 * GET /transacciones, así recargar la página no la repite.
 */
@WebServlet(urlPatterns = {"/transacciones", "/transacciones/preparar", "/transacciones/registrar"})
public class TransaccionControlador extends ControladorBase {

    // Mayor valor que admite DECIMAL(12,2) en el servidor (RN-01)
    private static final BigDecimal IMPORTE_MAXIMO = new BigDecimal("9999999999.99");

    private static final String TIPO = "transaccion.tipo";
    private static final String FORMULARIO = "transaccion.formulario";
    private static final String PENDIENTE = "transaccion.pendiente";
    private static final String REGISTRADA = "transaccion.registrada";
    private static final String RESULTADO = "transaccion.resultado";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession sesion = req.getSession();
        TipoTransaccion tipo = leerTipo(req.getParameter("tipo"), sesion);
        if (tipo != sesion.getAttribute(TIPO)) {
            cambiarTipo(sesion, tipo);
        }
        // Volver a la página (Cancelar o Esc) descarta la confirmación abierta
        sesion.removeAttribute(PENDIENTE);
        tomarAviso(req);
        mostrarPantalla(req, resp, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/transacciones/registrar".equals(req.getServletPath())) {
            registrar(req, resp);
        } else if ("/transacciones/preparar".equals(req.getServletPath())) {
            preparar(req, resp);
        } else {
            redirigir("/transacciones", req, resp);
        }
    }

    /**
     * Valida los datos escritos y, si son correctos, pide la confirmación del
     * empleado antes de registrar nada.
     */
    private void preparar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession sesion = req.getSession();
        TipoTransaccion tipo = leerTipo(req.getParameter("tipo"), sesion);
        if (tipo != sesion.getAttribute(TIPO)) {
            cambiarTipo(sesion, tipo);
        }
        Map<String, String> formulario = formulario(sesion);
        formulario.put("cuenta", valor(req.getParameter("cuenta")));
        formulario.put("cuentaDestino", valor(req.getParameter("cuentaDestino")));
        formulario.put("importe", valor(req.getParameter("importe")));
        try {
            TransaccionModelo modelo = new TransaccionModelo(tipo, formulario.get("cuenta"),
                    tipo == TipoTransaccion.TRANSFERENCIA ? formulario.get("cuentaDestino") : null,
                    leerImporte(formulario.get("importe")));
            validar(modelo);
            sesion.setAttribute(PENDIENTE, modelo);
            mostrarPantalla(req, resp, modelo);
        } catch (IllegalArgumentException ex) {
            sesion.removeAttribute(PENDIENTE);
            mostrarMensaje(req, ex.getMessage(), true);
            mostrarPantalla(req, resp, null);
        }
    }

    private void registrar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession sesion = req.getSession();
        TransaccionModelo modelo = (TransaccionModelo) sesion.getAttribute(PENDIENTE);
        sesion.removeAttribute(PENDIENTE);
        if (modelo == null) {
            redirigir("/transacciones", req, resp);
            return;
        }
        String destino = "/transacciones?tipo=" + modelo.getTipo().name();
        try {
            validar(modelo);
        } catch (IllegalArgumentException ex) {
            avisar(req, ex.getMessage(), true);
            redirigir(destino, req, resp);
            return;
        }
        ClienteRESTEureka clienteRest = cliente(req);
        String cuenta = modelo.getCuenta().trim();
        try {
            ResultadoTransaccionModelo resultado = switch (modelo.getTipo()) {
                case DEPOSITO -> clienteRest.depositar(cuenta, modelo.getImporte());
                case RETIRO -> clienteRest.retirar(cuenta, modelo.getImporte());
                case TRANSFERENCIA -> clienteRest.transferir(cuenta, modelo.getCuentaDestino().trim(),
                        modelo.getImporte());
            };
            sesion.setAttribute(REGISTRADA, modelo);
            sesion.setAttribute(RESULTADO, resultado);
            // Como TransaccionVista.mostrarResultado: se conservan las cuentas y se borra el importe
            formulario(sesion).put("importe", "");
        } catch (RuntimeException ex) {
            if (requiereNuevoLogin(ex)) {
                volverAlLogin(mensajeDe(ex) + ". Inicia sesión nuevamente.", true, req, resp);
                return;
            }
            avisar(req, mensajeDe(ex), true);
        }
        redirigir(destino, req, resp);
    }

    private void mostrarPantalla(HttpServletRequest req, HttpServletResponse resp, TransaccionModelo pendiente)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession();
        req.setAttribute("tipo", sesion.getAttribute(TIPO));
        req.setAttribute("tipos", TipoTransaccion.values());
        req.setAttribute("formulario", formulario(sesion));
        req.setAttribute("registrada", sesion.getAttribute(REGISTRADA));
        req.setAttribute("resultado", sesion.getAttribute(RESULTADO));
        req.setAttribute("pendiente", pendiente);
        mostrar("transacciones", req, resp);
    }

    /** Como TransaccionVista.cambiarTipo: otro tipo de operación oculta el comprobante anterior. */
    private static void cambiarTipo(HttpSession sesion, TipoTransaccion tipo) {
        sesion.setAttribute(TIPO, tipo);
        sesion.removeAttribute(REGISTRADA);
        sesion.removeAttribute(RESULTADO);
    }

    private static TipoTransaccion leerTipo(String texto, HttpSession sesion) {
        if (texto != null) {
            for (TipoTransaccion tipo : TipoTransaccion.values()) {
                if (tipo.name().equalsIgnoreCase(texto.trim())) {
                    return tipo;
                }
            }
        }
        return sesion.getAttribute(TIPO) instanceof TipoTransaccion actual ? actual : TipoTransaccion.DEPOSITO;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> formulario(HttpSession sesion) {
        Map<String, String> formulario = (Map<String, String>) sesion.getAttribute(FORMULARIO);
        if (formulario == null) {
            formulario = new HashMap<>();
            sesion.setAttribute(FORMULARIO, formulario);
        }
        return formulario;
    }

    private static String valor(String texto) {
        return texto == null ? "" : texto;
    }

    /** Al volver al menú la pantalla de transacciones se cierra y olvida lo que mostraba. */
    static void limpiar(HttpSession sesion) {
        sesion.removeAttribute(TIPO);
        sesion.removeAttribute(FORMULARIO);
        sesion.removeAttribute(PENDIENTE);
        sesion.removeAttribute(REGISTRADA);
        sesion.removeAttribute(RESULTADO);
    }

    /** RN-01 y RN-04, con los mismos mensajes que los clientes móvil y de escritorio. */
    private static void validar(TransaccionModelo modelo) {
        if (modelo == null || modelo.getTipo() == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de transacción.");
        }
        if (modelo.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            String origen = CuentaControlador.validarCuenta(modelo.getCuenta(), "La cuenta de origen");
            String destino = CuentaControlador.validarCuenta(modelo.getCuentaDestino(), "La cuenta de destino");
            if (origen.equals(destino)) {
                throw new IllegalArgumentException("La cuenta de destino debe ser diferente de la cuenta de origen.");
            }
        } else {
            CuentaControlador.validarCuenta(modelo.getCuenta(), "La cuenta");
        }
        BigDecimal importe = modelo.getImporte();
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("El importe debe ser mayor que cero y tener como máximo dos decimales.");
        }
        if (importe.compareTo(IMPORTE_MAXIMO) > 0) {
            throw new IllegalArgumentException("El importe no puede superar 9999999999.99.");
        }
    }

    /** Acepta coma o punto decimal; el dinero se maneja siempre con BigDecimal. */
    private static BigDecimal leerImporte(String texto) {
        String valor = texto == null ? "" : texto.trim().replace(',', '.');
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el importe de la operación.");
        }
        try {
            return new BigDecimal(valor);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingresa un importe numérico válido, por ejemplo 150.50");
        }
    }
}
