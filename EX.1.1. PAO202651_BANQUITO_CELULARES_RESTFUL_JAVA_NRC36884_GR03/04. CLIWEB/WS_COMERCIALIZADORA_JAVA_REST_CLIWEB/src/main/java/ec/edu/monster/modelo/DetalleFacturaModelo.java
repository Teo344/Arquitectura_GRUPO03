package ec.edu.monster.modelo;

import java.math.BigDecimal;

/** Línea de una factura: el precio unitario es el del catálogo en el momento de la venta. */
public class DetalleFacturaModelo {

    private final int codTelefono;
    private final String marca;
    private final String modelo;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public DetalleFacturaModelo(int codTelefono, String marca, String modelo, int cantidad, BigDecimal precioUnitario,
            BigDecimal subtotal) {
        this.codTelefono = codTelefono;
        this.marca = marca;
        this.modelo = modelo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public int getCodTelefono() {
        return codTelefono;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
