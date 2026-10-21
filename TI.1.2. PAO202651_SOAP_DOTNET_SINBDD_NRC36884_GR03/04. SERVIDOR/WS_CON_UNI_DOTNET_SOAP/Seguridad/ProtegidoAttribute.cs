using System;
using System.Collections.ObjectModel;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;

namespace WS_CON_UNI_DOTNET_SOAP.Seguridad
{
    /// <summary>
    /// Marca un servicio como protegido: registra <see cref="ManejadorAutenticacion"/> en todos
    /// sus endpoints. Es el equivalente WCF de @HandlerChain en el servidor Java.
    /// </summary>
    [AttributeUsage(AttributeTargets.Class)]
    public class ProtegidoAttribute : Attribute, IServiceBehavior
    {
        public void ApplyDispatchBehavior(ServiceDescription descripcion, ServiceHostBase host)
        {
            foreach (ChannelDispatcher canal in host.ChannelDispatchers)
            {
                foreach (EndpointDispatcher endpoint in canal.Endpoints)
                {
                    // El endpoint de metadatos (?wsdl) no lleva token
                    if (endpoint.ContractName != "IMetadataExchange")
                    {
                        endpoint.DispatchRuntime.MessageInspectors.Add(new ManejadorAutenticacion());
                    }
                }
            }
        }

        public void AddBindingParameters(ServiceDescription descripcion, ServiceHostBase host,
            Collection<ServiceEndpoint> endpoints, BindingParameterCollection parametros)
        {
        }

        public void Validate(ServiceDescription descripcion, ServiceHostBase host)
        {
        }
    }
}
