using System.Runtime.Serialization;

namespace WS_CON_UNI_DOTNET_SOAP.Vista
{
    /// <summary>Solicitud de conversión. El token no va aquí: viaja en la cabecera SOAP.</summary>
    [DataContract(Name = "solicitudConversion", Namespace = Contrato.Namespace)]
    public class SolicitudConversionDto
    {
        [DataMember(Name = "valor", Order = 1)] public double? Valor { get; set; }
        [DataMember(Name = "unidadOrigen", Order = 2)] public string UnidadOrigen { get; set; }
        [DataMember(Name = "unidadDestino", Order = 3)] public string UnidadDestino { get; set; }
    }
}
