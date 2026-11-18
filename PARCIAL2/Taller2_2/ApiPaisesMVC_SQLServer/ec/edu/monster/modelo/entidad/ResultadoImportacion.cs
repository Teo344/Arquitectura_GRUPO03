namespace ec.edu.monster.modelo.entidad
{
    /// <summary>Resumen de una importación desde la API (CU-01 y CU-09).</summary>
    /// <param name="Recibidos">registros válidos recibidos de la API</param>
    /// <param name="Nuevos">registros que no existían y se insertaron</param>
    /// <param name="Actualizados">registros que ya existían y se refrescaron</param>
    public sealed record ResultadoImportacion(int Recibidos, int Nuevos, int Actualizados)
    {
        public static ResultadoImportacion Vacio() => new ResultadoImportacion(0, 0, 0);
    }
}
