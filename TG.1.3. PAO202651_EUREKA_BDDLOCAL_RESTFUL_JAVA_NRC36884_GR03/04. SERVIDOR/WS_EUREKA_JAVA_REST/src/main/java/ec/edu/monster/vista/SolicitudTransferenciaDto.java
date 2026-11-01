package ec.edu.monster.vista;

import java.math.BigDecimal;

/**
 * Cuerpo JSON de las transferencias. El token no va aquí: viaja en la
 * cabecera Authorization.
 *
 * @author carlo
 */
public class SolicitudTransferenciaDto {

    public String cuentaOrigen;
    public String cuentaDestino;
    public BigDecimal importe;
}
