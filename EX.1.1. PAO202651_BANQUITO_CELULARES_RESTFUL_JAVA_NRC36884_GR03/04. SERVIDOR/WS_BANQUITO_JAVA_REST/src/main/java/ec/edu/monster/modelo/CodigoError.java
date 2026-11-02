package ec.edu.monster.modelo;

/**
 * Códigos de error del Core. Cada uno lleva el estado HTTP con el que se
 * responde, así MapeadorExcepciones no tiene que deducirlo: 400 datos
 * inválidos, 401 sin clave de API, 404 no existe, 409 una regla de negocio lo
 * impide y 503 la base de datos no responde.
 *
 * @author carlo
 */
public enum CodigoError {

    AUTH_004("AUTH-004", 401, "Clave de API ausente o inválida"),
    DATA_001("DATA-001", 400, "Datos incompletos o con formato inválido"),
    CLI_001("CLI-001", 404, "No existe un cliente con los datos indicados"),
    CRE_001("CRE-001", 409, "El cliente no es sujeto de crédito"),
    CRE_002("CRE-002", 409, "El monto solicitado supera el monto máximo de crédito del cliente"),
    CRE_003("CRE-003", 400, "El número de cuotas debe estar entre 3 y 18"),
    CRE_004("CRE-004", 404, "El crédito no existe"),
    CRE_005("CRE-005", 400, "El monto debe ser mayor que cero y tener como máximo dos decimales"),
    SYS_001("SYS-001", 503, "No se pudo completar la operación; servicio o base de datos no disponible");

    private final String codigo;
    private final int estadoHttp;
    private final String mensaje;

    CodigoError(String codigo, int estadoHttp, String mensaje) {
        this.codigo = codigo;
        this.estadoHttp = estadoHttp;
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getEstadoHttp() {
        return estadoHttp;
    }

    public String getMensaje() {
        return mensaje;
    }
}
