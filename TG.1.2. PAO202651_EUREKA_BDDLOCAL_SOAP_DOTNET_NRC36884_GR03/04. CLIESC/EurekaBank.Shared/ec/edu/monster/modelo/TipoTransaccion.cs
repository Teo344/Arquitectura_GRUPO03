namespace ec.edu.monster.modelo
{
    /// <summary>Operaciones monetarias que ofrece TransaccionWS (RQF-005 a RQF-007).</summary>
    public enum TipoTransaccion
    {
        Deposito,
        Retiro,
        Transferencia
    }

    public static class TipoTransaccionExtensiones
    {
        public static string NombreVisible(this TipoTransaccion tipo)
        {
            return tipo switch
            {
                TipoTransaccion.Deposito => "Depósito",
                TipoTransaccion.Retiro => "Retiro",
                _ => "Transferencia"
            };
        }
    }
}
