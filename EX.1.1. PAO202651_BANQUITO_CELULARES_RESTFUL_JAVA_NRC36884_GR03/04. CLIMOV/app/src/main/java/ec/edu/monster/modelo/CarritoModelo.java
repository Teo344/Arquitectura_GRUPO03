package ec.edu.monster.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Venta en curso del punto de venta; vive en el MenuControlador mientras dura
 * la sesión y viaja a la pantalla de venta. Los totales son una vista previa para el vendedor: al facturar, el
 * servidor toma los precios del catálogo y vuelve a calcular todo.
 */
public class CarritoModelo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Descuento por pago en efectivo, el mismo del servidor (factura.descuentoEfectivo). */
    public static final BigDecimal PORCENTAJE_EFECTIVO = new BigDecimal("42");
    public static final int CUOTAS_MINIMO = 3;
    public static final int CUOTAS_MAXIMO = 18;
    public static final int CANTIDAD_MAXIMA = 100;
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final Map<Integer, LineaCarrito> lineas = new LinkedHashMap<>();
    private String cedula = "";
    private EvaluacionClienteModelo evaluacion;
    private FormaPago formaPago = FormaPago.EFECTIVO;
    private int numCuotas = 6;

    public List<LineaCarrito> getLineas() {
        return new ArrayList<>(lineas.values());
    }

    public boolean isVacio() {
        return lineas.isEmpty();
    }

    public int getArticulos() {
        return lineas.values().stream().mapToInt(LineaCarrito::getCantidad).sum();
    }

    /** Unidades de cada teléfono en la venta (código → cantidad), para marcar el catálogo. */
    public Map<Integer, Integer> getCantidades() {
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        lineas.forEach((codigo, linea) -> cantidades.put(codigo, linea.getCantidad()));
        return cantidades;
    }

    /** Suma una unidad; un teléfono que ya está en el carrito no se repite. */
    public void agregar(TelefonoModelo telefono) {
        LineaCarrito linea = lineas.get(telefono.getCodTelefono());
        if (linea == null) {
            lineas.put(telefono.getCodTelefono(), new LineaCarrito(telefono, 1));
        } else if (linea.getCantidad() < CANTIDAD_MAXIMA) {
            linea.setCantidad(linea.getCantidad() + 1);
        }
    }

    /** Cantidad entre 1 y 100; con 0 o menos el teléfono sale del carrito. */
    public void cambiarCantidad(int codTelefono, int cantidad) {
        LineaCarrito linea = lineas.get(codTelefono);
        if (linea == null) {
            return;
        }
        if (cantidad <= 0) {
            lineas.remove(codTelefono);
        } else {
            linea.setCantidad(Math.min(cantidad, CANTIDAD_MAXIMA));
        }
    }

    public void quitar(int codTelefono) {
        lineas.remove(codTelefono);
    }

    /**
     * Actualiza las líneas con el catálogo vigente (precios editados) y quita
     * los teléfonos que se dieron de baja. Devuelve los nombres que salieron.
     */
    public List<String> sincronizar(Map<Integer, TelefonoModelo> catalogo) {
        List<String> retirados = new ArrayList<>();
        for (LineaCarrito linea : new ArrayList<>(lineas.values())) {
            int codigo = linea.getTelefono().getCodTelefono();
            TelefonoModelo vigente = catalogo.get(codigo);
            if (vigente == null) {
                lineas.remove(codigo);
                retirados.add(linea.getTelefono().getNombre());
            } else {
                lineas.put(codigo, new LineaCarrito(vigente, linea.getCantidad()));
            }
        }
        return retirados;
    }

    public BigDecimal getSubtotal() {
        return lineas.values().stream().map(LineaCarrito::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getDescuento() {
        if (formaPago != FormaPago.EFECTIVO) {
            return BigDecimal.ZERO.setScale(2);
        }
        return getSubtotal().multiply(PORCENTAJE_EFECTIVO).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return getSubtotal().subtract(getDescuento());
    }

    public String getCedula() {
        return cedula;
    }

    /** Otra cédula invalida la evaluación anterior del banco. */
    public void setCedula(String cedula) {
        String nueva = cedula == null ? "" : cedula.trim();
        if (!nueva.equals(this.cedula)) {
            evaluacion = null;
        }
        this.cedula = nueva;
    }

    public EvaluacionClienteModelo getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(EvaluacionClienteModelo evaluacion) {
        this.evaluacion = evaluacion;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public boolean isCredito() {
        return formaPago == FormaPago.CREDITO;
    }

    public int getNumCuotas() {
        return numCuotas;
    }

    public void setNumCuotas(int numCuotas) {
        this.numCuotas = Math.max(CUOTAS_MINIMO, Math.min(CUOTAS_MAXIMO, numCuotas));
    }

    /** Para el selector de cuotas de la vista: 3, 4, ..., 18. */
    public List<Integer> getOpcionesCuotas() {
        List<Integer> opciones = new ArrayList<>();
        for (int n = CUOTAS_MINIMO; n <= CUOTAS_MAXIMO; n++) {
            opciones.add(n);
        }
        return opciones;
    }

    /** A crédito, el total no puede superar el monto máximo que calculó el banco. */
    public boolean isSuperaMontoMaximo() {
        return isCredito() && evaluacion != null && evaluacion.getMontoMaximo() != null
                && getTotal().compareTo(evaluacion.getMontoMaximo()) > 0;
    }

    public void vaciar() {
        lineas.clear();
        cedula = "";
        evaluacion = null;
        formaPago = FormaPago.EFECTIVO;
        numCuotas = 6;
    }
}
