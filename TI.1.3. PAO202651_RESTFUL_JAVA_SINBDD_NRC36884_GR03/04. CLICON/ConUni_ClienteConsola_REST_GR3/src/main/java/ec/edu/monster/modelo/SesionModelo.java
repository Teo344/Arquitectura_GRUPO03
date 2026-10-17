package ec.edu.monster.modelo;

import java.time.Instant;

/** Datos de la sesión devueltos por el servidor REST. */
public class SesionModelo {

    private final String usuario;
    private final String token;
    private final Instant expiraEn;

    public SesionModelo(String usuario, String token, Instant expiraEn) {
        this.usuario = usuario;
        this.token = token;
        this.expiraEn = expiraEn;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }
}
