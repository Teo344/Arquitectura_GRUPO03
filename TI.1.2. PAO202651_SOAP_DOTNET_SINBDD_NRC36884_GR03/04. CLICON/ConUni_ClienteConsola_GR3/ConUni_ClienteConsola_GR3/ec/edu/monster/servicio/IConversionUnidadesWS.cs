using System.ServiceModel;

namespace ec.edu.monster.servicio
{
    [ServiceContract(Name = "ConversionUnidadesWS", Namespace = ContratoWS.Namespace)]
    internal interface IConversionUnidadesWS
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
