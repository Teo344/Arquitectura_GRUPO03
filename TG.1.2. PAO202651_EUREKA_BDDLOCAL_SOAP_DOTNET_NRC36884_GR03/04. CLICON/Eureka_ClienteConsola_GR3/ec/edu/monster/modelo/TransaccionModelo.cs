namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Datos de una transacción que ingresa el empleado. CuentaDestino solo se usa en las
    /// transferencias; en ellas Cuenta es la cuenta de origen.
    /// </summary>
    public class TransaccionModelo
    {
        public TipoTransaccion Tipo { get; }
        public string Cuenta { get; }
        public string CuentaDestino { get; }
        public decimal Importe { get; }

        public TransaccionModelo(TipoTransaccion tipo, string cuenta, decimal importe)
            : this(tipo, cuenta, null, importe)
        {
        }

        public TransaccionModelo(TipoTransaccion tipo, string cuenta, string cuentaDestino, decimal importe)
        {
            Tipo = tipo;
            Cuenta = cuenta;
            CuentaDestino = cuentaDestino;
            Importe = importe;
        }
    }
}
