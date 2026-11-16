package ec.edu.monster.modelo.excepcion;

/**
 * Raíz de las fallas que la aplicación conoce. El mensaje ya está redactado
 * para mostrarse al usuario, así la vista puede atrapar este único tipo y
 * presentar {@link #getMessage()} sin conocer el detalle técnico.
 * <p>
 * Es no comprobada para no obligar a cada capa intermedia a declararla: solo
 * la vista decide cómo informarla.
 *
 * @author carlo
 */
public abstract class ExcepcionAplicacion extends RuntimeException {

    protected ExcepcionAplicacion(String mensaje) {
        super(mensaje);
    }

    protected ExcepcionAplicacion(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
