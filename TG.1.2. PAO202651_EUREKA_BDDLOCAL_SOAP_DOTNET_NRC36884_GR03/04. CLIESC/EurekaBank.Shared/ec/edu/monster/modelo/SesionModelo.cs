namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Sesión activa del empleado (RQF-001). El token solo lo usa la capa de servicio para la
    /// cabecera SOAP; la vista nunca lo muestra. ExpiraEn es la fecha ISO-8601 (UTC) que
    /// devuelve el servidor.
    /// </summary>
    public sealed class SesionModelo
    {
        public string Token { get; }
        public string? ExpiraEn { get; }
        public string? CodigoEmpleado { get; }
        public string? NombreEmpleado { get; }
        public string? CodigoSucursal { get; }
        public string? Sucursal { get; }

        public SesionModelo(string token, string? expiraEn, string? codigoEmpleado, string? nombreEmpleado,
            string? codigoSucursal, string? sucursal)
        {
            Token = token;
            ExpiraEn = expiraEn;
            CodigoEmpleado = codigoEmpleado;
            NombreEmpleado = nombreEmpleado;
            CodigoSucursal = codigoSucursal;
            Sucursal = sucursal;
        }
    }
}
