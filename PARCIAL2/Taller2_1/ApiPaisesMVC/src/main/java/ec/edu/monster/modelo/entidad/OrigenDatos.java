package ec.edu.monster.modelo.entidad;

/**
 * De dónde salió el resultado de una búsqueda.
 *
 * @author carlo
 */
public enum OrigenDatos {

    /** Respuesta en vivo de REST Countries. */
    API_EXTERNA,
    /** Respaldo: la API falló y se respondió con lo ya guardado en MySQL. */
    BASE_LOCAL
}
