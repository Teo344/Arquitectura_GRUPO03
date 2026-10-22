using System.Runtime.Serialization;

namespace ec.edu.monster.servicio
{
    [DataContract(Name = "ErrorServicio", Namespace = ContratoWS.Namespace)]
    internal sealed class ErrorServicio
    {
        [DataMember(Name = "codigo", Order = 1)] public string Codigo { get; set; }
        [DataMember(Name = "mensaje", Order = 2)] public string Mensaje { get; set; }
    }
}
