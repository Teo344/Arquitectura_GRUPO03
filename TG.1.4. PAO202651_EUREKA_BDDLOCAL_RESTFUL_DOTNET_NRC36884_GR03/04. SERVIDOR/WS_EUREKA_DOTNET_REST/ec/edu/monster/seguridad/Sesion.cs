namespace ec.edu.monster.seguridad
{
    /// <summary>
    /// Datos del empleado autenticado que acompañan a un token. LogId es la fila de logsession
    /// donde se registra la salida (RQF-002).
    /// </summary>
    public class Sesion
    {
        public string CodigoEmpleado { get; set; }
        public string Usuario { get; set; }
        public string Nombre { get; set; }
        public string CodigoSucursal { get; set; }
        public string Sucursal { get; set; }
        public int LogId { get; set; }
    }
}
