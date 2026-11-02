package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;

/**
 * Respuesta del servicio 2 (monto máximo), con los datos del cálculo.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"codCliente", "promedioDepositos", "promedioRetiros", "porcentaje", "plazoMeses", "montoMaximo"})
public class MontoMaximoDto {

    public int codCliente;
    public BigDecimal promedioDepositos;
    public BigDecimal promedioRetiros;
    public BigDecimal porcentaje;
    public int plazoMeses;
    public BigDecimal montoMaximo;
}
