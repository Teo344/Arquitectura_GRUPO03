using System.Text.RegularExpressions;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador;

/// <summary>
/// Datos tal como los escribe el usuario en el formulario de país (todo texto). La vista solo copia
/// los campos de la pantalla aquí; convertirlos y validarlos es trabajo del controlador y el servicio.
/// </summary>
public sealed record FormularioPais(string? Codigo, string? Nombre, string? Capital, string? Region,
    string? Poblacion, string? Moneda);

/// <summary>
/// Controlador de la gestión de países (CU-01 a CU-07). Recibe las acciones de la vista, convierte
/// la entrada de texto en objetos del modelo y delega en <see cref="PaisServicio"/>. No contiene
/// SQL, JSON ni reglas de negocio. Las fallas llegan a la vista como ExcepcionAplicacion con un
/// mensaje listo para mostrar.
/// </summary>
public sealed partial class PaisControlador(PaisServicio servicio)
{
    private readonly PaisServicio servicio = servicio ?? throw new ArgumentNullException(nameof(servicio));

    /// <summary>CU-01</summary>
    public Task<ResultadoImportacion> ImportarPaisesAsync() => servicio.ImportarDesdeApiAsync();

    /// <summary>CU-02 (Actividad 1)</summary>
    public Task<ResultadoBusqueda> BuscarEnApiAsync(string? nombre) => servicio.BuscarPorNombreAsync(nombre);

    /// <summary>CU-02: guarda un país elegido de los resultados de la búsqueda.</summary>
    public Task<ResultadoImportacion> GuardarResultadoAsync(Pais pais) => servicio.GuardarResultadoAsync(pais);

    /// <summary>CU-03</summary>
    public Task<Pais> RegistrarPaisAsync(FormularioPais formulario) => servicio.RegistrarAsync(Convertir(null, formulario));

    /// <summary>CU-04 (Actividad 3: datos de la tabla)</summary>
    public Task<IReadOnlyList<Pais>> ListarPaisesAsync() => servicio.ListarTodosAsync();

    /// <summary>CU-05</summary>
    public Task<Pais> ActualizarPaisAsync(int id, FormularioPais formulario) =>
        servicio.ActualizarAsync(Convertir(id, formulario));

    /// <summary>CU-06</summary>
    public Task EliminarPaisAsync(int id) => servicio.EliminarAsync(id);

    /// <summary>CU-07 (Actividad 4)</summary>
    public Task<IReadOnlyList<Pais>> BuscarPorRegionAsync(Region region) => servicio.BuscarPorRegionAsync(region);

    /// <summary>Opciones para la lista de regiones.</summary>
    public IReadOnlyList<Region> Regiones => modelo.entidad.Regiones.Todas;

    [GeneratedRegex(@"[\s.,'_]")]
    private static partial Regex SeparadoresMiles();

    /// <summary>
    /// Convierte el formulario en un Pais. Reúne los errores de formato de región y población; las
    /// demás reglas las valida el servicio.
    /// </summary>
    internal static Pais Convertir(int? id, FormularioPais formulario)
    {
        ArgumentNullException.ThrowIfNull(formulario);
        List<string> errores = [];

        Region? region = null;
        try
        {
            region = modelo.entidad.Regiones.DesdeTexto(formulario.Region);
        }
        catch (ExcepcionValidacion e)
        {
            errores.AddRange(e.Errores);
        }

        long poblacion = 0;
        try
        {
            poblacion = ConvertirPoblacion(formulario.Poblacion);
        }
        catch (ExcepcionValidacion e)
        {
            errores.AddRange(e.Errores);
        }

        if (errores.Count > 0)
        {
            throw new ExcepcionValidacion(errores);
        }
        return new Pais(id, formulario.Codigo, formulario.Nombre, formulario.Capital, region, poblacion,
            formulario.Moneda);
    }

    /// <summary>
    /// Acepta separadores de miles comunes ("18.001.000", "18,001,000", "18 001 000"): la población
    /// siempre es un número entero.
    /// </summary>
    internal static long ConvertirPoblacion(string? texto)
    {
        if (string.IsNullOrWhiteSpace(texto))
        {
            throw new ExcepcionValidacion("La población es obligatoria.");
        }
        string digitos = SeparadoresMiles().Replace(texto.Trim(), "");
        return long.TryParse(digitos, out long poblacion)
            ? poblacion
            : throw new ExcepcionValidacion("La población debe ser un número entero.");
    }
}

/// <summary>
/// Controlador de reportes (CU-08 / Actividad 5). Separado de PaisControlador para que cada
/// pantalla dependa solo de lo que usa (ISP); el formato del reporte lo decide la vista.
/// </summary>
public sealed class ReporteControlador(PaisServicio servicio)
{
    private readonly PaisServicio servicio = servicio ?? throw new ArgumentNullException(nameof(servicio));

    public Task<EstadisticasPaises> GenerarReporteAsync() => servicio.GenerarEstadisticasAsync();
}

/// <summary>Controlador de la pantalla de inicio: estado de la base de datos y de la API.</summary>
public sealed class InicioControlador(PaisServicio servicio)
{
    private readonly PaisServicio servicio = servicio ?? throw new ArgumentNullException(nameof(servicio));

    public Task<EstadoSistema> VerificarEstadoAsync() => servicio.VerificarEstadoAsync();
}
