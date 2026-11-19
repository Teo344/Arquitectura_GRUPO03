using ec.edu.monster.modelo.api;
using ec.edu.monster.modelo.dao;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Reglas de negocio de las universidades (actividad avanzada): consulta la API pública de
    /// Hipolabs por país y guarda el resultado en SQL Server sin duplicar.
    /// </summary>
    public class UniversidadServicio
    {
        internal const int LongitudPais = 100;

        private readonly IUniversidadDAO dao;
        private readonly IUniversidadApiCliente api;

        public UniversidadServicio(IUniversidadDAO dao, IUniversidadApiCliente api)
        {
            this.dao = dao ?? throw new ArgumentNullException(nameof(dao));
            this.api = api ?? throw new ArgumentNullException(nameof(api));
        }

        /// <summary>
        /// CU-09: importa las universidades de un país. Las repetidas (mismo nombre) se guardan una
        /// vez; las que ya estaban se actualizan.
        /// </summary>
        /// <exception cref="ExcepcionApiExterna">si la API falla; en ese caso la base no se toca</exception>
        public async Task<ResultadoImportacion> ImportarPorPaisAsync(string? pais)
        {
            string buscado = ValidarPais(pais);
            IReadOnlyList<Universidad> recibidas = await api.BuscarPorPaisAsync(buscado);
            List<Universidad> unicas = recibidas
                .Where(u => u.Nombre != null && u.Nombre.Length <= 200)
                .Select(u =>
                {
                    u.Pais ??= buscado;
                    u.Dominio = Recortar(u.Dominio, 100);
                    u.SitioWeb = Recortar(u.SitioWeb, 200);
                    return u;
                })
                .GroupBy(u => u.Pais + "|" + u.Nombre, StringComparer.OrdinalIgnoreCase)
                .Select(g => g.First())
                .ToList();
            return await dao.GuardarOActualizarAsync(unicas);
        }

        /// <summary>
        /// CU-10: universidades guardadas, de un país o de todos, opcionalmente filtradas por un texto
        /// que aparezca en el nombre, el dominio o el sitio web.
        /// </summary>
        public Task<IReadOnlyList<Universidad>> ListarAsync(string? pais, string? texto = null)
        {
            string? buscado = string.IsNullOrWhiteSpace(texto) ? null : texto.Trim();
            if (buscado != null && buscado.Length > 100)
            {
                throw new ExcepcionValidacion("El texto a buscar no puede superar 100 caracteres.");
            }
            return dao.ListarAsync(string.IsNullOrWhiteSpace(pais) ? null : pais.Trim(), buscado);
        }

        /// <summary>Países con universidades guardadas, para los filtros.</summary>
        public Task<IReadOnlyDictionary<string, long>> ContarPorPaisAsync()
        {
            return dao.ContarPorPaisAsync();
        }

        private static string ValidarPais(string? pais)
        {
            if (string.IsNullOrWhiteSpace(pais))
            {
                throw new ExcepcionValidacion("Ingrese el país (en inglés) cuyas universidades desea importar.");
            }
            string texto = pais.Trim();
            if (texto.Length > LongitudPais)
            {
                throw new ExcepcionValidacion("El país no puede superar " + LongitudPais + " caracteres.");
            }
            return texto;
        }

        private static string? Recortar(string? texto, int maximo)
        {
            return texto == null || texto.Length <= maximo ? texto : null;
        }
    }
}
