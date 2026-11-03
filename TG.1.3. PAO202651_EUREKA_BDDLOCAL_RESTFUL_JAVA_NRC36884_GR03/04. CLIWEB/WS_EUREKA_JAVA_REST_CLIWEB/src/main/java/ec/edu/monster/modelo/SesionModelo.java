package ec.edu.monster.modelo;

import java.time.Instant;

/**
 * Sesión activa del empleado (RQF-001). El token solo lo usa la capa de
 * servicio para la cabecera Authorization; la vista nunca lo muestra.
 */
public class SesionModelo {

    private final String token;
    private final Instant expiraEn;
    private final String codigoEmpleado;
    private final String nombreEmpleado;
    private final String codigoSucursal;
    private final String sucursal;

    public SesionModelo(String token, Instant expiraEn, String codigoEmpleado, String nombreEmpleado,
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

    public Instant getExpiraEn() {
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
