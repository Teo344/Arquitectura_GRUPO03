using System.Runtime.Serialization;

namespace ec.edu.monster.servicio
{
    [DataContract(Name = "unidad", Namespace = ContratoWS.Namespace)]
    internal sealed class UnidadDto
    {
        [DataMember(Name = "codigo", Order = 1)] public string Codigo { get; set; }
        [DataMember(Name = "nombre", Order = 2)] public string Nombre { get; set; }
        [DataMember(Name = "simbolo", Order = 3)] public string Simbolo { get; set; }
        [DataMember(Name = "categoria", Order = 4)] public string Categoria { get; set; }
    }
}
