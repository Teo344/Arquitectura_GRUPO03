package ec.edu.monster.modelo;

/**
 * Empleado que inicia sesión, con su usuario y su sucursal vigente (RQF-001).
 * codigoSucursal es null si no tiene una asignación vigente.
 *
 * @author carlo
 */
public record Empleado(String codigo, String usuario, String nombre, String estado,
        String codigoSucursal, String sucursal) {
}
