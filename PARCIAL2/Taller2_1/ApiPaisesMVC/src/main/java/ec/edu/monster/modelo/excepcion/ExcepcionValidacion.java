package ec.edu.monster.modelo.excepcion;

import java.util.List;

/**
 * Los datos ingresados no cumplen las reglas del negocio. Reúne todos los
 * problemas encontrados para que el usuario los corrija de una sola vez.
 *
 * @author carlo
 */
public class ExcepcionValidacion extends ExcepcionAplicacion {

    private final List<String> errores;

    public ExcepcionValidacion(String error) {
        this(List.of(error));
    }

    public ExcepcionValidacion(List<String> errores) {
        super(String.join("\n", errores));
        this.errores = List.copyOf(errores);
    }

    public List<String> getErrores() {
        return errores;
    }
}
