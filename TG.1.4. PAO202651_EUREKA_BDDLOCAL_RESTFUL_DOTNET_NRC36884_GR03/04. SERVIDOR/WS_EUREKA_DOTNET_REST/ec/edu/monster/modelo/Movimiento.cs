using System;

namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Movimiento del historial de una cuenta (RQF-004). Accion es INGRESO o SALIDA;
    /// CuentaReferencia es la otra cuenta de una transferencia.
    /// </summary>
    public class Movimiento
    {
        public int Numero { get; set; }
        public DateTime Fecha { get; set; }
        public string Tipo { get; set; }
        public string Accion { get; set; }
        public decimal Importe { get; set; }
        public string CuentaReferencia { get; set; }
    }
}
