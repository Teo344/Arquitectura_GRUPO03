package ec.edu.monster.modelo;

import java.io.Serializable;

/**
 * Sesión del empleado de la comercializadora. Viaja entre pantallas; el token
 * solo lo usa la capa de servicio para la cabecera Authorization y la vista
 * nunca lo muestra. expiraEn es la fecha ISO-8601 (UTC) que devuelve el servidor.
 */
public final class SesionModelo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String token;
    private final String expiraEn;
    private final int codUsuario;
    private final String usuario;
    private final String nombre;

    public SesionModelo(String token, String expiraEn, int codUsuario, String usuario, String nombre) {
        this.token = token;
        this.expiraEn = expiraEn;
        this.codUsuario = codUsuario;
        this.usuario = usuario;
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public String getExpiraEn() {
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
