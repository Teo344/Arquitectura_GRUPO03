package ec.edu.monster.modelo;

/**
 * Error que devuelve el servidor en el detalle ErrorServicio del SOAP Fault
 * (codigo y mensaje), o un error de comunicación (sin codigo).
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

    /** El token ya no es válido: el cliente debe volver al inicio de sesión. */
    public boolean requiereNuevoLogin() {
        return SESION_INVALIDA.equals(codigo);
    }
}
