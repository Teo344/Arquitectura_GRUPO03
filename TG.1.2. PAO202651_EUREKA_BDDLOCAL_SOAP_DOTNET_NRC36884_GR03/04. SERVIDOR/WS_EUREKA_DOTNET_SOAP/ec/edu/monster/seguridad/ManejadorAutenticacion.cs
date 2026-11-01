using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Dispatcher;

namespace ec.edu.monster.seguridad
{
    /// <summary>
    /// Middleware de autenticación (RN-06), equivalente al SOAPHandler de Java. Lee el elemento
    /// "token" de la cabecera SOAP (sin importar su namespace), lo valida con
    /// <see cref="GestorToken"/> y deja la <see cref="Sesion"/> en las propiedades del mensaje para
    /// que el servicio registre los movimientos con el empleado autenticado. Si falla, la
    /// ExcepcionServicio sale como SOAPFault con el mismo detalle ErrorServicio que generan los
    /// servicios.
    /// </summary>
    public class ManejadorAutenticacion : IDispatchMessageInspector
    {
        /// <summary>El servicio la lee con OperationContext.Current.IncomingMessageProperties.</summary>
        public const string PropiedadSesion = "sesion";
        private const string ElementoToken = "token";

        public object AfterReceiveRequest(ref Message request, IClientChannel channel, InstanceContext instanceContext)
        {
            Sesion sesion = GestorToken.Instancia.Validar(LeerToken(request.Headers));
            request.Properties[PropiedadSesion] = sesion;
            return null;
        }

        public void BeforeSendReply(ref Message reply, object correlationState)
        {
        }

        /// <summary>
        /// Devuelve el texto del primer encabezado "token" o null si no hay. Lo usa también
        /// cerrarSesion, que no pasa por este inspector.
        /// </summary>
        public static string LeerToken(MessageHeaders cabeceras)
        {
            for (int i = 0; i < cabeceras.Count; i++)
            {
                if (cabeceras[i].Name == ElementoToken)
                {
                    using (var lector = cabeceras.GetReaderAtHeader(i))
                    {
                        return lector.ReadElementContentAsString();
                    }
                }
            }
            return null;
        }
    }
}
