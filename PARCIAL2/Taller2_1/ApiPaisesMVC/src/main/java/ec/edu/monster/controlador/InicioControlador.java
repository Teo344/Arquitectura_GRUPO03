package ec.edu.monster.controlador;

import ec.edu.monster.modelo.entidad.EstadoSistema;
import ec.edu.monster.servicio.PaisServicio;
import java.util.Objects;

/**
 * Controlador de la pantalla de inicio: informa si la base de datos responde
 * y cómo está configurada la API antes de entrar a trabajar.
 *
 * @author carlo
 */
public class InicioControlador {

    private final PaisServicio servicio;

    public InicioControlador(PaisServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio, "servicio");
    }

    public EstadoSistema verificarEstado() {
        return servicio.verificarEstado();
    }
}
