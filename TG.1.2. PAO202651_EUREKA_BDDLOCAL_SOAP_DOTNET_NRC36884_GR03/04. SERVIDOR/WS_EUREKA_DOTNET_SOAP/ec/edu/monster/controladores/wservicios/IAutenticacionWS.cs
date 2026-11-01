using System.ServiceModel;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.wservicios
{
    /// <summary>
    /// Contrato del servicio público: inicio (RQF-001) y cierre (RQF-002) de sesión. cerrarSesion
    /// recibe el token en la cabecera SOAP, igual que las operaciones protegidas.
    /// </summary>
    [ServiceContract(Name = "AutenticacionWS", Namespace = Contrato.Namespace)]
    public interface IAutenticacionWS
    {
        /// <summary>equipo es opcional: el nombre del equipo del cliente para logsession.</summary>
        [OperationContract(Name = "iniciarSesion")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto IniciarSesion(string usuario, string clave, string equipo);

        [OperationContract(Name = "cerrarSesion")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto CerrarSesion();
    }
}
