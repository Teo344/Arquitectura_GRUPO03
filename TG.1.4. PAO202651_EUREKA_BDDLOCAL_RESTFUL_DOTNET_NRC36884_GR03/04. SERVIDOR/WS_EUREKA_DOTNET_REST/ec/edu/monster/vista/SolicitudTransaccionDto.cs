namespace ec.edu.monster.vista
{
    /// <summary>Cuerpo de los depósitos y retiros. El token no va aquí: viaja en la cabecera Authorization.</summary>
    public class SolicitudTransaccionDto
    {
        public string Cuenta { get; set; }
        public decimal? Importe { get; set; }
    }
}
