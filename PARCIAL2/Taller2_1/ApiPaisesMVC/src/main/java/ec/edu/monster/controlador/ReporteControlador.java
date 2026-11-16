package ec.edu.monster.controlador;

import ec.edu.monster.modelo.entidad.EstadisticasPaises;
import ec.edu.monster.servicio.PaisServicio;
import java.util.Objects;

/**
 * Controlador de reportes (CU-08 / Actividad 5). Separado de PaisControlador
 * para que cada pantalla dependa solo de lo que usa (ISP); el formato del
 * reporte (tabla, gráfico) lo decide la vista.
 *
 * @author carlo
 */
public class ReporteControlador {

    private final PaisServicio servicio;

    public ReporteControlador(PaisServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio, "servicio");
    }

    public EstadisticasPaises generarReporte() {
        return servicio.generarEstadisticas();
    }
}
