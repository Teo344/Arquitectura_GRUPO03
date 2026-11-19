package ec.edu.monster.modelo.dao;

import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.entidad.ResultadoImportacion;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrato de persistencia de países. El servicio depende de esta interfaz y
 * no de MySQL (inversión de dependencias): en las pruebas se usa una versión
 * en memoria y se podría cambiar de motor sin tocar el servicio.
 * <p>
 * Todas las operaciones lanzan ExcepcionPersistencia ante fallas de la base
 * y ExcepcionValidacion si se viola el código único.
 *
 * @author carlo
 */
public interface PaisDAO {

    /**
     * @return el país con el id generado
     */
    Pais insertar(Pais pais);

    /**
     * @return {@code false} si no existe un país con ese id
     */
    boolean actualizar(Pais pais);

    /**
     * @return {@code false} si no existe un país con ese id
     */
    boolean eliminar(int id);

    Optional<Pais> buscarPorId(int id);

    Optional<Pais> buscarPorCodigo(String codigo);

    /**
     * Todos los países ordenados por nombre.
     */
    List<Pais> listarTodos();

    List<Pais> buscarPorRegion(Region region);

    /**
     * Países cuyo nombre contiene el fragmento (sin distinguir mayúsculas).
     */
    List<Pais> buscarPorNombre(String fragmento);

    /**
     * Inserta los países nuevos y actualiza los existentes (mismo código) en
     * una sola transacción: o se guardan todos o ninguno.
     */
    ResultadoImportacion guardarOActualizar(List<Pais> paises);

    /**
     * Cantidad de países por región, de mayor a menor. Solo incluye regiones
     * con al menos un país.
     */
    Map<Region, Long> contarPorRegion();

    /**
     * Los países con más habitantes, de mayor a menor (ante empate, por nombre).
     *
     * @param limite cuántos países devolver como máximo
     */
    List<Pais> listarMasPoblados(int limite);

    /**
     * Cantidad de países guardados. Sirve también para comprobar la conexión.
     */
    long contar();
}
