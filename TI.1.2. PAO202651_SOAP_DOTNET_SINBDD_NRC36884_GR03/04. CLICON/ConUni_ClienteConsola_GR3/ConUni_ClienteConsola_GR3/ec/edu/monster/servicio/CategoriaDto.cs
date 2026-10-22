using System.Runtime.Serialization;

namespace ec.edu.monster.servicio
{
    [DataContract(Name = "categoria", Namespace = ContratoWS.Namespace)]
    internal sealed class CategoriaDto
    {
        [DataMember(Name = "codigo", Order = 1)] public string Codigo { get; set; }
        [DataMember(Name = "nombre", Order = 2)] public string Nombre { get; set; }
        [DataMember(Name = "convertible", Order = 3)] public bool Convertible { get; set; }
    }
}
