package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Factura de venta (tabla FACTURA). codCreditoBanco y numCuotas solo existen
 * en las ventas a CREDITO; detalles queda vacío cuando solo se listan las
 * facturas.
 *
 * @author carlo
 */
public record Factura(int numero, LocalDateTime fecha, String usuario, String cedula, int codClienteBanco,
        String nombreCliente, FormaPago formaPago, BigDecimal subtotal, BigDecimal descuento, BigDecimal total,
        Integer codCreditoBanco, Integer numCuotas, List<DetalleFactura> detalles) {
}
