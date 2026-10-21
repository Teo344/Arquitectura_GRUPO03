using WS_CON_UNI_DOTNET_SOAP.Servicios;
using WS_CON_UNI_DOTNET_SOAP.Vista;

namespace WS_CON_UNI_DOTNET_SOAP.Controladores
{
    /// <summary>Servicio público: delega en ServicioAutenticacion y arma la respuesta con el mapeador.</summary>
    public class AutenticacionWS : IAutenticacionWS
    {
        private readonly ServicioAutenticacion servicioAutenticacion = new ServicioAutenticacion();

        public RespuestaDto Login(string usuario, string clave)
        {
            return RespuestaMapeador.DeToken(servicioAutenticacion.Login(usuario, clave));
        }

        public RespuestaDto CambiarClave(string usuario, string claveActual, string claveNueva)
        {
            servicioAutenticacion.CambiarClave(usuario, claveActual, claveNueva);
            return RespuestaMapeador.DeCambioClave();
        }
    }
}
