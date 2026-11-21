using System.Text.RegularExpressions;
using ec.edu.monster.modelo.api;
using ec.edu.monster.modelo.dao;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;

namespace ec.edu.monster.servicio;

/// <summary>
/// Reglas de negocio de los países: valida, orquesta la API y la base de datos y arma los reportes.
/// Recibe sus dependencias por constructor como interfaces, así se prueba con dobles en memoria sin
/// red ni SQL Server.
/// </summary>
public sealed partial class PaisServicio(IPaisDAO dao, IPaisApiCliente api, ILogger<PaisServicio>? logger = null)
{
    /// <summary>Tamaño de las columnas NVARCHAR(100) de la tabla pais.</summary>
    internal const int LongitudMaxima = 100;

    private readonly IPaisDAO dao = dao ?? throw new ArgumentNullException(nameof(dao));
    private readonly IPaisApiCliente api = api ?? throw new ArgumentNullException(nameof(api));
    private readonly ILogger log = (ILogger?)logger ?? NullLogger.Instance;

    [GeneratedRegex("^[A-Z]{3}$")]
    private static partial Regex CodigoIso();

    /// <summary>
    /// Comprueba la base de datos e informa cómo está configurada la API. No consulta la API para
    /// no gastar la cuota mensual de solicitudes.
    /// </summary>
    public async Task<EstadoSistema> VerificarEstadoAsync()
    {
        bool demostracion = api.EsModoDemostracion;
        try
        {
            return new EstadoSistema(true, await dao.ContarAsync(), null, demostracion);
        }
        catch (ExcepcionPersistencia e)
        {
            return new EstadoSistema(false, 0, e.Message, demostracion);
        }
    }

    /// <summary>
    /// CU-01: trae todos los países de la API y los guarda. Reimportar no duplica: los países ya
    /// registrados (mismo código) se actualizan. Si la API falla, la base no se toca.
    /// </summary>
    public async Task<ResultadoImportacion> ImportarDesdeApiAsync()
    {
        Dictionary<string, Pais> porCodigo = [];
        foreach (Pais pais in await api.ObtenerTodosAsync())
        {
            IReadOnlyList<string> errores = Validar(pais);
            if (errores.Count == 0)
            {
                porCodigo.TryAdd(pais.Codigo!, pais);
            }
            else
            {
                log.LogWarning("Se omite {Pais} de la importación: {Errores}", pais.Nombre, string.Join(" ", errores));
            }
        }
        return await dao.GuardarOActualizarAsync([.. porCodigo.Values]);
    }

    /// <summary>
    /// CU-02 (Actividad 1): busca en la API. Si la API no está disponible, responde con los países
    /// ya guardados (patrón fallback) e indica el motivo en el resultado.
    /// </summary>
    public async Task<ResultadoBusqueda> BuscarPorNombreAsync(string? nombre)
    {
        if (string.IsNullOrWhiteSpace(nombre))
        {
            throw new ExcepcionValidacion("Ingrese el nombre del país a buscar.");
        }
        string texto = nombre.Trim();
        try
        {
            return ResultadoBusqueda.DesdeApi(await api.BuscarPorNombreAsync(texto));
        }
        catch (ExcepcionApiExterna e)
        {
            log.LogWarning("API no disponible, se responde con la base local: {Motivo}", e.Message);
            return ResultadoBusqueda.DesdeBaseLocal(await dao.BuscarPorNombreAsync(texto),
                e.Message + " Se muestran los países guardados localmente.");
        }
    }

    /// <summary>CU-02 (opcional): guarda un resultado de la búsqueda; si el código ya existe, lo actualiza.</summary>
    public Task<ResultadoImportacion> GuardarResultadoAsync(Pais pais)
    {
        ValidarOLanzar(pais);
        return dao.GuardarOActualizarAsync([pais]);
    }

    /// <summary>CU-03: registra un país ingresado por el usuario.</summary>
    public async Task<Pais> RegistrarAsync(Pais pais)
    {
        ArgumentNullException.ThrowIfNull(pais);
        if (!pais.EsNuevo)
        {
            throw new ExcepcionValidacion("Un país nuevo no debe tener id.");
        }
        ValidarOLanzar(pais);
        if (await dao.BuscarPorCodigoAsync(pais.Codigo!) is not null)
        {
            throw CodigoDuplicado(pais.Codigo!);
        }
        return await dao.InsertarAsync(pais);
    }

    /// <summary>CU-04: país por id.</summary>
    public async Task<Pais> ObtenerPorIdAsync(int id) => await dao.BuscarPorIdAsync(id) ?? throw NoEncontrado(id);

    /// <summary>CU-04 (Actividad 3): todos los países, ordenados por nombre.</summary>
    public Task<IReadOnlyList<Pais>> ListarTodosAsync() => dao.ListarTodosAsync();

    /// <summary>CU-05: actualiza un país existente.</summary>
    public async Task<Pais> ActualizarAsync(Pais pais)
    {
        ArgumentNullException.ThrowIfNull(pais);
        if (pais.Id is not int id)
        {
            throw new ExcepcionValidacion("Seleccione el país que desea actualizar.");
        }
        ValidarOLanzar(pais);
        Pais? conMismoCodigo = await dao.BuscarPorCodigoAsync(pais.Codigo!);
        if (conMismoCodigo is not null && conMismoCodigo.Id != id)
        {
            throw CodigoDuplicado(pais.Codigo!);
        }
        if (!await dao.ActualizarAsync(pais))
        {
            throw NoEncontrado(id);
        }
        return pais;
    }

    /// <summary>CU-06: elimina un país.</summary>
    public async Task EliminarAsync(int id)
    {
        if (!await dao.EliminarAsync(id))
        {
            throw NoEncontrado(id);
        }
    }

    /// <summary>CU-07 (Actividad 4): países de una región.</summary>
    public Task<IReadOnlyList<Pais>> BuscarPorRegionAsync(Region? region)
    {
        return region is Region elegida
            ? dao.BuscarPorRegionAsync(elegida)
            : throw new ExcepcionValidacion("Seleccione una región.");
    }

    /// <summary>CU-08 (Actividad 5): países por región, país más poblado y región con más países.</summary>
    public async Task<EstadisticasPaises> GenerarEstadisticasAsync()
    {
        // Se ordena aquí (y no se confía en el orden del DAO): mayor cantidad primero y, ante
        // empate, el orden del enum
        List<ConteoRegion> porRegion = [.. (await dao.ContarPorRegionAsync())
            .OrderByDescending(c => c.Cantidad)
            .ThenBy(c => c.Region)];

        long total = porRegion.Sum(c => c.Cantidad);
        if (total == 0)
        {
            return new EstadisticasPaises([], 0, null, null);
        }
        return new EstadisticasPaises(porRegion, total, await dao.ObtenerMasPobladoAsync(), porRegion[0].Region);
    }

    // ------------------------------------------------------------ validación

    /// <summary>Reglas de un país válido. Devuelve todos los errores (lista vacía si es válido).</summary>
    internal static IReadOnlyList<string> Validar(Pais pais)
    {
        List<string> errores = [];
        if (pais.Codigo is null || !CodigoIso().IsMatch(pais.Codigo))
        {
            errores.Add("El código debe tener 3 letras (ISO alfa-3, p. ej. ECU).");
        }
        if (pais.Nombre is null)
        {
            errores.Add("El nombre es obligatorio.");
        }
        if (pais.Region is null)
        {
            errores.Add("La región es obligatoria.");
        }
        if (pais.Poblacion < 0)
        {
            errores.Add("La población no puede ser negativa.");
        }
        ValidarLongitud(pais.Nombre, "El nombre", errores);
        ValidarLongitud(pais.Capital, "La capital", errores);
        ValidarLongitud(pais.Moneda, "La moneda", errores);
        return errores;
    }

    private static void ValidarOLanzar(Pais pais)
    {
        ArgumentNullException.ThrowIfNull(pais);
        IReadOnlyList<string> errores = Validar(pais);
        if (errores.Count > 0)
        {
            throw new ExcepcionValidacion(errores);
        }
    }

    private static void ValidarLongitud(string? valor, string campo, List<string> errores)
    {
        if (valor is not null && valor.Length > LongitudMaxima)
        {
            errores.Add($"{campo} no puede superar {LongitudMaxima} caracteres.");
        }
    }

    private static ExcepcionValidacion CodigoDuplicado(string codigo) => new($"Ya existe un país con el código {codigo}.");

    private static ExcepcionNoEncontrado NoEncontrado(int id) => new($"No existe un país con id {id}.");
}
