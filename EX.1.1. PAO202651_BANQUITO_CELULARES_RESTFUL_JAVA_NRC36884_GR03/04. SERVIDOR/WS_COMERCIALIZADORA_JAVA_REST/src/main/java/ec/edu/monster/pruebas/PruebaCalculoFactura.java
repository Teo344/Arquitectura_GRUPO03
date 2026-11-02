package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.servicios.CalculadoraFactura;
import java.math.BigDecimal;

/**
 * Prueba unitaria de los valores de la factura: no necesita servidor ni base
 * de datos. El descuento por pago en efectivo es del 42 %.
 *
 * @author carlo
 */
public class PruebaCalculoFactura {

    private static final BigDecimal DESCUENTO = new BigDecimal("42");
    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== PruebaCalculoFactura ===");
        comprobar("3 x 199.00 = 597.00",
                CalculadoraFactura.subtotalLinea(new BigDecimal("199.00"), 3).equals(new BigDecimal("597.00")));
        comprobar("42 % de 1199.00 en efectivo = 503.58", CalculadoraFactura.descuento(new BigDecimal("1199.00"),
                FormaPago.EFECTIVO, DESCUENTO).equals(new BigDecimal("503.58")));
        comprobar("a crédito no hay descuento (0.00)", CalculadoraFactura.descuento(new BigDecimal("1199.00"),
                FormaPago.CREDITO, DESCUENTO).equals(new BigDecimal("0.00")));
        comprobar("el descuento se redondea HALF_UP: 42 % de 199.99 = 84.00", CalculadoraFactura.descuento(
                new BigDecimal("199.99"), FormaPago.EFECTIVO, DESCUENTO).equals(new BigDecimal("84.00")));
        comprobar("precio en efectivo de 199.00 = 115.42 (58 %)",
                CalculadoraFactura.precioEfectivo(new BigDecimal("199.00"), DESCUENTO).equals(new BigDecimal("115.42")));

        BigDecimal subtotal = CalculadoraFactura.subtotalLinea(new BigDecimal("199.00"), 2)
                .add(CalculadoraFactura.subtotalLinea(new BigDecimal("279.00"), 1));
        BigDecimal descuento = CalculadoraFactura.descuento(subtotal, FormaPago.EFECTIVO, DESCUENTO);
        comprobar("2 x 199 + 279: subtotal 677.00, descuento 284.34, total 392.66",
                subtotal.equals(new BigDecimal("677.00")) && descuento.equals(new BigDecimal("284.34"))
                && subtotal.subtract(descuento).equals(new BigDecimal("392.66")));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
