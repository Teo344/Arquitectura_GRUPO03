package ec.edu.monster.modelo;

/**
 * Códigos de error que viajan en el cuerpo JSON de las respuestas de error (RQF-010).
 *
 * @author carlo
 */
public enum CodigoError {

    AUTH_001("AUTH-001", "Token inválido o ausente"),
    AUTH_002("AUTH-002", "Sesión expirada"),
    AUTH_003("AUTH-003", "Credenciales incorrectas"),
    UNIT_001("UNIT-001", "Unidad no soportada"),
    DATA_001("DATA-001", "Datos inconsistentes");

    private final String codigo;
    private final String mensaje;

    CodigoError(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getMensaje() {
        return mensaje;
    }
}
