package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;

/**
 * Una fila de la tabla de amortización (servicio 4).
 *
 * @author carlo
 */
@JsonbPropertyOrder({"numero", "valorCuota", "interes", "capital", "saldo"})
public class CuotaDto {

    public int numero;
    public BigDecimal valorCuota;
    public BigDecimal interes;
    public BigDecimal capital;
    public BigDecimal saldo;
}
