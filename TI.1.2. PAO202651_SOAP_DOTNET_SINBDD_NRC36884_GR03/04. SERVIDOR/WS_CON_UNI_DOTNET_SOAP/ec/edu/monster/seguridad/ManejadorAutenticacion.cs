using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Dispatcher;

namespace ec.edu.monster.seguridad
{
    /// <summary>
    /// Middleware de autenticación (RQF-007), equivalente al SOAPHandler de Java. Lee el
    /// elemento "token" de la cabecera SOAP (sin importar su namespace), lo valida con
    /// <see cref="GestorToken"/> y, si falla, la ExcepcionServicio sale como SOAPFault con el
    /// mismo detalle ErrorServicio que generan los servicios.
    /// </summary>
    public class ManejadorAutenticacion : IDispatchMessageInspector
    {
        public const string PropiedadUsuario = "usuario";
        private const string ElementoToken = "token";

        public object AfterReceiveRequest(ref Message request, IClientChannel channel, InstanceContext instanceContext)
        {
            string usuario = GestorToken.Instancia.Validar(LeerToken(request));
            request.Properties[PropiedadUsuario] = usuario;
            return null;
        }

        public void BeforeSendReply(ref Message reply, object correlationState)
        {
        }

        /// <summary>Devuelve el texto del primer encabezado "token" o null si no hay.</summary>
        private static string LeerToken(Message request)
        {
            for (int i = 0; i < request.Headers.Count; i++)
            {
                if (request.Headers[i].Name == ElementoToken)
                {
                    using (var lector = request.Headers.GetReaderAtHeader(i))
                    {
                        return lector.ReadElementContentAsString();
                    }
                }
            }
            return null;
        }
    }
}
