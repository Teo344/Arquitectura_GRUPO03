using System;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;

namespace ec.edu.monster.servicio
{
    internal sealed class ManejadorTokenSOAP : IClientMessageInspector, IEndpointBehavior
    {
        // La fábrica se reutiliza entre sesiones, por eso el token se consulta en cada envío.
        private readonly Func<string> proveedorToken;

        internal ManejadorTokenSOAP(Func<string> proveedorToken)
        {
            this.proveedorToken = proveedorToken;
        }

        public object BeforeSendRequest(ref Message request, IClientChannel channel)
        {
            request.Headers.Add(MessageHeader.CreateHeader("token", ContratoWS.Namespace, proveedorToken()));
            return null;
        }

        public void AfterReceiveReply(ref Message reply, object correlationState) { }
        public void ApplyClientBehavior(ServiceEndpoint endpoint, ClientRuntime clientRuntime)
        {
            clientRuntime.ClientMessageInspectors.Add(this);
        }
        public void AddBindingParameters(ServiceEndpoint endpoint, BindingParameterCollection bindingParameters) { }
        public void ApplyDispatchBehavior(ServiceEndpoint endpoint, EndpointDispatcher endpointDispatcher) { }
        public void Validate(ServiceEndpoint endpoint) { }
    }
}
