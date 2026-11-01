package ec.edu.monster.seguridad;

/**
 * Datos del empleado autenticado que acompañan a un token. logId es la fila
 * de logsession donde se registra la salida (RQF-002).
 *
 * @author carlo
 */
public record Sesion(String codigoEmpleado, String usuario, String nombre, String codigoSucursal,
        String sucursal, int logId) {
}
