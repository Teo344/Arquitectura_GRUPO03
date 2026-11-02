package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Línea de una factura (tabla DETALLE_FACTURA) con la marca y el modelo del
 * teléfono. subtotal = cantidad * precioUnitario.
 *
 * @author carlo
 */
public record DetalleFactura(int codTelefono, String marca, String modelo, int cantidad, BigDecimal precioUnitario,
        BigDecimal subtotal) {
}
