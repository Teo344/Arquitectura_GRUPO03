package ec.edu.monster.modelo.entidad;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reporte estadístico (CU-08 / Actividad 5).
 *
 * @param paisesPorRegion    cantidad de países por región, de mayor a menor
 * @param totalPaises        países registrados
 * @param paisMasPoblado     {@code null} si no hay países
 * @param regionConMasPaises {@code null} si no hay países; ante empate, la
 *                           primera en el orden del mapa
 * @author carlo
 */
public record EstadisticasPaises(Map<Region, Long> paisesPorRegion, long totalPaises,
        Pais paisMasPoblado, Region regionConMasPaises) {

    public EstadisticasPaises {
        // Copia que conserva el orden y no se puede modificar desde afuera
        paisesPorRegion = Collections.unmodifiableMap(new LinkedHashMap<>(paisesPorRegion));
    }

    public boolean estaVacio() {
        return totalPaises == 0;
    }
}
