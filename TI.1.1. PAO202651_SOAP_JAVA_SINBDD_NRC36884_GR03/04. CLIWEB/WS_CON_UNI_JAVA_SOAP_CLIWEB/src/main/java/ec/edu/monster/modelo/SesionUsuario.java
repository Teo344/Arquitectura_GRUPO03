package ec.edu.monster.modelo;

/** Estado de autenticación de la sesión actual del cliente. */
public final class SesionUsuario {
    private final String usuario;
    private final String token;
    private final String expiracion;

    public SesionUsuario(String usuario, String token, String expiracion) {
        this.usuario = usuario;
        this.token = token;
        this.expiracion = expiracion;
    }

    public String getUsuario() { return usuario; }
    public String getToken() { return token; }
    public String getExpiracion() { return expiracion; }
}
