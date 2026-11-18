using ec.edu.monster.modelo.entidad;

namespace ec.edu.monster.modelo.dao
{
    /// <summary>Contrato de persistencia de universidades (actividad avanzada).</summary>
    public interface IUniversidadDAO
    {
        /// <summary>
        /// Inserta las universidades nuevas y actualiza las existentes (mismo nombre y país) en una
        /// sola transacción.
        /// </summary>
        Task<ResultadoImportacion> GuardarOActualizarAsync(IReadOnlyList<Universidad> universidades);

        /// <summary>Universidades guardadas, ordenadas por país y nombre; filtra por país si se indica.</summary>
        Task<IReadOnlyList<Universidad>> ListarAsync(string? pais);

        /// <summary>Países con universidades guardadas y su cantidad, para los filtros.</summary>
        Task<IReadOnlyDictionary<string, long>> ContarPorPaisAsync();
    }
}
