namespace ec.edu.monster.modelo.entidad;

/// <summary>
/// País almacenado en la tabla <c>pais</c>. Es inmutable: para cambiar un dato se crea otra
/// instancia, así ningún componente modifica por sorpresa un objeto que otra
/// capa sigue usando. El constructor solo normaliza los textos (recorta espacios, código en
/// mayúsculas, vacíos como null); las reglas de negocio se validan en PaisServicio.
/// </summary>
public sealed record Pais
{
    public Pais(int? id, string? codigo, string? nombre, string? capital, Region? region, long poblacion,
        string? moneda)
    {
        Id = id;
        Codigo = Limpiar(codigo)?.ToUpperInvariant();
        Nombre = Limpiar(nombre);
        Capital = Limpiar(capital);
        Region = region;
        Poblacion = poblacion;
        Moneda = Limpiar(moneda);
    }

    /// <summary>Clave generada por la base de datos; null si aún no se guarda.</summary>
    public int? Id { get; init; }

    /// <summary>Código ISO 3166-1 alfa-3 (p. ej. ECU), único.</summary>
    public string? Codigo { get; }

    public string? Nombre { get; }

    /// <summary>Capital principal; null si no tiene.</summary>
    public string? Capital { get; }

    public Region? Region { get; }

    public long Poblacion { get; }

    /// <summary>Nombre de la moneda principal; null si no tiene.</summary>
    public string? Moneda { get; }

    public bool EsNuevo => Id is null;

    /// <summary>País que todavía no existe en la base de datos.</summary>
    public static Pais Nuevo(string? codigo, string? nombre, string? capital, Region? region, long poblacion,
        string? moneda)
    {
        return new Pais(null, codigo, nombre, capital, region, poblacion, moneda);
    }

    public Pais ConId(int id) => this with { Id = id };

    private static string? Limpiar(string? texto)
    {
        string? recortado = texto?.Trim();
        return string.IsNullOrEmpty(recortado) ? null : recortado;
    }
}
