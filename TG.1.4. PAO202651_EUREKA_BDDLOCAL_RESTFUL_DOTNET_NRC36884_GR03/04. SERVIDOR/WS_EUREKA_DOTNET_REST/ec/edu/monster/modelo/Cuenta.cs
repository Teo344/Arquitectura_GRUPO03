using System;

namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Cuenta con sus datos de consulta (RQF-003). Al bloquearla para una transacción solo se leen
    /// Codigo, CodigoMoneda, Saldo, Estado y Contador; el resto queda null.
    /// </summary>
    public class Cuenta
    {
        public string Codigo { get; set; }
        public string Titular { get; set; }
        public string CodigoMoneda { get; set; }
        public string Moneda { get; set; }
        public decimal Saldo { get; set; }
        public string Estado { get; set; }
        public string Sucursal { get; set; }
        public DateTime? FechaCreacion { get; set; }
        public int Contador { get; set; }
    }
}
