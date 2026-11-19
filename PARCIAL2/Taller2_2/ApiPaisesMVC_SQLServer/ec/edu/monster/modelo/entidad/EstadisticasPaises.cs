namespace ec.edu.monster.modelo.entidad
{
    /// <summary>
    /// Reportes de la Actividad 5 (CU-08).
    /// </summary>
    /// <param name="PaisesPorRegion">Reporte 1: cantidad de países por región, de mayor a menor</param>
    /// <param name="TotalPaises">países registrados</param>
    /// <param name="MasPoblados">Reporte 2: los países más poblados, de mayor a menor (vacía si no hay países)</param>
    /// <param name="RegionConMasPaises">null si no hay países; ante empate, la primera del orden</param>
    /// <param name="PromedioPorRegion">Reporte 3: promedio poblacional por región, de mayor a menor</param>
    public sealed record EstadisticasPaises(
        IReadOnlyList<KeyValuePair<Region, long>> PaisesPorRegion,
        long TotalPaises,
        IReadOnlyList<Pais> MasPoblados,
        Region? RegionConMasPaises,
        IReadOnlyList<KeyValuePair<Region, double>> PromedioPorRegion)
    {
        public bool EstaVacio => TotalPaises == 0;

        /// <summary>El país con más habitantes; null si no hay países.</summary>
        public Pais? PaisMasPoblado => MasPoblados.Count > 0 ? MasPoblados[0] : null;

        public static EstadisticasPaises Vacias()
        {
            return new EstadisticasPaises(Array.Empty<KeyValuePair<Region, long>>(), 0, Array.Empty<Pais>(), null,
                Array.Empty<KeyValuePair<Region, double>>());
        }
    }
}
