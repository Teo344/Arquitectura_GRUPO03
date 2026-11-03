package ec.edu.monster.modelo;

import java.time.Instant;

/**
 * Sesión del empleado de la comercializadora. El token solo lo usa la capa de
 * servicio para la cabecera Authorization; la vista nunca lo muestra.
 */
public class SesionModelo {

    private final String token;
    private final Instant expiraEn;
    private final int codUsuario;
    private final String usuario;
    private final String nombre;

    public SesionModelo(String token, Instant expiraEn, int codUsuario, String usuario, String nombre) {
        this.token = token;
        this.expiraEn = expiraEn;
        this.codUsuario = codUsuario;
        this.usuario = usuario;
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public int getCodUsuario() {
        return codUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getNombre() {
        return nombre;
    }
}
