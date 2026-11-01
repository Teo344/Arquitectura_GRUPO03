using System.ServiceModel;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.wservicios
{
    /// <summary>
    /// Contrato del servicio protegido: depósito (RQF-005), retiro (RQF-006) y transferencia
    /// (RQF-007). El token viaja en la cabecera SOAP y lo valida ManejadorAutenticacion.
    /// importe es nullable para que su ausencia se responda con MOV-001.
    /// </summary>
    [ServiceContract(Name = "TransaccionWS", Namespace = Contrato.Namespace)]
    public interface ITransaccionWS
    {
        [OperationContract(Name = "depositar")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto Depositar(string cuenta, decimal? importe);

        [OperationContract(Name = "retirar")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto Retirar(string cuenta, decimal? importe);

        [OperationContract(Name = "transferir")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto Transferir(string cuentaOrigen, string cuentaDestino, decimal? importe);
    }
}
