using System.Runtime.Serialization;

namespace WS_CON_UNI_DOTNET_SOAP.Vista
{
    /// <summary>Elemento del catálogo de categorías.</summary>
    [DataContract(Name = "categoria", Namespace = Contrato.Namespace)]
    public class CategoriaDto
    {
        [DataMember(Name = "codigo", Order = 1)] public string Codigo { get; set; }
        [DataMember(Name = "nombre", Order = 2)] public string Nombre { get; set; }
        [DataMember(Name = "convertible", Order = 3)] public bool Convertible { get; set; }
    }
}
