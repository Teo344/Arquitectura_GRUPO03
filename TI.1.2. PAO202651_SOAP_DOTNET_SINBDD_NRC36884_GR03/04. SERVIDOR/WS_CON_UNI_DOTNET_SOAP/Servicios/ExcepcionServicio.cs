using System.Runtime.Serialization;
using System.ServiceModel;
using WS_CON_UNI_DOTNET_SOAP.Modelo;
using WS_CON_UNI_DOTNET_SOAP.Vista;

namespace WS_CON_UNI_DOTNET_SOAP.Servicios
{
    /// <summary>Detalle estructurado del SOAPFault (RQF-010): mismo formato venga de un servicio o del inspector.</summary>
    [DataContract(Name = "ErrorServicio", Namespace = Contrato.Namespace)]
    public class ErrorServicio
    {
        [DataMember(Name = "codigo", Order = 1)] public string Codigo { get; set; }
        [DataMember(Name = "mensaje", Order = 2)] public string Mensaje { get; set; }
    }

    /// <summary>
    /// Excepción de negocio. Al heredar de FaultException, WCF la envía como SOAPFault
    /// con <see cref="ErrorServicio"/> en el detalle, igual que @WebFault en Java.
    /// </summary>
    public class ExcepcionServicio : FaultException<ErrorServicio>
    {
        public CodigoError CodigoError { get; }

        public ExcepcionServicio(CodigoError codigoError)
            : base(new ErrorServicio { Codigo = codigoError.Codigo, Mensaje = codigoError.Mensaje },
                   new FaultReason(codigoError.Mensaje),
                   new FaultCode("Server"))
        {
            CodigoError = codigoError;
        }
    }
}
