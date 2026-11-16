package ec.edu.monster.modelo.excepcion;

/**
 * La API pública no respondió, rechazó la solicitud o devolvió algo que no se
 * pudo interpretar. El servicio la usa para decidir el respaldo con la base
 * de datos local.
 *
 * @author carlo
 */
public class ExcepcionApiExterna extends ExcepcionAplicacion {

    public ExcepcionApiExterna(String mensaje) {
        super(mensaje);
    }

    public ExcepcionApiExterna(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
