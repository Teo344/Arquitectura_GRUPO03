using ec.edu.monster.modelo.api;
using ec.edu.monster.modelo.dao;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;

namespace ec.edu.monster.pruebas;

/// <summary>
/// Doble de prueba de <see cref="IPaisDAO"/>: guarda los países en un diccionario. Gracias a que el
/// servicio depende de la interfaz, las pruebas no necesitan SQL Server.
/// </summary>
internal class PaisDAOEnMemoria : IPaisDAO
{
    private readonly SortedDictionary<int, Pais> filas = [];
    private int secuencia;

    public int Cantidad => filas.Count;

    public Pais? PorCodigo(string codigo) => filas.Values.FirstOrDefault(p => p.Codigo == codigo);

    public Task<Pais> InsertarAsync(Pais pais)
    {
        if (PorCodigo(pais.Codigo!) is not null)
        {
            throw new ExcepcionValidacion($"Ya existe un país con el código {pais.Codigo}.");
        }
        Pais guardado = pais.ConId(++secuencia);
        filas[guardado.Id!.Value] = guardado;
        return Task.FromResult(guardado);
    }

    public Task<bool> ActualizarAsync(Pais pais)
    {
        if (!filas.ContainsKey(pais.Id!.Value))
        {
            return Task.FromResult(false);
        }
        filas[pais.Id.Value] = pais;
        return Task.FromResult(true);
    }

    public Task<bool> EliminarAsync(int id) => Task.FromResult(filas.Remove(id));

    public Task<Pais?> BuscarPorIdAsync(int id) => Task.FromResult(filas.GetValueOrDefault(id));

    public Task<Pais?> BuscarPorCodigoAsync(string codigo) => Task.FromResult(PorCodigo(codigo));

    public Task<IReadOnlyList<Pais>> ListarTodosAsync() =>
        Task.FromResult<IReadOnlyList<Pais>>([.. filas.Values.OrderBy(p => p.Nombre)]);

    public async Task<IReadOnlyList<Pais>> BuscarPorRegionAsync(Region region) =>
        [.. (await ListarTodosAsync()).Where(p => p.Region == region)];

    public async Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string fragmento) =>
        [.. (await ListarTodosAsync()).Where(p => p.Nombre!.Contains(fragmento, StringComparison.OrdinalIgnoreCase))];

    public async Task<ResultadoImportacion> GuardarOActualizarAsync(IReadOnlyList<Pais> paises)
    {
        int nuevos = 0;
        foreach (Pais pais in paises)
        {
            if (PorCodigo(pais.Codigo!) is Pais existente)
            {
                filas[existente.Id!.Value] = pais.ConId(existente.Id.Value);
            }
            else
            {
                await InsertarAsync(pais);
                nuevos++;
            }
        }
        return new ResultadoImportacion(paises.Count, nuevos, paises.Count - nuevos);
    }

    /// <summary>Sin orden a propósito: el servicio es quien ordena.</summary>
    public Task<IReadOnlyList<ConteoRegion>> ContarPorRegionAsync() =>
        Task.FromResult<IReadOnlyList<ConteoRegion>>(
            [.. filas.Values.GroupBy(p => p.Region!.Value).Select(g => new ConteoRegion(g.Key, g.LongCount()))]);

    public Task<Pais?> ObtenerMasPobladoAsync() =>
        Task.FromResult(filas.Values.OrderByDescending(p => p.Poblacion).FirstOrDefault());

    public virtual Task<long> ContarAsync() => Task.FromResult((long)filas.Count);
}

/// <summary>API falsa: devuelve la lista configurada o falla si se indica.</summary>
internal sealed class ApiFalsa : IPaisApiCliente
{
    public List<Pais> Paises { get; set; } = [];
    public bool Caida { get; set; }

    public Task<IReadOnlyList<Pais>> ObtenerTodosAsync()
    {
        FallarSiEstaCaida();
        return Task.FromResult<IReadOnlyList<Pais>>(Paises);
    }

    public Task<IReadOnlyList<Pais>> BuscarPorNombreAsync(string nombre)
    {
        FallarSiEstaCaida();
        return Task.FromResult<IReadOnlyList<Pais>>(
            [.. Paises.Where(p => p.Nombre!.Contains(nombre, StringComparison.OrdinalIgnoreCase))]);
    }

    private void FallarSiEstaCaida()
    {
        if (Caida)
        {
            throw new ExcepcionApiExterna("La API REST Countries no respondió a tiempo.");
        }
    }
}

/// <summary>DAO cuya base de datos no responde.</summary>
internal sealed class PaisDAOCaido : PaisDAOEnMemoria
{
    public override Task<long> ContarAsync() =>
        throw new ExcepcionPersistencia("No se pudo conectar a la base de datos.");
}
