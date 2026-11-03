package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Factura de venta. codCreditoBanco y numCuotas solo existen en las ventas a
 * crédito; detalles viene vacío en la lista de facturas.
 */
public class FacturaModelo {

    private final int numFactura;
    private final String fecha;
    private final String usuario;
    private final String cedula;
    private final int codClienteBanco;
    private final String nombreCliente;
    private final FormaPago formaPago;
    private final BigDecimal subtotal;
    private final BigDecimal descuento;
    private final BigDecimal total;
    private final Integer codCreditoBanco;
    private final Integer numCuotas;
    private final List<DetalleFacturaModelo> detalles;

    public FacturaModelo(int numFactura, String fecha, String usuario, String cedula, int codClienteBanco,
            String nombreCliente, FormaPago formaPago, BigDecimal subtotal, BigDecimal descuento, BigDecimal total,
            Integer codCreditoBanco, Integer numCuotas, List<DetalleFacturaModelo> detalles) {
        this.numFactura = numFactura;
        this.fecha = fecha;
        this.usuario = usuario;
        this.cedula = cedula;
        this.codClienteBanco = codClienteBanco;
        this.nombreCliente = nombreCliente;
        this.formaPago = formaPago;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.total = total;
        this.codCreditoBanco = codCreditoBanco;
        this.numCuotas = numCuotas;
        this.detalles = List.copyOf(detalles);
    }

    public int getNumFactura() {
        return numFactura;
    }

    public String getFecha() {
        return fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getCedula() {
        return cedula;
    }

    public int getCodClienteBanco() {
        return codClienteBanco;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Integer getCodCreditoBanco() {
        return codCreditoBanco;
    }

    public Integer getNumCuotas() {
        return numCuotas;
    }

    public List<DetalleFacturaModelo> getDetalles() {
        return detalles;
    }

    public boolean isCredito() {
        return formaPago == FormaPago.CREDITO;
    }

    /** Número con ceros a la izquierda, como se imprime: 000123. */
    public String getNumeroVisible() {
        return String.format("%06d", numFactura);
    }
}
