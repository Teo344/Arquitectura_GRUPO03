using System.ServiceModel;

namespace ec.edu.monster.servicio
{
    [ServiceContract(Name = "AutenticacionWS", Namespace = ContratoWS.Namespace)]
    internal interface IAutenticacionWS
    {
        [OperationContract(Name = "login")]
        [FaultContract(typeof(ErrorServicio))]
        [return: MessageParameter(Name = "return")]
        RespuestaDto Login(string usuario, string clave);
    }
}
