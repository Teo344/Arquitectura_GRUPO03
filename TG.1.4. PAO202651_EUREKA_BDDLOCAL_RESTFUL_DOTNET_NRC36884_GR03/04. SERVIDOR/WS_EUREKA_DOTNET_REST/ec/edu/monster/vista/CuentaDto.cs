namespace ec.edu.monster.vista
{
    /// <summary>Datos de una cuenta (RQF-003). La fecha va en formato ISO (aaaa-mm-dd).</summary>
    public class CuentaDto
    {
        public string Codigo { get; set; }
        public string Titular { get; set; }
        public string Moneda { get; set; }
        public decimal Saldo { get; set; }
        public string Estado { get; set; }
        public string Sucursal { get; set; }
        public string FechaCreacion { get; set; }
    }
}
