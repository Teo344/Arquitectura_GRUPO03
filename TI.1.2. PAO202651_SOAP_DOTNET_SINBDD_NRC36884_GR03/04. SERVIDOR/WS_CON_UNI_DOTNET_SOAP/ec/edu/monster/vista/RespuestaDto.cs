using System.Collections.Generic;
using System.Runtime.Serialization;

namespace ec.edu.monster.vista
{
    /// <summary>
    /// Respuesta exitosa de todas las operaciones (RQF-009). Los campos que no aplican
    /// a la operación quedan nulos y no se serializan (EmitDefaultValue = false).
    /// </summary>
    [DataContract(Name = "respuesta", Namespace = Contrato.Namespace)]
    public class RespuestaDto
    {
        [DataMember(Name = "estado", Order = 1, EmitDefaultValue = false)] public string Estado { get; set; }
        [DataMember(Name = "codigo", Order = 2, EmitDefaultValue = false)] public string Codigo { get; set; }
        [DataMember(Name = "mensaje", Order = 3, EmitDefaultValue = false)] public string Mensaje { get; set; }
        [DataMember(Name = "resultado", Order = 4, EmitDefaultValue = false)] public double? Resultado { get; set; }
        [DataMember(Name = "unidadOrigen", Order = 5, EmitDefaultValue = false)] public string UnidadOrigen { get; set; }
        [DataMember(Name = "unidadDestino", Order = 6, EmitDefaultValue = false)] public string UnidadDestino { get; set; }
        [DataMember(Name = "token", Order = 7, EmitDefaultValue = false)] public string Token { get; set; }
        [DataMember(Name = "expiraEn", Order = 8, EmitDefaultValue = false)] public string ExpiraEn { get; set; }
        [DataMember(Name = "categorias", Order = 9, EmitDefaultValue = false)] public List<CategoriaDto> Categorias { get; set; }
        [DataMember(Name = "unidades", Order = 10, EmitDefaultValue = false)] public List<UnidadDto> Unidades { get; set; }
    }
}
