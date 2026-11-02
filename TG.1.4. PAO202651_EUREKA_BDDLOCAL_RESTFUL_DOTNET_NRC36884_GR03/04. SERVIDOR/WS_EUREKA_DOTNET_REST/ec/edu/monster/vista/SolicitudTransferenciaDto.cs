namespace ec.edu.monster.vista
{
    /// <summary>Cuerpo de las transferencias. El token no va aquí: viaja en la cabecera Authorization.</summary>
    public class SolicitudTransferenciaDto
    {
        public string CuentaOrigen { get; set; }
        public string CuentaDestino { get; set; }
        public decimal? Importe { get; set; }
    }
}
