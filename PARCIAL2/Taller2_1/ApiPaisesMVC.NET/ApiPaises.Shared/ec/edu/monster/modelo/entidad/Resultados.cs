namespace ec.edu.monster.modelo.entidad;

/// <summary>Resumen de una importación desde la API (CU-01).</summary>
/// <param name="Recibidos">países válidos recibidos de la API</param>
/// <param name="Nuevos">países que no existían y se insertaron</param>
/// <param name="Actualizados">países que ya existían (mismo código) y se refrescaron</param>
public sealed record ResultadoImportacion(int Recibidos, int Nuevos, int Actualizados)
{
    public static ResultadoImportacion Vacio { get; } = new(0, 0, 0);
}

/// <summary>De dónde salió el resultado de una búsqueda.</summary>
public enum OrigenDatos
{
    /// <summary>Respuesta en vivo de REST Countries.</summary>
    ApiExterna,

    /// <summary>Respaldo: la API falló y se respondió con lo ya guardado en SQL Server.</summary>
    BaseLocal
}

/// <summary>
/// Resultado de buscar países por nombre (CU-02). Indica el origen para que la vista avise cuando
/// los datos vienen del respaldo local.
/// </summary>
public sealed record ResultadoBusqueda(IReadOnlyList<Pais> Paises, OrigenDatos Origen, string? Aviso)
{
    public static ResultadoBusqueda DesdeApi(IReadOnlyList<Pais> paises) => new(paises, OrigenDatos.ApiExterna, null);

    public static ResultadoBusqueda DesdeBaseLocal(IReadOnlyList<Pais> paises, string aviso) =>
        new(paises, OrigenDatos.BaseLocal, aviso);

    public bool EstaVacio => Paises.Count == 0;
}

/// <summary>Cantidad de países de una región.</summary>
public sealed record ConteoRegion(Region Region, long Cantidad);

/// <summary>Reporte estadístico (CU-08 / Actividad 5).</summary>
/// <param name="PaisesPorRegion">cantidad por región, de mayor a menor</param>
/// <param name="PaisMasPoblado">null si no hay países</param>
/// <param name="RegionConMasPaises">null si no hay países; ante empate, la primera de la lista</param>
public sealed record EstadisticasPaises(IReadOnlyList<ConteoRegion> PaisesPorRegion, long TotalPaises,
    Pais? PaisMasPoblado, Region? RegionConMasPaises)
{
    public bool EstaVacio => TotalPaises == 0;
}

/// <summary>
/// Estado de las dependencias externas al abrir la aplicación, para que el usuario sepa antes de
/// trabajar si la base de datos responde y qué datos entregará la API.
/// </summary>
public sealed record EstadoSistema(bool BaseDisponible, long TotalPaises, string? DetalleBase, bool ApiDemostracion);
