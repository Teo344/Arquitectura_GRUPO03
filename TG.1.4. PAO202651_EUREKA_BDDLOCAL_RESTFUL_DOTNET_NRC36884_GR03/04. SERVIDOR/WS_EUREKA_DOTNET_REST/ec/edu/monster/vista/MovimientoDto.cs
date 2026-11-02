namespace ec.edu.monster.vista
{
    /// <summary>Elemento del historial de movimientos (RQF-004). cuentaReferencia solo aparece en las transferencias.</summary>
    public class MovimientoDto
    {
        public int Numero { get; set; }
        public string Fecha { get; set; }
        public string Tipo { get; set; }
        public string Accion { get; set; }
        public decimal Importe { get; set; }
        public string CuentaReferencia { get; set; }
    }
}
