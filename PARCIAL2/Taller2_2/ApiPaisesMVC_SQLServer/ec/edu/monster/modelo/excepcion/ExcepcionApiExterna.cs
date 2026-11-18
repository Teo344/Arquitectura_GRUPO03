namespace ec.edu.monster.modelo.excepcion
{
    /// <summary>
    /// La API pública no respondió, rechazó la solicitud o devolvió algo que no se pudo interpretar.
    /// El servicio la usa para decidir el respaldo con la base de datos local.
    /// </summary>
    public class ExcepcionApiExterna : ExcepcionAplicacion
    {
        public ExcepcionApiExterna(string mensaje) : base(mensaje)
        {
        }

        public ExcepcionApiExterna(string mensaje, Exception? causa) : base(mensaje, causa)
        {
        }
    }
}
