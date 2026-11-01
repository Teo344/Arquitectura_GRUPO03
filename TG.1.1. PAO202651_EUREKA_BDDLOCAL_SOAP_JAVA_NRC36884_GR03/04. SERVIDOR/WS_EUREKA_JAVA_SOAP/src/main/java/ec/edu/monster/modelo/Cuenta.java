package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cuenta con sus datos de consulta (RQF-003). Al bloquearla para una
 * transacción solo se leen codigo, codigoMoneda, saldo, estado y contador; el
 * resto queda null.
 *
 * @author carlo
 */
public record Cuenta(String codigo, String titular, String codigoMoneda, String moneda,
        BigDecimal saldo, String estado, String sucursal, LocalDate fechaCreacion, int contador) {
}
