using ec.edu.monster.modelo.entidad;

namespace ec.edu.monster.modelo.api;

/// <summary>
/// Fuente externa de países. El servicio depende de esta abstracción (DIP), de modo que cambiar de
/// proveedor (otra API, un archivo, un doble de prueba) no obliga a modificarlo (OCP).
/// Las implementaciones lanzan ExcepcionApiExterna si el proveedor no está disponible.
/// </summary>
public interface IPaisApiCliente
{
    /// <summary>Todos los países que ofrece el proveedor.</summary>
    Task<IReadOnlyList<Pais>> ObtenerTodosAsync();

    /// <summary>Países cuyo nombre contiene el texto. Lista vacía si no hay coincidencias.</summary>
    Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string nombre);

    /// <summary>True si el proveedor solo entrega datos de muestra (p. ej. una clave de demostración).</summary>
    bool EsModoDemostracion => false;
}
