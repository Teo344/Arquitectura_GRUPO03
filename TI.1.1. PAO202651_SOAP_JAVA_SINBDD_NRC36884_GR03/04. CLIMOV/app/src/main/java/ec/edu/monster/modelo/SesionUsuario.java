package ec.edu.monster.modelo;

import java.io.Serializable;

/** Estado de autenticación de la sesión actual del cliente. Se envía entre pantallas. */
public final class SesionUsuario implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String usuario;
    private final String token;
    private final String expiracion;

    public SesionUsuario(String usuario, String token, String expiracion) {
        this.usuario = usuario;
        this.token = token;
        this.expiracion = expiracion;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getToken() {
        return token;
    }

    public String getExpiracion() {
        return expiracion;
    }
}
