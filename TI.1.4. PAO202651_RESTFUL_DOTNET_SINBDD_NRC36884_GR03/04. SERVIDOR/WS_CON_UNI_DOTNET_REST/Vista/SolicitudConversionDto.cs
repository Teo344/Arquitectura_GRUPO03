namespace WS_CON_UNI_DOTNET_REST.Vista
{
    /// <summary>Solicitud de conversión. El token no va aquí: viaja en la cabecera Authorization.</summary>
    public class SolicitudConversionDto
    {
        public double? Valor { get; set; }
        public string UnidadOrigen { get; set; }
        public string UnidadDestino { get; set; }
    }
}
