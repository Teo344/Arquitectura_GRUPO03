using ec.edu.monster.modelo.entidad;

namespace ec.edu.monster.modelo.api
{
    /// <summary>
    /// Fuente externa de universidades (actividad avanzada). Igual que <see cref="IPaisApiCliente"/>,
    /// el servicio depende de esta abstracción y no de Hipolabs.
    /// </summary>
    public interface IUniversidadApiCliente
    {
        /// <summary>Universidades de un país (nombre en inglés, p. ej. Ecuador).</summary>
        Task<IReadOnlyList<Universidad>> BuscarPorPaisAsync(string pais);
    }
}
