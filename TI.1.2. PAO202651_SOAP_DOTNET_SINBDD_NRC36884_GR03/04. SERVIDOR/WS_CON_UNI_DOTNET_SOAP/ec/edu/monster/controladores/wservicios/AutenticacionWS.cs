using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.wservicios
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
