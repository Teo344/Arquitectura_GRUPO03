namespace ec.edu.monster.modelo
{
    /// <summary>Datos de una cuenta devueltos por CuentaWS.consultarCuenta (RQF-003).</summary>
    public class CuentaModelo
    {
        public string Codigo { get; }
        public string Titular { get; }
        public string Moneda { get; }
        public decimal Saldo { get; }
        public string Estado { get; }
        public string Sucursal { get; }
        public string FechaCreacion { get; }

        public CuentaModelo(string codigo, string titular, string moneda, decimal saldo, string estado,
            string sucursal, string fechaCreacion)
        {
            Codigo = codigo;
            Titular = titular;
            Moneda = moneda;
            Saldo = saldo;
            Estado = estado;
            Sucursal = sucursal;
            FechaCreacion = fechaCreacion;
        }
    }
}
