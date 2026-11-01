package ec.edu.monster.modelo;

/**
 * Códigos de error del ERS (sección 8.2.7) que viajan en el detalle del
 * SOAPFault (RNF-009).
 *
 * @author carlo
 */
public enum CodigoError {

    AUTH_001("AUTH-001", "Usuario o contraseña incorrectos"),
    AUTH_002("AUTH-002", "El usuario no se encuentra activo"),
    AUTH_003("AUTH-003", "Sesión inválida o expirada; inicie sesión nuevamente"),
    DATA_001("DATA-001", "Datos incompletos o con formato inválido"),
    CTA_001("CTA-001", "La cuenta no existe"),
    CTA_002("CTA-002", "La cuenta no se encuentra activa"),
    MOV_001("MOV-001", "El importe debe ser mayor que cero y tener como máximo dos decimales"),
    MOV_002("MOV-002", "Saldo insuficiente para realizar la operación"),
    MOV_003("MOV-003", "La cuenta de destino debe ser diferente de la cuenta de origen"),
    MOV_004("MOV-004", "Las cuentas deben tener la misma moneda"),
    SYS_001("SYS-001", "No se pudo completar la operación; servicio o base de datos no disponible");

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
