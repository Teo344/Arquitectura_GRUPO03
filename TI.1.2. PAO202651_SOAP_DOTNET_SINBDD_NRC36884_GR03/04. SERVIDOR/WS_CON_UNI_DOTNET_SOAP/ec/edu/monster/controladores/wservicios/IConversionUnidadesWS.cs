using System.ServiceModel;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.wservicios
{
    /// <summary>
    /// Contrato del servicio protegido: catálogo (RQF-002, RQF-003) y conversiones (RQF-004 a RQF-006).
    /// El token viaja en la cabecera SOAP y lo valida ManejadorAutenticacion.
    /// </summary>
    [ServiceContract(Name = "ConversionUnidadesWS", Namespace = Contrato.Namespace)]
    public interface IConversionUnidadesWS
    {
        [OperationContract(Name = "consultarCatalogo")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto ConsultarCatalogo();

        [OperationContract(Name = "consultarUnidades")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto ConsultarUnidades(string categoria);

        [OperationContract(Name = "convertirLongitud")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto ConvertirLongitud(SolicitudConversionDto solicitud);

        [OperationContract(Name = "convertirMasa")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto ConvertirMasa(SolicitudConversionDto solicitud);

        [OperationContract(Name = "convertirTemperatura")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto ConvertirTemperatura(SolicitudConversionDto solicitud);
    }
}
