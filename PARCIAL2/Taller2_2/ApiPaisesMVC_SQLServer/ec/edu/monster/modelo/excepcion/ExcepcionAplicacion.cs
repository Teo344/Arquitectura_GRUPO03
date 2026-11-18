namespace ec.edu.monster.modelo.excepcion
{
    /// <summary>
    /// Raíz de las fallas que la aplicación conoce. El mensaje ya está redactado para mostrarse al
    /// usuario, así el controlador puede atrapar este único tipo y pasar <see cref="Exception.Message"/>
    /// a la vista sin conocer el detalle técnico.
    /// </summary>
    public abstract class ExcepcionAplicacion : Exception
    {
        protected ExcepcionAplicacion(string mensaje) : base(mensaje)
        {
        }

        protected ExcepcionAplicacion(string mensaje, Exception? causa) : base(mensaje, causa)
        {
        }
    }
}
