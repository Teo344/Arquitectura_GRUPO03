package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Respuesta de POST /sesiones: el token para la cabecera Authorization y el
 * empleado autenticado. expiraEn es un instante ISO-8601 en UTC.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"token", "expiraEn", "codUsuario", "usuario", "nombre"})
public class SesionDto {

    public String token;
    public String expiraEn;
    public int codUsuario;
    public String usuario;
    public String nombre;
}
