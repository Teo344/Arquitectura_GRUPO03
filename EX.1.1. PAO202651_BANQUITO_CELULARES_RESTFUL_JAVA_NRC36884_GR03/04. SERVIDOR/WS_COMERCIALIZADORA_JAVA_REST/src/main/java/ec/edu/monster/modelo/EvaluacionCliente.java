package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Lo que la comercializadora necesita saber de un comprador antes de venderle
 * a crédito: la evaluación del servicio 1 y el monto máximo del servicio 2
 * (null si la cédula no es de un cliente).
 *
 * @author carlo
 */
public record EvaluacionCliente(EvaluacionBanco evaluacion, BigDecimal montoMaximo) {
}
