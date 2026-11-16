package ec.edu.monster.modelo.api;

import ec.edu.monster.modelo.entidad.Pais;
import java.util.List;

/**
 * Fuente externa de países. El servicio depende de esta abstracción (DIP), de
 * modo que cambiar de proveedor (otra API, un archivo, un doble de prueba) no
 * obliga a modificarlo (OCP).
 * <p>
 * Las implementaciones lanzan ExcepcionApiExterna si el proveedor no está
 * disponible o responde con error.
 *
 * @author carlo
 */
public interface PaisApiCliente {

    /**
     * Todos los países que ofrece el proveedor.
     */
    List<Pais> obtenerTodos();

    /**
     * Países cuyo nombre contiene el texto. Lista vacía si no hay coincidencias.
     */
    List<Pais> buscarPorNombre(String nombre);

    /**
     * {@code true} si el proveedor solo entrega datos de muestra (p. ej. una
     * clave de demostración). No hace llamadas de red.
     */
    default boolean esModoDemostracion() {
        return false;
    }
}
