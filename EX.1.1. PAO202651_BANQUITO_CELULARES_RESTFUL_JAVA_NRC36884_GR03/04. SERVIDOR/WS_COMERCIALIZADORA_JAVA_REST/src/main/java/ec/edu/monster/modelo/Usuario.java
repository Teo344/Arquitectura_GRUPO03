package ec.edu.monster.modelo;

/**
 * Empleado de la comercializadora que inicia sesión (tabla USUARIO).
 *
 * @author carlo
 */
public record Usuario(int codigo, String usuario, String nombre, String estado) {

    public boolean activo() {
        return "ACTIVO".equals(estado);
    }
}
