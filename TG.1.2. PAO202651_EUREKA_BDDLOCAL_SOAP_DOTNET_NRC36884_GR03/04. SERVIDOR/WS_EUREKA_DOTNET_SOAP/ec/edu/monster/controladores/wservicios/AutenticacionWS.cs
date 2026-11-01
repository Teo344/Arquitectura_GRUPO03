using System.ServiceModel;
using System.ServiceModel.Channels;
using ec.edu.monster.seguridad;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.wservicios
{
    /// <summary>Servicio público (sin inspector): delega en ServicioAutenticacion y arma la respuesta con el mapeador.</summary>
    public class AutenticacionWS : IAutenticacionWS
    {
        private readonly ServicioAutenticacion servicioAutenticacion = new ServicioAutenticacion();

        public RespuestaDto IniciarSesion(string usuario, string clave, string equipo)
        {
            return RespuestaMapeador.DeSesion(servicioAutenticacion.IniciarSesion(usuario, clave, IpCliente(), equipo));
        }

        /// <summary>No pasa por ManejadorAutenticacion: lee el token de la cabecera y lo valida el servicio.</summary>
        public RespuestaDto CerrarSesion()
        {
            servicioAutenticacion.CerrarSesion(ManejadorAutenticacion.LeerToken(OperationContext.Current.IncomingMessageHeaders));
            return RespuestaMapeador.DeCierreSesion();
        }

        private static string IpCliente()
        {
            OperationContext.Current.IncomingMessageProperties.TryGetValue(RemoteEndpointMessageProperty.Name, out object valor);
            return (valor as RemoteEndpointMessageProperty)?.Address;
        }
    }
}
