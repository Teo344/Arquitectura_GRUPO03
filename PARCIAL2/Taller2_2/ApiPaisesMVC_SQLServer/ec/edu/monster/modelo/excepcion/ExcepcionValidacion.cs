namespace ec.edu.monster.modelo.excepcion
{
    /// <summary>
    /// Datos no válidos. Reúne todos los errores para mostrarlos juntos en el formulario.
    /// </summary>
    public class ExcepcionValidacion : ExcepcionAplicacion
    {
        public IReadOnlyList<string> Errores { get; }

        public ExcepcionValidacion(string error) : this(new[] { error })
        {
        }

        public ExcepcionValidacion(IEnumerable<string> errores) : this(errores.ToList())
        {
        }

        private ExcepcionValidacion(List<string> errores) : base(string.Join("\n", errores))
        {
            Errores = errores.AsReadOnly();
        }
    }
}
