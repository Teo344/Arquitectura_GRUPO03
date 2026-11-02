package ec.edu.monster.servicios;

import ec.edu.monster.modelo.FormaPago;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Valores de la factura. Es una clase pura (sin base de datos ni CDI) para
 * poder probarla sin servidor. El descuento por pago en efectivo se aplica
 * sobre el subtotal y se redondea HALF_UP a 2 decimales; las ventas a crédito
 * no tienen descuento.
 *
 * @author carlo
 */
public final class CalculadoraFactura {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private CalculadoraFactura() {
    }

    public static BigDecimal subtotalLinea(BigDecimal precioUnitario, int cantidad) {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal descuento(BigDecimal subtotal, FormaPago formaPago, BigDecimal porcentaje) {
        if (formaPago != FormaPago.EFECTIVO) {
            return BigDecimal.ZERO.setScale(2);
        }
        return subtotal.multiply(porcentaje).divide(CIEN, 2, RoundingMode.HALF_UP);
    }

    /**
     * Precio de un teléfono pagado en efectivo, para mostrarlo en el catálogo.
     */
    public static BigDecimal precioEfectivo(BigDecimal precio, BigDecimal porcentaje) {
        return precio.subtract(descuento(precio, FormaPago.EFECTIVO, porcentaje));
    }
}
