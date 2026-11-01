package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Movimiento del historial de una cuenta (RQF-004). accion es INGRESO o
 * SALIDA; cuentaReferencia es la otra cuenta de una transferencia.
 *
 * @author carlo
 */
public record Movimiento(int numero, LocalDate fecha, String tipo, String accion,
        BigDecimal importe, String cuentaReferencia) {
}
