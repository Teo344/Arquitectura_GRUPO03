namespace ec.edu.monster.modelo.entidad
{
    /// <summary>Campo en el que se busca el texto (Actividad 3).</summary>
    public enum CampoBusqueda
    {
        NOMBRE,
        CAPITAL,
        REGION
    }

    /// <summary>
    /// Criterios de la lista de países: búsqueda por nombre, capital o región (Actividad 3) y filtro
    /// dinámico por región (Actividad 4). Ambos se pueden combinar.
    /// </summary>
    /// <param name="Campo">dónde buscar el texto</param>
    /// <param name="Texto">texto a buscar; null o vacío para no filtrar</param>
    /// <param name="Region">región elegida en los filtros; null para todas</param>
    public sealed record FiltroPaises(CampoBusqueda Campo, string? Texto, Region? Region)
    {
        public static FiltroPaises Ninguno { get; } = new FiltroPaises(CampoBusqueda.NOMBRE, null, null);

        public bool TieneTexto => !string.IsNullOrWhiteSpace(Texto);

        public bool EstaVacio => !TieneTexto && Region == null;
    }

    public static class CampoBusquedaExtensiones
    {
        public static string Nombre(this CampoBusqueda campo) => campo switch
        {
            CampoBusqueda.NOMBRE => "Nombre",
            CampoBusqueda.CAPITAL => "Capital",
            CampoBusqueda.REGION => "Región",
            _ => campo.ToString()
        };
    }
}
