namespace ec.edu.monster.modelo.excepcion
{
    /// <summary>El registro pedido no existe (o lo borró otro usuario).</summary>
    public class ExcepcionNoEncontrado : ExcepcionAplicacion
    {
        public ExcepcionNoEncontrado(string mensaje) : base(mensaje)
        {
        }
    }
}
