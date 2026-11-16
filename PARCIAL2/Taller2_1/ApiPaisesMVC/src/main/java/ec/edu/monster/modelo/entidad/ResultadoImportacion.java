package ec.edu.monster.modelo.entidad;

/**
 * Resumen de una importación desde la API (CU-01).
 *
 * @param recibidos   países válidos recibidos de la API
 * @param nuevos      países que no existían y se insertaron
 * @param actualizados países que ya existían (mismo código) y se refrescaron
 * @author carlo
 */
public record ResultadoImportacion(int recibidos, int nuevos, int actualizados) {

    public static ResultadoImportacion vacio() {
        return new ResultadoImportacion(0, 0, 0);
    }
}
