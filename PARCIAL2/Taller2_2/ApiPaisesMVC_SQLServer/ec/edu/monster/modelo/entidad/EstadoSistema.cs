namespace ec.edu.monster.modelo.entidad
{
    /// <summary>
    /// Estado de las dependencias externas al abrir la aplicación, para que el usuario sepa antes de
    /// trabajar si la base de datos responde y qué datos entregará la API.
    /// </summary>
    /// <param name="BaseDisponible">SQL Server respondió</param>
    /// <param name="TotalPaises">países guardados (0 si la base no respondió)</param>
    /// <param name="DetalleBase">motivo de la falla; null si la base respondió</param>
    /// <param name="ApiDemostracion">la API está configurada con la clave demo (solo datos de muestra)</param>
    public sealed record EstadoSistema(bool BaseDisponible, long TotalPaises, string? DetalleBase,
        bool ApiDemostracion);
}
