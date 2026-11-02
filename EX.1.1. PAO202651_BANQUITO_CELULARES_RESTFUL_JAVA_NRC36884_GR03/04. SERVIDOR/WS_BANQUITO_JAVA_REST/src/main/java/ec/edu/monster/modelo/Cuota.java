package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Una fila de la tabla de amortización (tabla CUOTA). saldo es el capital que
 * queda por pagar después de esta cuota.
 *
 * @author carlo
 */
public record Cuota(int numero, BigDecimal valorCuota, BigDecimal interes, BigDecimal capital, BigDecimal saldo) {
}
