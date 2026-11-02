package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Crédito otorgado (tabla CREDITO). cuotas es la tabla de amortización, o
 * una lista vacía cuando solo se consultan los datos del crédito.
 *
 * @author carlo
 */
public record Credito(int codigo, int codCliente, LocalDate fecha, BigDecimal monto, int numCuotas,
        BigDecimal tasaAnual, BigDecimal valorCuota, String estado, List<Cuota> cuotas) {
}
