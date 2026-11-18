namespace ec.edu.monster.modelo.entidad
{
    /// <summary>
    /// Resultado de buscar países por nombre (CU-02). Indica el origen para que la vista avise al
    /// usuario cuando los datos vienen del respaldo local.
    /// </summary>
    /// <param name="Paises">coincidencias encontradas (puede estar vacía)</param>
    /// <param name="Origen">API o base local</param>
    /// <param name="Aviso">motivo del respaldo; null si respondió la API</param>
    public sealed record ResultadoBusqueda(IReadOnlyList<Pais> Paises, OrigenDatos Origen, string? Aviso)
    {
        public static ResultadoBusqueda DesdeApi(IReadOnlyList<Pais> paises)
        {
            return new ResultadoBusqueda(paises, OrigenDatos.API_EXTERNA, null);
        }

        public static ResultadoBusqueda DesdeBaseLocal(IReadOnlyList<Pais> paises, string aviso)
        {
            return new ResultadoBusqueda(paises, OrigenDatos.BASE_LOCAL, aviso);
        }

        public bool EstaVacio => Paises.Count == 0;
    }
}
