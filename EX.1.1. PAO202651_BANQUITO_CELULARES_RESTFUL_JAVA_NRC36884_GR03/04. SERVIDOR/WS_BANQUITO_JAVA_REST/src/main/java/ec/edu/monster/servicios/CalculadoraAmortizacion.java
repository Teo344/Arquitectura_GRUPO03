package ec.edu.monster.servicios;

import ec.edu.monster.modelo.Cuota;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla de amortización de un crédito de cuota fija con pago mensual. Es una
 * clase pura (sin base de datos ni CDI) para poder probarla sin servidor.
 *
 * <ul>
 * <li>tasa mensual = tasa anual / 12</li>
 * <li>cuota = monto · i / (1 − (1 + i)^−n), redondeada a 2 decimales</li>
 * <li>interés = saldo · i y capital = cuota − interés, en cada fila</li>
 * <li>la última fila abona todo el saldo como capital y el interés absorbe
 * el centavo que dejan los redondeos: la cuota sigue fija y el saldo cierra
 * en 0.00</li>
 * </ul>
 * Los redondeos son HALF_UP, igual que REDONDEAR de Excel.
 *
 * @author carlo
 */
public final class CalculadoraAmortizacion {

    private static final MathContext PRECISION = MathContext.DECIMAL128;
    private static final BigDecimal MESES_POR_ANIO_POR_CIEN = new BigDecimal("1200");

    private CalculadoraAmortizacion() {
    }

    /**
     * Tasa mensual como fracción: 16.50 % anual da 0.01375.
     */
    public static BigDecimal tasaMensual(BigDecimal tasaAnual) {
        return tasaAnual.divide(MESES_POR_ANIO_POR_CIEN, PRECISION);
    }

    public static BigDecimal valorCuota(BigDecimal monto, int numCuotas, BigDecimal tasaAnual) {
        BigDecimal i = tasaMensual(tasaAnual);
        BigDecimal factor = BigDecimal.ONE.subtract(BigDecimal.ONE.add(i).pow(-numCuotas, PRECISION), PRECISION);
        return monto.multiply(i, PRECISION).divide(factor, PRECISION).setScale(2, RoundingMode.HALF_UP);
    }

    public static List<Cuota> tabla(BigDecimal monto, int numCuotas, BigDecimal tasaAnual) {
        BigDecimal i = tasaMensual(tasaAnual);
        BigDecimal cuota = valorCuota(monto, numCuotas, tasaAnual);
        BigDecimal saldo = monto.setScale(2, RoundingMode.HALF_UP);
        List<Cuota> cuotas = new ArrayList<>(numCuotas);
        for (int numero = 1; numero <= numCuotas; numero++) {
            BigDecimal interes;
            BigDecimal capital;
            if (numero < numCuotas) {
                interes = saldo.multiply(i).setScale(2, RoundingMode.HALF_UP);
                capital = cuota.subtract(interes);
            } else {
                capital = saldo;
                interes = cuota.subtract(capital);
            }
            saldo = saldo.subtract(capital);
            cuotas.add(new Cuota(numero, cuota, interes, capital, saldo));
        }
        return cuotas;
    }
}
