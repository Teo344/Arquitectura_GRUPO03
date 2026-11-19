package ec.edu.monster.modelo.entidad;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reporte estadístico (CU-08 / Actividad 5).
 *
 * @param paisesPorRegion    cantidad de países por región, de mayor a menor
 * @param totalPaises        países registrados
 * @param masPoblados        los países con más habitantes, de mayor a menor
 *                           (vacía si no hay países)
 * @param regionConMasPaises {@code null} si no hay países; ante empate, la
 *                           primera en el orden del mapa
 * @author carlo
 */
public record EstadisticasPaises(Map<Region, Long> paisesPorRegion, long totalPaises,
        List<Pais> masPoblados, Region regionConMasPaises) {

    public EstadisticasPaises {
        // Copias que conservan el orden y no se pueden modificar desde afuera
        paisesPorRegion = Collections.unmodifiableMap(new LinkedHashMap<>(paisesPorRegion));
        masPoblados = List.copyOf(masPoblados);
    }

    /**
     * @return el país con más habitantes; {@code null} si no hay países
     */
    public Pais paisMasPoblado() {
        return masPoblados.isEmpty() ? null : masPoblados.get(0);
    }

    public boolean estaVacio() {
        return totalPaises == 0;
    }
}
