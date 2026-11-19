using ec.edu.monster.modelo.dao;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Doble de prueba de <see cref="IPaisDAO"/> que guarda los países en memoria. Gracias a que el
    /// servicio depende de la interfaz (DIP), se prueba sin SQL Server (LSP: se sustituye sin cambios).
    /// </summary>
    public class PaisDAOEnMemoria : IPaisDAO
    {
        private readonly SortedDictionary<int, Pais> paises = new SortedDictionary<int, Pais>();
        private int siguienteId = 1;

        public int Cantidad => paises.Count;

        public IReadOnlyDictionary<string, Pais> PorCodigo()
        {
            return paises.Values.ToDictionary(p => p.Codigo!);
        }

        public virtual Task<Pais> InsertarAsync(Pais pais)
        {
            if (paises.Values.Any(p => p.Codigo == pais.Codigo))
            {
                throw new ExcepcionValidacion("Ya existe un país con el código " + pais.Codigo + ".");
            }
            Pais nuevo = pais.ConId(siguienteId++);
            paises[nuevo.Id] = nuevo.Copiar();
            return Task.FromResult(nuevo);
        }

        public virtual Task<bool> ActualizarAsync(Pais pais)
        {
            if (!paises.ContainsKey(pais.Id))
            {
                return Task.FromResult(false);
            }
            paises[pais.Id] = pais.Copiar();
            return Task.FromResult(true);
        }

        public virtual Task<bool> EliminarAsync(int id)
        {
            return Task.FromResult(paises.Remove(id));
        }

        public virtual Task<Pais?> BuscarPorIdAsync(int id)
        {
            return Task.FromResult(paises.TryGetValue(id, out Pais? pais) ? pais.Copiar() : null);
        }

        public virtual Task<Pais?> BuscarPorCodigoAsync(string codigo)
        {
            return Task.FromResult(paises.Values.FirstOrDefault(p => p.Codigo == codigo)?.Copiar());
        }

        public virtual Task<IReadOnlyList<Pais>> ListarTodosAsync()
        {
            return Lista(paises.Values);
        }

        public virtual Task<IReadOnlyList<Pais>> BuscarPorRegionAsync(Region region)
        {
            return Lista(paises.Values.Where(p => p.Region == region));
        }

        public virtual Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string fragmento)
        {
            return Lista(paises.Values.Where(p => Contiene(p.Nombre, fragmento)));
        }

        public virtual Task<IReadOnlyList<Pais>> BuscarAsync(FiltroPaises filtro)
        {
            IEnumerable<Pais> consulta = paises.Values;
            if (filtro.Region != null)
            {
                consulta = consulta.Where(p => p.Region == filtro.Region);
            }
            if (filtro.TieneTexto)
            {
                string texto = filtro.Texto!.Trim();
                consulta = filtro.Campo switch
                {
                    CampoBusqueda.CAPITAL => consulta.Where(p => Contiene(p.Capital, texto)),
                    CampoBusqueda.REGION => consulta.Where(p => Contiene(p.Region?.Nombre(), texto) || Contiene(p.SubRegion, texto)),
                    _ => consulta.Where(p => Contiene(p.Nombre, texto) || string.Equals(p.Codigo, texto, StringComparison.OrdinalIgnoreCase))
                };
            }
            return Lista(consulta);
        }

        public virtual Task<ResultadoImportacion> GuardarOActualizarAsync(IReadOnlyList<Pais> lista)
        {
            int nuevos = 0;
            int actualizados = 0;
            foreach (Pais pais in lista)
            {
                Pais? existente = paises.Values.FirstOrDefault(p => p.Codigo == pais.Codigo);
                if (existente == null)
                {
                    Pais nuevo = pais.ConId(siguienteId++);
                    paises[nuevo.Id] = nuevo;
                    nuevos++;
                }
                else
                {
                    paises[existente.Id] = pais.ConId(existente.Id);
                    actualizados++;
                }
            }
            return Task.FromResult(new ResultadoImportacion(lista.Count, nuevos, actualizados));
        }

        public virtual Task<IReadOnlyDictionary<Region, long>> ContarPorRegionAsync()
        {
            IReadOnlyDictionary<Region, long> conteo = paises.Values
                .GroupBy(p => p.Region!.Value)
                .ToDictionary(g => g.Key, g => g.LongCount());
            return Task.FromResult(conteo);
        }

        public virtual Task<IReadOnlyList<Pais>> ListarMasPobladosAsync(int limite)
        {
            IReadOnlyList<Pais> ranking = paises.Values.OrderByDescending(p => p.Poblacion).ThenBy(p => p.Nombre)
                .Take(Math.Max(0, limite)).Select(p => p.Copiar()).ToList();
            return Task.FromResult(ranking);
        }

        public virtual Task<IReadOnlyDictionary<Region, double>> PromedioPoblacionPorRegionAsync()
        {
            IReadOnlyDictionary<Region, double> promedio = paises.Values
                .GroupBy(p => p.Region!.Value)
                .ToDictionary(g => g.Key, g => g.Average(p => (double)p.Poblacion));
            return Task.FromResult(promedio);
        }

        public virtual Task<long> ContarAsync()
        {
            return Task.FromResult((long)paises.Count);
        }

        private static Task<IReadOnlyList<Pais>> Lista(IEnumerable<Pais> origen)
        {
            IReadOnlyList<Pais> lista = origen.OrderBy(p => p.Nombre, StringComparer.CurrentCulture)
                .Select(p => p.Copiar()).ToList();
            return Task.FromResult(lista);
        }

        private static bool Contiene(string? valor, string fragmento)
        {
            return valor != null && valor.Contains(fragmento.Trim(), StringComparison.OrdinalIgnoreCase);
        }
    }
}
