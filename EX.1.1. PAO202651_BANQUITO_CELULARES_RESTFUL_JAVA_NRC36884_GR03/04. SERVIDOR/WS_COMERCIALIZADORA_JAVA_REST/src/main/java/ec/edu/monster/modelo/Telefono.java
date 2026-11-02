package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Teléfono del catálogo (tabla TELEFONO). Solo los ACTIVOS están a la venta;
 * eliminar un teléfono lo deja INACTIVO.
 *
 * @author carlo
 */
public record Telefono(int codigo, String marca, String modelo, BigDecimal precio, String estado) {

    public boolean activo() {
        return "ACTIVO".equals(estado);
    }
}
