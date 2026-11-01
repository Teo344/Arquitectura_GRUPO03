using System.ServiceModel;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.wservicios
{
    /// <summary>
    /// Contrato del servicio protegido: consulta de cuenta (RQF-003) y de movimientos (RQF-004).
    /// El token viaja en la cabecera SOAP y lo valida ManejadorAutenticacion.
    /// </summary>
    [ServiceContract(Name = "CuentaWS", Namespace = Contrato.Namespace)]
    public interface ICuentaWS
    {
        [OperationContract(Name = "consultarCuenta")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto ConsultarCuenta(string cuenta);

        [OperationContract(Name = "consultarMovimientos")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto ConsultarMovimientos(string cuenta);
    }
}
