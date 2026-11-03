namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Sesión activa del empleado (RQF-001). El token solo lo usa la capa de servicio para la
    /// cabecera Authorization: Bearer; la vista nunca lo muestra.
    /// </summary>
    public class SesionModelo
    {
        public string Token { get; }
        public DateTimeOffset ExpiraEn { get; }
        public string? CodigoEmpleado { get; }
        public string? NombreEmpleado { get; }
        public string? CodigoSucursal { get; }
        public string? Sucursal { get; }

        public SesionModelo(string token, DateTimeOffset expiraEn, string? codigoEmpleado, string? nombreEmpleado,
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
