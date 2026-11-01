using System.Runtime.Serialization;

namespace ec.edu.monster.vista
{
    /// <summary>Datos de una cuenta (RQF-003). La fecha va en formato ISO (aaaa-mm-dd).</summary>
    [DataContract(Name = "cuenta", Namespace = Contrato.Namespace)]
    public class CuentaDto
    {
        [DataMember(Name = "codigo", Order = 1)] public string Codigo { get; set; }
        [DataMember(Name = "titular", Order = 2)] public string Titular { get; set; }
        [DataMember(Name = "moneda", Order = 3)] public string Moneda { get; set; }
        [DataMember(Name = "saldo", Order = 4)] public decimal Saldo { get; set; }
        [DataMember(Name = "estado", Order = 5)] public string Estado { get; set; }
        [DataMember(Name = "sucursal", Order = 6)] public string Sucursal { get; set; }
        [DataMember(Name = "fechaCreacion", Order = 7)] public string FechaCreacion { get; set; }
    }
}
