using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ModelBinding;
using ec.edu.monster.seguridad;
using ec.edu.monster.servicios;
using ec.edu.monster.vista;

namespace ec.edu.monster.controladores.recursos
{
    /// <summary>
    /// Controlador público (sin filtro): inicio (RQF-001) y cierre (RQF-002) de sesión.
    /// cerrar-sesion recibe el token en la cabecera Authorization igual que los controladores
    /// protegidos, pero lo valida el servicio.
    /// </summary>
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
            string ip = HttpContext.Connection.RemoteIpAddress?.ToString();
            return RespuestaMapeador.DeSesion(servicioAutenticacion.IniciarSesion(c.Usuario, c.Clave, ip, c.Equipo));
        }

        [HttpPost("cerrar-sesion")]
        public RespuestaDto CerrarSesion([FromHeader(Name = "Authorization")] string autorizacion)
        {
            servicioAutenticacion.CerrarSesion(ManejadorAutenticacion.LeerToken(autorizacion));
            return RespuestaMapeador.DeCierreSesion();
        }
    }
}
