package ec.edu.monster.modelo;

import java.io.Serializable;

/**
 * Sesión activa del empleado (RQF-001). Viaja entre pantallas; el token solo
 * lo usa la capa de servicio para la cabecera SOAP y la vista nunca lo muestra.
 * expiraEn es la fecha ISO-8601 (UTC) que devuelve el servidor.
 */
public final class SesionModelo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String token;
    private final String expiraEn;
    private final String codigoEmpleado;
    private final String nombreEmpleado;
    private final String codigoSucursal;
    private final String sucursal;

    public SesionModelo(String token, String expiraEn, String codigoEmpleado, String nombreEmpleado,
            String codigoSucursal, String sucursal) {
        this.token = token;
        this.expiraEn = expiraEn;
        this.codigoEmpleado = codigoEmpleado;
        this.nombreEmpleado = nombreEmpleado;
        this.codigoSucursal = codigoSucursal;
        this.sucursal = sucursal;
    }

    public String getToken() {
        return token;
    }

    public String getExpiraEn() {
        return expiraEn;
    }

    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public String getCodigoSucursal() {
        return codigoSucursal;
    }

    public String getSucursal() {
        return sucursal;
    }
}
