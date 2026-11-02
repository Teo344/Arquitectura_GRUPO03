package ec.edu.monster.modelo;

/**
 * Códigos de error de la comercializadora. Cada uno lleva el estado HTTP con
 * el que se responde. CLI, CRE y DATA son los mismos códigos del Core: los
 * errores que devuelve el Core se reenvían con su código (ver
 * {@link #delCore(String)}).
 *
 * @author carlo
 */
public enum CodigoError {

    AUTH_001("AUTH-001", 401, "Usuario o contraseña incorrectos"),
    AUTH_002("AUTH-002", 401, "El usuario no se encuentra activo"),
    AUTH_003("AUTH-003", 401, "Sesión inválida o expirada; inicie sesión nuevamente"),
    DATA_001("DATA-001", 400, "Datos incompletos o con formato inválido"),
    TEL_001("TEL-001", 404, "El teléfono no existe"),
    TEL_002("TEL-002", 409, "El teléfono no está a la venta"),
    TEL_003("TEL-003", 400, "El precio debe ser mayor que cero y tener como máximo dos decimales"),
    FAC_001("FAC-001", 404, "La factura no existe"),
    FAC_002("FAC-002", 400, "Para vender a crédito el número de cuotas debe estar entre 3 y 18"),
    CLI_001("CLI-001", 404, "El comprador debe ser cliente de BanQuito"),
    CRE_001("CRE-001", 409, "El cliente no es sujeto de crédito"),
    CRE_002("CRE-002", 409, "El total de la venta supera el monto máximo de crédito del cliente"),
    CRE_003("CRE-003", 400, "El número de cuotas debe estar entre 3 y 18"),
    CRE_004("CRE-004", 404, "El crédito no existe"),
    CRE_005("CRE-005", 400, "El monto debe ser mayor que cero y tener como máximo dos decimales"),
    EXT_001("EXT-001", 502, "El Core BanQuito no está disponible; intente más tarde"),
    SYS_001("SYS-001", 503, "No se pudo completar la operación; servicio o base de datos no disponible");

    private final String codigo;
    private final int estadoHttp;
    private final String mensaje;

    CodigoError(String codigo, int estadoHttp, String mensaje) {
        this.codigo = codigo;
        this.estadoHttp = estadoHttp;
        this.mensaje = mensaje;
    }

    /**
     * Código de un error del Core que se puede mostrar al usuario tal cual, o
     * null si es un error interno del Core (clave de API, base de datos).
     */
    public static CodigoError delCore(String codigo) {
        for (CodigoError error : new CodigoError[]{DATA_001, CLI_001, CRE_001, CRE_002, CRE_003, CRE_004, CRE_005}) {
            if (error.codigo.equals(codigo)) {
                return error;
            }
        }
        return null;
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
