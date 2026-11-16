package ec.edu.monster.modelo.entidad;

/**
 * Estado de las dependencias externas al abrir la aplicación, para que el
 * usuario sepa antes de trabajar si la base de datos responde y qué datos
 * entregará la API.
 *
 * @param baseDisponible  MySQL respondió
 * @param totalPaises     países guardados (0 si la base no respondió)
 * @param detalleBase     motivo de la falla; {@code null} si la base respondió
 * @param apiDemostracion la API está configurada con la clave demo (solo datos de muestra)
 * @author carlo
 */
public record EstadoSistema(boolean baseDisponible, long totalPaises, String detalleBase,
        boolean apiDemostracion) {
}
