package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Un crédito otorgado. cuotas (la tabla de amortización) solo viene al
 * otorgarlo o al consultarlo por su código; en la lista de créditos de un
 * cliente queda nula y JSON-B no la incluye.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"codCredito", "codCliente", "fecha", "monto", "numCuotas", "tasaAnual", "valorCuota",
    "estado", "cuotas"})
public class CreditoDto {

    public int codCredito;
    public int codCliente;
    public LocalDate fecha;
    public BigDecimal monto;
    public int numCuotas;
    public BigDecimal tasaAnual;
    public BigDecimal valorCuota;
    public String estado;
    public List<CuotaDto> cuotas;
}
