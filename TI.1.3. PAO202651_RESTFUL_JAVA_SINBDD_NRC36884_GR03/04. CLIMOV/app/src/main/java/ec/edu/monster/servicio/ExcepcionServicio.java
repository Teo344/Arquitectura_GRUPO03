package ec.edu.monster.servicio;

/**
 * Error devuelto por el servidor (JSON ErrorServicio con codigo AUTH-001, AUTH-002,
 * AUTH-003, UNIT-001, DATA-001) o de comunicación con él.
 */
public class ExcepcionServicio extends Exception {

    public static final String SIN_CONEXION = "CONEXION";

    private final String codigo;

    public ExcepcionServicio(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    /** AUTH-001 (token inválido) o AUTH-002 (sesión expirada): hay que volver a iniciar sesión. */
    public static boolean esSesionInvalida(Throwable error) {
        return error instanceof ExcepcionServicio
                && ("AUTH-001".equals(((ExcepcionServicio) error).codigo)
                || "AUTH-002".equals(((ExcepcionServicio) error).codigo));
    }

    /** Mensaje listo para mostrar al usuario a partir de cualquier error. */
    public static String mensajeAmigable(Throwable error) {
        String mensaje = error == null ? null : error.getMessage();
        return mensaje == null || mensaje.trim().isEmpty() ? "No se pudo completar la operación." : mensaje;
    }
}
