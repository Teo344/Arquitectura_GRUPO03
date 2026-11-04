package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.LineaCarrito;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.servicio.CreditoRestService;
import ec.edu.monster.servicio.FacturaRestService;
import ec.edu.monster.servicio.TelefonoRestService;
import ec.edu.monster.vista.VentaActivity;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Punto de venta (CU-07, CU-09 y CU-10): catálogo y venta en curso. Las reglas
 * son las de los clientes web y de escritorio: el vendedor ve el motivo por el
 * que no puede facturar y la API vuelve a validar todo (precios del catálogo,
 * reglas del banco).
 */
public class VentaControlador extends ControladorBase {

    private final VentaActivity vista;
    private final TelefonoRestService telefonos;
    private final CreditoRestService creditos;
    private final FacturaRestService facturas;
    private final SesionModelo sesion;
    private final CarritoModelo carrito;

    private List<CuotaModelo> simulacion;
    // Solo cuenta la última simulación pedida: al cambiar rápido las cuotas llegan respuestas viejas
    private int simulacionVigente;
    private BigDecimal montoSimulado;
    private int cuotasSimuladas;

    public VentaControlador(VentaActivity vista, TelefonoRestService telefonos, CreditoRestService creditos,
            FacturaRestService facturas, SesionModelo sesion, CarritoModelo carrito) {
        this.vista = vista;
        this.telefonos = telefonos;
        this.creditos = creditos;
        this.facturas = facturas;
        this.sesion = sesion;
        this.carrito = carrito;
    }

    public CarritoModelo getCarrito() {
        return carrito;
    }

    // ---------------------------------------------------------------- Catálogo

    /**
     * Carga el catálogo y actualiza el carrito con él: precios editados y
     * teléfonos dados de baja desde que se agregaron.
     */
    public void cargarCatalogo() {
        ejecutar(vista, vista::setCargandoCatalogo, () -> telefonos.listar(sesion.getToken()), catalogo -> {
            Map<Integer, TelefonoModelo> porCodigo = new LinkedHashMap<>();
            catalogo.forEach(t -> porCodigo.put(t.getCodTelefono(), t));
            List<String> retirados = carrito.sincronizar(porCodigo);
            vista.mostrarCatalogo(catalogo);
            ventaCambiada();
            if (!retirados.isEmpty()) {
                vista.mostrarMensaje("Se quitaron del carrito porque ya no están a la venta: "
                        + retirados.stream().collect(Collectors.joining(", ")) + ".", true);
            }
        });
    }

    // ---------------------------------------------------------------- Carrito

    public void agregar(TelefonoModelo telefono) {
        if (!telefono.isActivo()) {
            vista.mostrarMensaje(telefono.getNombre() + " ya no está a la venta.", true);
            return;
        }
        carrito.agregar(telefono);
        ventaCambiada();
    }

    /** Con 0 el teléfono sale de la venta; el máximo es 100 unidades. */
    public void cambiarCantidad(int codTelefono, int cantidad) {
        carrito.cambiarCantidad(codTelefono, cantidad);
        ventaCambiada();
    }

    public void quitar(int codTelefono) {
        carrito.quitar(codTelefono);
        ventaCambiada();
    }

    public void vaciar() {
        carrito.vaciar();
        vista.limpiarComprador();
        ventaCambiada();
    }

    // ---------------------------------------------------------------- Comprador (CU-07)

    /** Otra cédula invalida la evaluación anterior: hay que verificarla de nuevo antes de facturar. */
    public void cambiarCedula(String texto) {
        carrito.setCedula(texto);
        ventaCambiada();
    }

    /** Servicios 1 y 2 del banco: si es cliente, si es sujeto de crédito y su monto máximo. */
    public void verificarComprador(String texto) {
        carrito.setCedula(texto);
        final String cedula;
        try {
            cedula = Validacion.cedula(texto);
        } catch (IllegalArgumentException ex) {
            ventaCambiada();
            vista.mostrarMensaje(ex.getMessage(), true);
            return;
        }
        ventaCambiada();
        ejecutar(vista, vista::setVerificando, () -> creditos.evaluarCliente(sesion.getToken(), cedula), evaluacion -> {
            // Si mientras tanto se verificó otra cédula, esta respuesta ya no aplica
            if (!cedula.equals(carrito.getCedula())) {
                return;
            }
            carrito.setEvaluacion(evaluacion);
            ventaCambiada();
            if (!evaluacion.isCliente()) {
                vista.mostrarMensaje("La cédula " + cedula + " no es de un cliente de BanQuito: solo los clientes del"
                        + " banco pueden comprar.", true);
            }
        });
    }

    // ---------------------------------------------------------------- Forma de pago

    public void cambiarFormaPago(FormaPago formaPago) {
        carrito.setFormaPago(formaPago);
        ventaCambiada();
    }

    /** Entre 3 y 18 cuotas (RN-06); el modelo ajusta los valores fuera de rango. */
    public void cambiarCuotas(int numCuotas) {
        carrito.setNumCuotas(numCuotas);
        ventaCambiada();
    }

    // ---------------------------------------------------------------- Facturar (CU-09 y CU-10)

    /**
     * Lo que el vendedor debe corregir antes de facturar, o null si la venta
     * está lista. La vista lo muestra junto al botón deshabilitado.
     */
    public String motivoParaNoFacturar() {
        try {
            validarParaFacturar();
            return null;
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }

    /** Valida y pide a la vista la confirmación del vendedor antes de facturar. */
    public void preparar() {
        try {
            validarParaFacturar();
            vista.pedirConfirmacion();
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
        }
    }

    /** Solo se envían teléfonos y cantidades: la API toma los precios del catálogo y pide el crédito al banco. */
    public void facturar() {
        try {
            validarParaFacturar();
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
            return;
        }
        String cedula = carrito.getCedula();
        FormaPago formaPago = carrito.getFormaPago();
        int numCuotas = carrito.getNumCuotas();
        List<LineaCarrito> lineas = carrito.getLineas();
        ejecutar(vista, vista::setFacturando,
                () -> facturas.facturar(sesion.getToken(), cedula, formaPago, numCuotas, lineas),
                factura -> {
                    carrito.vaciar();
                    vista.limpiarComprador();
                    ventaCambiada();
                    vista.mostrarFacturaRegistrada(factura, mensajeRegistro(factura));
                });
    }

    /** Servicio 4 del banco: la tabla del crédito con que se pagó la factura. */
    public void verTabla(FacturaModelo factura) {
        ejecutar(vista, () -> creditos.cuotas(sesion.getToken(), factura.getCodCreditoBanco()), vista::mostrarTablaFactura);
    }

    private static String mensajeRegistro(FacturaModelo factura) {
        return "Factura N.º " + factura.getNumeroVisible() + " registrada"
                + (factura.isCredito() ? " con el crédito BanQuito N.º " + factura.getCodCreditoBanco() : "") + ".";
    }

    /** Lo que el vendedor puede corregir antes de llamar a la API (los mismos mensajes de los otros clientes). */
    private void validarParaFacturar() {
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

    // ---------------------------------------------------------------- Simulación

    /** Cuota de la última simulación, si corresponde a la venta actual. */
    public BigDecimal cuotaEstimada() {
        return simulacion == null || simulacion.isEmpty() ? null : simulacion.get(0).getValorCuota();
    }

    /**
     * Cada cambio de la venta se refleja en pantalla y, a crédito, pide la
     * cuota estimada. Es solo informativa: si el banco no responde, la venta
     * sigue.
     */
    private void ventaCambiada() {
        vista.actualizarVenta();
        if (!carrito.isCredito() || carrito.isVacio()) {
            simulacionVigente++;
            simulacion = null;
            montoSimulado = null;
            vista.mostrarSimulacion(null);
            return;
        }
        BigDecimal monto = carrito.getTotal();
        int cuotas = carrito.getNumCuotas();
        // Verificar al comprador no cambia el total ni las cuotas: la cuota pedida (o recibida) sigue valiendo
        if (montoSimulado != null && montoSimulado.compareTo(monto) == 0 && cuotasSimuladas == cuotas) {
            return;
        }
        int pedido = ++simulacionVigente;
        simulacion = null;
        montoSimulado = monto;
        cuotasSimuladas = cuotas;
        vista.mostrarSimulacionCalculando();
        enSegundoPlano(() -> {
            try {
                List<CuotaModelo> tabla = creditos.simular(sesion.getToken(), monto, cuotas);
                enPantalla(() -> {
                    if (pedido == simulacionVigente) {
                        simulacion = tabla;
                        vista.mostrarSimulacion(tabla);
                    }
                });
            } catch (RuntimeException ex) {
                enPantalla(() -> {
                    if (pedido != simulacionVigente) {
                        return;
                    }
                    // Con el siguiente cambio se vuelve a intentar
                    montoSimulado = null;
                    if (requiereNuevoLogin(ex)) {
                        vista.volverAlLogin(mensajeDe(ex));
                        return;
                    }
                    vista.mostrarSimulacionNoDisponible(mensajeDe(ex));
                });
            }
        });
    }
}
