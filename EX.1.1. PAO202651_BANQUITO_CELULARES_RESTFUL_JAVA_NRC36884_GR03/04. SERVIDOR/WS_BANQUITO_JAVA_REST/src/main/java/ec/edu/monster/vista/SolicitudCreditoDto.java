package ec.edu.monster.vista;

import java.math.BigDecimal;

/**
 * Cuerpo JSON de POST /creditos (servicio 3).
 *
 * @author carlo
 */
public class SolicitudCreditoDto {

    public Integer codCliente;
    public BigDecimal monto;
    public Integer numCuotas;
}
