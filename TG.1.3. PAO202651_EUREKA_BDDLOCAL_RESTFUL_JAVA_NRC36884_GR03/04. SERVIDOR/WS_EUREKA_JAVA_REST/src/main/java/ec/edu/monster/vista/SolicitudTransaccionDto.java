package ec.edu.monster.vista;

import java.math.BigDecimal;

/**
 * Cuerpo JSON de los depósitos y retiros. El token no va aquí: viaja en la
 * cabecera Authorization.
 *
 * @author carlo
 */
public class SolicitudTransaccionDto {

    public String cuenta;
    public BigDecimal importe;
}
