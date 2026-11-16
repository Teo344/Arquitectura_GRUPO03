package ec.edu.monster.modelo.excepcion;

/**
 * Falla al conectar o al operar con la base de datos. La causa (SQLException)
 * se conserva para el registro técnico; el mensaje es para el usuario.
 *
 * @author carlo
 */
public class ExcepcionPersistencia extends ExcepcionAplicacion {

    public ExcepcionPersistencia(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
