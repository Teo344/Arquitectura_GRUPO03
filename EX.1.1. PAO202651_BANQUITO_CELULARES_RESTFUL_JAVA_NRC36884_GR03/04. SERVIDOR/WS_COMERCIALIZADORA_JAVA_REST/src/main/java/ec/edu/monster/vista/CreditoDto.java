package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Un crédito del banco en la lista de créditos de un comprador. La tabla de
 * amortización se consulta aparte: GET /creditos/{codCredito}/cuotas.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"codCredito", "codCliente", "fecha", "monto", "numCuotas", "tasaAnual", "valorCuota", "estado"})
public class CreditoDto {

    public int codCredito;
    public int codCliente;
    public LocalDate fecha;
    public BigDecimal monto;
    public int numCuotas;
    public BigDecimal tasaAnual;
    public BigDecimal valorCuota;
    public String estado;
}
