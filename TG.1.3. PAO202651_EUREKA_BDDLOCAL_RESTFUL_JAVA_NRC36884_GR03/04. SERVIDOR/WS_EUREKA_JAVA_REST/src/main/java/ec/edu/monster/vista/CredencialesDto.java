package ec.edu.monster.vista;

/**
 * Cuerpo JSON de POST /api/autenticacion/login. equipo es opcional: el nombre
 * del equipo del cliente para logsession.
 *
 * @author carlo
 */
public class CredencialesDto {

    public String usuario;
    public String clave;
    public String equipo;
}
