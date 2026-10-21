using System.Runtime.Serialization;

namespace WS_CON_UNI_DOTNET_SOAP.Vista
{
    /// <summary>Elemento de la lista de unidades. No expone el factor de conversión.</summary>
    [DataContract(Name = "unidad", Namespace = Contrato.Namespace)]
    public class UnidadDto
    {
        [DataMember(Name = "codigo", Order = 1)] public string Codigo { get; set; }
        [DataMember(Name = "nombre", Order = 2)] public string Nombre { get; set; }
        [DataMember(Name = "simbolo", Order = 3)] public string Simbolo { get; set; }
        [DataMember(Name = "categoria", Order = 4)] public string Categoria { get; set; }
    }
}
