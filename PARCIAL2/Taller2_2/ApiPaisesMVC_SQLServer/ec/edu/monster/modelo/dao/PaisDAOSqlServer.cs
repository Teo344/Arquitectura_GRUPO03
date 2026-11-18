using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Microsoft.EntityFrameworkCore;

namespace ec.edu.monster.modelo.dao
{
    /// <summary>
    /// Implementación de <see cref="IPaisDAO"/> para SQL Server con Entity Framework Core. Las
    /// consultas LINQ se traducen a SQL parametrizado (sin riesgo de inyección SQL). Las lecturas no
    /// rastrean entidades: el servicio y la vista reciben copias que no cambian la base por sorpresa.
    /// </summary>
    public class PaisDAOSqlServer : IPaisDAO
    {
        private readonly AppDbContext contexto;
        private readonly ILogger<PaisDAOSqlServer> log;

        public PaisDAOSqlServer(AppDbContext contexto, ILogger<PaisDAOSqlServer> log)
        {
            this.contexto = contexto ?? throw new ArgumentNullException(nameof(contexto));
            this.log = log ?? throw new ArgumentNullException(nameof(log));
        }

        private IQueryable<Pais> Consulta => contexto.Paises.AsNoTracking();

        public Task<Pais> InsertarAsync(Pais pais)
        {
            return ErroresBD.EjecutarAsync(log, "registrar el país", async () =>
            {
                Pais nuevo = pais.Copiar();
                nuevo.Id = 0;
                contexto.Paises.Add(nuevo);
                await contexto.SaveChangesAsync();
                contexto.Entry(nuevo).State = EntityState.Detached;
                return nuevo;
            }, () => CodigoDuplicado(pais.Codigo));
        }

        public Task<bool> ActualizarAsync(Pais pais)
        {
            return ErroresBD.EjecutarAsync(log, "actualizar el país", async () =>
            {
                Pais? guardado = await contexto.Paises.FindAsync(pais.Id);
                if (guardado == null)
                {
                    return false;
                }
                guardado.CopiarDatosDe(pais);
                await contexto.SaveChangesAsync();
                contexto.Entry(guardado).State = EntityState.Detached;
                return true;
            }, () => CodigoDuplicado(pais.Codigo));
        }

        public Task<bool> EliminarAsync(int id)
        {
            return ErroresBD.EjecutarAsync(log, "eliminar el país", async () =>
                await contexto.Paises.Where(p => p.Id == id).ExecuteDeleteAsync() > 0);
        }

        public Task<Pais?> BuscarPorIdAsync(int id)
        {
            return ErroresBD.EjecutarAsync(log, "consultar el país",
                () => Consulta.FirstOrDefaultAsync(p => p.Id == id));
        }

        public Task<Pais?> BuscarPorCodigoAsync(string codigo)
        {
            string buscado = codigo.Trim().ToUpperInvariant();
            return ErroresBD.EjecutarAsync(log, "consultar el país",
                () => Consulta.FirstOrDefaultAsync(p => p.Codigo == buscado));
        }

        public Task<IReadOnlyList<Pais>> ListarTodosAsync()
        {
            return ErroresBD.EjecutarAsync(log, "listar los países", async () =>
                (IReadOnlyList<Pais>)await Consulta.OrderBy(p => p.Nombre).ToListAsync());
        }

        public Task<IReadOnlyList<Pais>> BuscarPorRegionAsync(Region region)
        {
            Region? buscada = region;
            return ErroresBD.EjecutarAsync(log, "buscar los países de la región", async () =>
                (IReadOnlyList<Pais>)await Consulta.Where(p => p.Region == buscada)
                    .OrderBy(p => p.Nombre).ToListAsync());
        }

        public Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string fragmento)
        {
            string texto = fragmento.Trim();
            return ErroresBD.EjecutarAsync(log, "buscar los países", async () =>
                (IReadOnlyList<Pais>)await Consulta.Where(p => p.Nombre!.Contains(texto))
                    .OrderBy(p => p.Nombre).ToListAsync());
        }

        public Task<IReadOnlyList<Pais>> BuscarAsync(FiltroPaises filtro)
        {
            return ErroresBD.EjecutarAsync(log, "buscar los países", async () =>
            {
                IQueryable<Pais> consulta = Consulta;
                if (filtro.Region != null)
                {
                    Region? region = filtro.Region;
                    consulta = consulta.Where(p => p.Region == region);
                }
                if (filtro.TieneTexto)
                {
                    string texto = filtro.Texto!.Trim();
                    switch (filtro.Campo)
                    {
                        case CampoBusqueda.CAPITAL:
                            consulta = consulta.Where(p => p.Capital!.Contains(texto));
                            break;
                        case CampoBusqueda.REGION:
                            // Coincide con el nombre de la región (en español o inglés) o con la subregión
                            List<Region?> regiones = RegionesQueContienen(texto);
                            consulta = consulta.Where(p => regiones.Contains(p.Region) || p.SubRegion!.Contains(texto));
                            break;
                        default:
                            string codigo = texto.ToUpperInvariant();
                            consulta = consulta.Where(p => p.Nombre!.Contains(texto) || p.Codigo == codigo);
                            break;
                    }
                }
                return (IReadOnlyList<Pais>)await consulta.OrderBy(p => p.Nombre).ToListAsync();
            });
        }

        public Task<ResultadoImportacion> GuardarOActualizarAsync(IReadOnlyList<Pais> paises)
        {
            if (paises.Count == 0)
            {
                return Task.FromResult(ResultadoImportacion.Vacio());
            }
            return ErroresBD.EjecutarAsync(log, "guardar los países", async () =>
            {
                List<string> codigos = paises.Select(p => p.Codigo!).Distinct().ToList();
                await using var transaccion = await contexto.Database.BeginTransactionAsync();
                Dictionary<string, Pais> existentes = await contexto.Paises
                    .Where(p => codigos.Contains(p.Codigo!))
                    .ToDictionaryAsync(p => p.Codigo!);

                int nuevos = 0;
                int actualizados = 0;
                foreach (Pais pais in paises)
                {
                    if (existentes.TryGetValue(pais.Codigo!, out Pais? guardado))
                    {
                        guardado.CopiarDatosDe(pais);
                        actualizados++;
                    }
                    else
                    {
                        Pais nuevo = pais.Copiar();
                        nuevo.Id = 0;
                        contexto.Paises.Add(nuevo);
                        existentes[nuevo.Codigo!] = nuevo;
                        nuevos++;
                    }
                }
                await contexto.SaveChangesAsync();
                await transaccion.CommitAsync();
                contexto.ChangeTracker.Clear();
                return new ResultadoImportacion(paises.Count, nuevos, actualizados);
            });
        }

        public Task<IReadOnlyDictionary<Region, long>> ContarPorRegionAsync()
        {
            // SELECT Region, COUNT(*) Total FROM Paises GROUP BY Region
            return ErroresBD.EjecutarAsync(log, "contar los países por región", async () =>
            {
                var filas = await Consulta.GroupBy(p => p.Region)
                    .Select(g => new { Region = g.Key, Total = g.LongCount() })
                    .ToListAsync();
                return (IReadOnlyDictionary<Region, long>)filas
                    .Where(f => f.Region != null)
                    .ToDictionary(f => f.Region!.Value, f => f.Total);
            });
        }

        public Task<Pais?> ObtenerMasPobladoAsync()
        {
            // SELECT TOP 1 * FROM Paises ORDER BY Poblacion DESC
            return ErroresBD.EjecutarAsync(log, "obtener el país más poblado",
                () => Consulta.OrderByDescending(p => p.Poblacion).ThenBy(p => p.Nombre).FirstOrDefaultAsync());
        }

        public Task<IReadOnlyDictionary<Region, double>> PromedioPoblacionPorRegionAsync()
        {
            // SELECT Region, AVG(Poblacion) FROM Paises GROUP BY Region
            return ErroresBD.EjecutarAsync(log, "calcular el promedio poblacional", async () =>
            {
                var filas = await Consulta.GroupBy(p => p.Region)
                    .Select(g => new { Region = g.Key, Promedio = g.Average(p => (double)p.Poblacion) })
                    .ToListAsync();
                return (IReadOnlyDictionary<Region, double>)filas
                    .Where(f => f.Region != null)
                    .ToDictionary(f => f.Region!.Value, f => f.Promedio);
            });
        }

        public Task<long> ContarAsync()
        {
            return ErroresBD.EjecutarAsync(log, "contar los países", () => Consulta.LongCountAsync());
        }

        private static List<Region?> RegionesQueContienen(string texto)
        {
            string buscado = RegionExtensiones.Normalizar(texto);
            return RegionExtensiones.Todas
                .Where(r => RegionExtensiones.Normalizar(r.Nombre()).Contains(buscado)
                    || RegionExtensiones.Normalizar(r.ValorApi()).Contains(buscado))
                .Select(r => (Region?)r)
                .ToList();
        }

        private static ExcepcionValidacion CodigoDuplicado(string? codigo)
        {
            return new ExcepcionValidacion("Ya existe un país con el código " + codigo + ".");
        }
    }
}
