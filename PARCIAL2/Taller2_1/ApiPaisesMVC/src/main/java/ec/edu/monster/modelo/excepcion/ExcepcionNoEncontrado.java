package ec.edu.monster.modelo.excepcion;

/**
 * Se pidió un registro que no existe (p. ej. actualizar o eliminar un id que
 * otro usuario ya borró).
 *
 * @author carlo
 */
public class ExcepcionNoEncontrado extends ExcepcionAplicacion {

    public ExcepcionNoEncontrado(String mensaje) {
        super(mensaje);
    }
}
