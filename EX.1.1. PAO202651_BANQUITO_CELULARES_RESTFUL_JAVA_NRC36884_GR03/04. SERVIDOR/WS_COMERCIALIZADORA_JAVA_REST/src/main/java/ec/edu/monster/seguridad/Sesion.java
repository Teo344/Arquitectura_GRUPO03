package ec.edu.monster.seguridad;

/**
 * Datos del empleado autenticado que acompañan a un token. Las facturas se
 * registran con codUsuario, nunca con un usuario que llegue en el cuerpo.
 *
 * @author carlo
 */
public record Sesion(int codUsuario, String usuario, String nombre) {
}
