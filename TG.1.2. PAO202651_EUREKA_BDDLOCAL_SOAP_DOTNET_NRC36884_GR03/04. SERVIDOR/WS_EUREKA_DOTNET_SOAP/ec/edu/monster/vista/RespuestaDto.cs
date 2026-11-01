using System.Collections.Generic;
using System.Runtime.Serialization;

namespace ec.edu.monster.vista
{
    /// <summary>
    /// Respuesta exitosa de todas las operaciones (sección 8.2.7 del ERS). Los campos que no
    /// aplican a la operación quedan nulos y no se serializan (EmitDefaultValue = false); los
    /// errores viajan como SOAPFault con ErrorServicio.
    /// </summary>
    [DataContract(Name = "respuesta", Namespace = Contrato.Namespace)]
    public class RespuestaDto
    {
        [DataMember(Name = "estado", Order = 1, EmitDefaultValue = false)] public string Estado { get; set; }
        [DataMember(Name = "codigo", Order = 2, EmitDefaultValue = false)] public string Codigo { get; set; }
        [DataMember(Name = "mensaje", Order = 3, EmitDefaultValue = false)] public string Mensaje { get; set; }
        [DataMember(Name = "token", Order = 4, EmitDefaultValue = false)] public string Token { get; set; }
        [DataMember(Name = "expiraEn", Order = 5, EmitDefaultValue = false)] public string ExpiraEn { get; set; }
        [DataMember(Name = "codigoEmpleado", Order = 6, EmitDefaultValue = false)] public string CodigoEmpleado { get; set; }
        [DataMember(Name = "nombreEmpleado", Order = 7, EmitDefaultValue = false)] public string NombreEmpleado { get; set; }
        [DataMember(Name = "codigoSucursal", Order = 8, EmitDefaultValue = false)] public string CodigoSucursal { get; set; }
        [DataMember(Name = "sucursal", Order = 9, EmitDefaultValue = false)] public string Sucursal { get; set; }
        [DataMember(Name = "cuenta", Order = 10, EmitDefaultValue = false)] public CuentaDto Cuenta { get; set; }
        [DataMember(Name = "movimientos", Order = 11, EmitDefaultValue = false)] public List<MovimientoDto> Movimientos { get; set; }
        [DataMember(Name = "nuevoSaldo", Order = 12, EmitDefaultValue = false)] public decimal? NuevoSaldo { get; set; }
        [DataMember(Name = "numeroMovimiento", Order = 13, EmitDefaultValue = false)] public int? NumeroMovimiento { get; set; }
        [DataMember(Name = "numeroMovimientoDestino", Order = 14, EmitDefaultValue = false)] public int? NumeroMovimientoDestino { get; set; }
    }
}
