namespace ec.edu.monster.modelo.excepcion
{
    /// <summary>Falla de SQL Server: conexión, consulta o transacción.</summary>
    public class ExcepcionPersistencia : ExcepcionAplicacion
    {
        public ExcepcionPersistencia(string mensaje, Exception? causa) : base(mensaje, causa)
        {
        }
    }
}
