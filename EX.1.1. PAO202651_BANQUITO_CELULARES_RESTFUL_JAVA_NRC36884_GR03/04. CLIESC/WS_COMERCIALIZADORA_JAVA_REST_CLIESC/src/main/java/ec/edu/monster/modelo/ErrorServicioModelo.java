package ec.edu.monster.modelo;

/**
 * Error que devuelve la API en el cuerpo application/problem+json (codigo y
 * detail), o un error de comunicación con el servidor (sin codigo).
 */
public class ErrorServicioModelo extends RuntimeException {

    private static final String SESION_INVALIDA = "AUTH-003";

    private final String codigo;

    public ErrorServicioModelo(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public ErrorServicioModelo(String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = null;
    }

    public String getCodigo() {
        return codigo;
    }

    /** El token ya no es válido: el empleado debe volver a iniciar sesión. */
    public boolean requiereNuevoLogin() {
        return SESION_INVALIDA.equals(codigo);
    }
}
