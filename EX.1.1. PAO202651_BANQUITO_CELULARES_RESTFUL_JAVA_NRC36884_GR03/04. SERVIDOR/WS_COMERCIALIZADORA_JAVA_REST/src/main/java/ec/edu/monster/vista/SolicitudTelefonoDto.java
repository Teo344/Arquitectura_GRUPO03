package ec.edu.monster.vista;

import java.math.BigDecimal;

/**
 * Cuerpo JSON de POST /telefonos y PUT /telefonos/{codTelefono}.
 *
 * @author carlo
 */
public class SolicitudTelefonoDto {

    public String marca;
    public String modelo;
    public BigDecimal precio;
}
