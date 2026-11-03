namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Resultado de un depósito, retiro o transferencia. En una transferencia, NuevoSaldo es el
    /// de la cuenta de origen y NumeroMovimientoDestino el del movimiento en la cuenta de destino.
    /// </summary>
    public sealed class ResultadoTransaccionModelo
    {
        public string? Mensaje { get; }
        public decimal? NuevoSaldo { get; }
        public int? NumeroMovimiento { get; }
        public int? NumeroMovimientoDestino { get; }

        public ResultadoTransaccionModelo(string? mensaje, decimal? nuevoSaldo, int? numeroMovimiento,
            int? numeroMovimientoDestino)
        {
            Mensaje = mensaje;
            NuevoSaldo = nuevoSaldo;
            NumeroMovimiento = numeroMovimiento;
            NumeroMovimientoDestino = numeroMovimientoDestino;
        }
    }
}
