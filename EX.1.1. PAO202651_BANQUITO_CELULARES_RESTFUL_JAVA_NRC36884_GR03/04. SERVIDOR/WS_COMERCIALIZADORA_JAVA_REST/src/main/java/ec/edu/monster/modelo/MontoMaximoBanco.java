package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Respuesta del servicio 2 del Core (monto máximo de crédito).
 *
 * @author carlo
 */
public class MontoMaximoBanco {

    public int codCliente;
    public BigDecimal promedioDepositos;
    public BigDecimal promedioRetiros;
    public BigDecimal porcentaje;
    public int plazoMeses;
    public BigDecimal montoMaximo;
}
