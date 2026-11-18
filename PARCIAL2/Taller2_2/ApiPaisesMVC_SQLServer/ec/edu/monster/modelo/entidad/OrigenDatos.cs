namespace ec.edu.monster.modelo.entidad
{
    /// <summary>De dónde salió el resultado de una búsqueda.</summary>
    public enum OrigenDatos
    {
        /// <summary>Respuesta en vivo de REST Countries.</summary>
        API_EXTERNA,

        /// <summary>Respaldo: la API falló y se respondió con lo ya guardado en SQL Server.</summary>
        BASE_LOCAL
    }
}
