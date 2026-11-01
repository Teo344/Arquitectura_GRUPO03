using System.Runtime.Serialization;

namespace ec.edu.monster.vista
{
    /// <summary>Elemento del historial de movimientos (RQF-004). cuentaReferencia solo aparece en las transferencias.</summary>
    [DataContract(Name = "movimiento", Namespace = Contrato.Namespace)]
    public class MovimientoDto
    {
        [DataMember(Name = "numero", Order = 1)] public int Numero { get; set; }
        [DataMember(Name = "fecha", Order = 2)] public string Fecha { get; set; }
        [DataMember(Name = "tipo", Order = 3)] public string Tipo { get; set; }
        [DataMember(Name = "accion", Order = 4)] public string Accion { get; set; }
        [DataMember(Name = "importe", Order = 5)] public decimal Importe { get; set; }
        [DataMember(Name = "cuentaReferencia", Order = 6, EmitDefaultValue = false)] public string CuentaReferencia { get; set; }
    }
}
