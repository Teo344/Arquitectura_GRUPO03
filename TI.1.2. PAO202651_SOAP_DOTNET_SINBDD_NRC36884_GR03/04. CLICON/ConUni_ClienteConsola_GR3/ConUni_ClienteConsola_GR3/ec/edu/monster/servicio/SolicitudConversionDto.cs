using System.Runtime.Serialization;

namespace ec.edu.monster.servicio
{
    [DataContract(Name = "solicitudConversion", Namespace = ContratoWS.Namespace)]
    internal sealed class SolicitudConversionDto
    {
        [DataMember(Name = "valor", Order = 1)] public double? Valor { get; set; }
        [DataMember(Name = "unidadOrigen", Order = 2)] public string UnidadOrigen { get; set; }
        [DataMember(Name = "unidadDestino", Order = 3)] public string UnidadDestino { get; set; }
    }
}
