using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.recursos
{
    /// <summary>Controlador público (sin token): login y cambio de clave (RQF-001).</summary>
    [ApiController]
    [Route("api/autenticacion")]
    public class AutenticacionControlador : ControllerBase
    {
        private readonly ServicioAutenticacion servicioAutenticacion;

        public AutenticacionControlador(ServicioAutenticacion servicioAutenticacion)
        {
            this.servicioAutenticacion = servicioAutenticacion;
        }

        [HttpPost("login")]
        public RespuestaDto Login([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] CredencialesDto credenciales)
        {
            CredencialesDto c = credenciales ?? new CredencialesDto();
            return RespuestaMapeador.DeToken(servicioAutenticacion.Login(c.Usuario, c.Clave));
        }

        [HttpPost("cambiar-clave")]
        public RespuestaDto CambiarClave([FromBody(EmptyBodyBehavior = EmptyBodyBehavior.Allow)] CambioClaveDto cambio)
        {
            CambioClaveDto c = cambio ?? new CambioClaveDto();
            servicioAutenticacion.CambiarClave(c.Usuario, c.ClaveActual, c.ClaveNueva);
            return RespuestaMapeador.DeCambioClave();
        }
    }
}
