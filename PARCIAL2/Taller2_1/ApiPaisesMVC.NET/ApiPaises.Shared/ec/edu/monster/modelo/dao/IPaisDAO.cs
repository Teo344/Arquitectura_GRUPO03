using ec.edu.monster.modelo.entidad;

namespace ec.edu.monster.modelo.dao;

/// <summary>
/// Contrato de persistencia de países. El servicio depende de esta interfaz y no de SQL Server
/// (inversión de dependencias): en las pruebas se usa una versión en memoria y se podría cambiar de
/// motor sin tocar el servicio. Las operaciones lanzan ExcepcionPersistencia ante fallas de la base y
/// ExcepcionValidacion si se viola el código único.
/// </summary>
public interface IPaisDAO
{
    /// <returns>el país con el id generado</returns>
    Task<Pais> InsertarAsync(Pais pais);

    /// <returns>false si no existe un país con ese id</returns>
    Task<bool> ActualizarAsync(Pais pais);

    /// <returns>false si no existe un país con ese id</returns>
    Task<bool> EliminarAsync(int id);

    Task<Pais?> BuscarPorIdAsync(int id);

    Task<Pais?> BuscarPorCodigoAsync(string codigo);

    /// <summary>Todos los países ordenados por nombre.</summary>
    Task<IReadOnlyList<Pais>> ListarTodosAsync();

    Task<IReadOnlyList<Pais>> BuscarPorRegionAsync(Region region);

    /// <summary>Países cuyo nombre contiene el fragmento (sin distinguir mayúsculas ni tildes).</summary>
    Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string fragmento);

    /// <summary>
    /// Inserta los países nuevos y actualiza los existentes (mismo código) en una sola transacción:
    /// o se guardan todos o ninguno.
    /// </summary>
    Task<ResultadoImportacion> GuardarOActualizarAsync(IReadOnlyList<Pais> paises);

    /// <summary>Cantidad de países por región, de mayor a menor (solo regiones con países).</summary>
    Task<IReadOnlyList<ConteoRegion>> ContarPorRegionAsync();

    Task<Pais?> ObtenerMasPobladoAsync();

    /// <summary>Cantidad de países guardados. Sirve también para comprobar la conexión.</summary>
    Task<long> ContarAsync();
}
