namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Movimiento del historial de una cuenta (RQF-004). Accion es INGRESO o SALIDA;
    /// CuentaReferencia solo viene en las transferencias.
    /// </summary>
    public class MovimientoModelo
    {
        public int Numero { get; }
        public string Fecha { get; }
        public string Tipo { get; }
        public string Accion { get; }
        public decimal Importe { get; }
        public string CuentaReferencia { get; }

        public MovimientoModelo(int numero, string fecha, string tipo, string accion, decimal importe,
            string cuentaReferencia)
        {
            Numero = numero;
            Fecha = fecha;
            Tipo = tipo;
            Accion = accion;
            Importe = importe;
            CuentaReferencia = cuentaReferencia;
        }
    }
}
