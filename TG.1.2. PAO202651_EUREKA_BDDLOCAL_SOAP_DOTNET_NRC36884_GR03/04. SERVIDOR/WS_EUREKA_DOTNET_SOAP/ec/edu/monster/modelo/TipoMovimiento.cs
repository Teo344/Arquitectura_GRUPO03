namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Tipos de movimiento que genera el sistema (tabla tipomovimiento). Los demás tipos solo
    /// aparecen en el historial.
    /// </summary>
    public sealed class TipoMovimiento
    {
        public static readonly TipoMovimiento Deposito = new TipoMovimiento("003");
        public static readonly TipoMovimiento Retiro = new TipoMovimiento("004");
        public static readonly TipoMovimiento TransferenciaIngreso = new TipoMovimiento("008");
        public static readonly TipoMovimiento TransferenciaSalida = new TipoMovimiento("009");

        public string Codigo { get; }

        private TipoMovimiento(string codigo)
        {
            Codigo = codigo;
        }
    }
}
