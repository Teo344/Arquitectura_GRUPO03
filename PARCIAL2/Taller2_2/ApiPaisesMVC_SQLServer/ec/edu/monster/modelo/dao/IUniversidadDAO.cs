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

        /// <summary>
        /// Universidades guardadas, ordenadas por país y nombre. Filtra por país y por un texto que
        /// aparezca en el nombre, el dominio o el sitio web (ambos opcionales y combinables).
        /// </summary>
        Task<IReadOnlyList<Universidad>> ListarAsync(string? pais, string? texto);

        /// <summary>Países con universidades guardadas y su cantidad, para los filtros.</summary>
        Task<IReadOnlyDictionary<string, long>> ContarPorPaisAsync();
    }
}
