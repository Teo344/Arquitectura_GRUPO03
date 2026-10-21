using System.ServiceModel;
using WS_CON_UNI_DOTNET_SOAP.Servicios;
using WS_CON_UNI_DOTNET_SOAP.Vista;

namespace WS_CON_UNI_DOTNET_SOAP.Controladores
{
    /// <summary>Contrato del servicio público (sin token): login y cambio de clave (RQF-001).</summary>
    [ServiceContract(Name = "AutenticacionWS", Namespace = Contrato.Namespace)]
    public interface IAutenticacionWS
    {
        [OperationContract(Name = "login")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto Login(string usuario, string clave);

        [OperationContract(Name = "cambiarClave")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto CambiarClave(string usuario, string claveActual, string claveNueva);
    }
}
