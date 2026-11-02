namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Empleado que inicia sesión, con su usuario y su sucursal vigente (RQF-001).
    /// CodigoSucursal es null si no tiene una asignación vigente.
    /// </summary>
    public class Empleado
    {
        public string Codigo { get; set; }
        public string Usuario { get; set; }
        public string Nombre { get; set; }
        public string Estado { get; set; }
        public string CodigoSucursal { get; set; }
        public string Sucursal { get; set; }
    }
}
