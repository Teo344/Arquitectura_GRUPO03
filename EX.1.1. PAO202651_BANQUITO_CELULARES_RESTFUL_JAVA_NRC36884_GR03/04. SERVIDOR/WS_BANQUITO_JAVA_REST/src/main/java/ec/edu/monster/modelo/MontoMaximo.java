package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Monto máximo de crédito de un cliente con los datos usados en el cálculo:
 * ((promedioDepositos - promedioRetiros) * porcentaje%) * plazoMeses.
 *
 * @author carlo
 */
public record MontoMaximo(int codCliente, BigDecimal promedioDepositos, BigDecimal promedioRetiros,
        BigDecimal porcentaje, int plazoMeses, BigDecimal montoMaximo) {
}
