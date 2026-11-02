package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;

/**
 * Teléfono del catálogo. precioEfectivo es el precio con el descuento por
 * pago en efectivo, calculado por el servidor.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"codTelefono", "marca", "modelo", "precio", "precioEfectivo", "estado"})
public class TelefonoDto {

    public int codTelefono;
    public String marca;
    public String modelo;
    public BigDecimal precio;
    public BigDecimal precioEfectivo;
    public String estado;
}
