using ec.edu.monster.modelo.entidad;
using Microsoft.EntityFrameworkCore;

namespace ec.edu.monster.modelo.dao
{
    /// <summary>Implementación de <see cref="IUniversidadDAO"/> para SQL Server con Entity Framework Core.</summary>
    public class UniversidadDAOSqlServer : IUniversidadDAO
    {
        private readonly AppDbContext contexto;
        private readonly ILogger<UniversidadDAOSqlServer> log;

        public UniversidadDAOSqlServer(AppDbContext contexto, ILogger<UniversidadDAOSqlServer> log)
        {
            this.contexto = contexto ?? throw new ArgumentNullException(nameof(contexto));
            this.log = log ?? throw new ArgumentNullException(nameof(log));
        }

        public Task<ResultadoImportacion> GuardarOActualizarAsync(IReadOnlyList<Universidad> universidades)
        {
            if (universidades.Count == 0)
            {
                return Task.FromResult(ResultadoImportacion.Vacio());
            }
            return ErroresBD.EjecutarAsync(log, "guardar las universidades", async () =>
            {
                List<string> paises = universidades.Select(u => u.Pais!).Distinct().ToList();
                await using var transaccion = await contexto.Database.BeginTransactionAsync();
                List<Universidad> guardadas = await contexto.Universidades
                    .Where(u => paises.Contains(u.Pais!))
                    .ToListAsync();
                Dictionary<string, Universidad> existentes = guardadas
                    .GroupBy(Clave, StringComparer.OrdinalIgnoreCase)
                    .ToDictionary(g => g.Key, g => g.First(), StringComparer.OrdinalIgnoreCase);

                int nuevas = 0;
                int actualizadas = 0;
                foreach (Universidad universidad in universidades)
                {
                    if (existentes.TryGetValue(Clave(universidad), out Universidad? guardada))
                    {
                        guardada.Dominio = universidad.Dominio;
                        guardada.SitioWeb = universidad.SitioWeb;
                        actualizadas++;
                    }
                    else
                    {
                        Universidad nueva = Universidad.Nueva(universidad.Nombre, universidad.Dominio,
                            universidad.SitioWeb, universidad.Pais);
                        contexto.Universidades.Add(nueva);
                        existentes[Clave(nueva)] = nueva;
                        nuevas++;
                    }
                }
                await contexto.SaveChangesAsync();
                await transaccion.CommitAsync();
                contexto.ChangeTracker.Clear();
                return new ResultadoImportacion(universidades.Count, nuevas, actualizadas);
            });
        }

        public Task<IReadOnlyList<Universidad>> ListarAsync(string? pais, string? texto)
        {
            return ErroresBD.EjecutarAsync(log, "listar las universidades", async () =>
            {
                IQueryable<Universidad> consulta = contexto.Universidades.AsNoTracking();
                if (!string.IsNullOrWhiteSpace(pais))
                {
                    string buscado = pais.Trim();
                    consulta = consulta.Where(u => u.Pais == buscado);
                }
                if (!string.IsNullOrWhiteSpace(texto))
                {
                    // WHERE Nombre LIKE @t OR Dominio LIKE @t OR SitioWeb LIKE @t (parametrizado por EF)
                    string fragmento = texto.Trim();
                    consulta = consulta.Where(u => (u.Nombre != null && u.Nombre.Contains(fragmento))
                        || (u.Dominio != null && u.Dominio.Contains(fragmento))
                        || (u.SitioWeb != null && u.SitioWeb.Contains(fragmento)));
                }
                return (IReadOnlyList<Universidad>)await consulta.OrderBy(u => u.Pais).ThenBy(u => u.Nombre)
                    .ToListAsync();
            });
        }

        public Task<IReadOnlyDictionary<string, long>> ContarPorPaisAsync()
        {
            return ErroresBD.EjecutarAsync(log, "contar las universidades", async () =>
            {
                var filas = await contexto.Universidades.AsNoTracking()
                    .GroupBy(u => u.Pais)
                    .Select(g => new { Pais = g.Key, Total = g.LongCount() })
                    .OrderBy(f => f.Pais)
                    .ToListAsync();
                return (IReadOnlyDictionary<string, long>)filas.ToDictionary(f => f.Pais!, f => f.Total);
            });
        }

        /// <summary>SQL Server compara sin distinguir mayúsculas: la clave en memoria hace lo mismo.</summary>
        private static string Clave(Universidad universidad)
        {
            return universidad.Pais + "|" + universidad.Nombre;
        }
    }
}
