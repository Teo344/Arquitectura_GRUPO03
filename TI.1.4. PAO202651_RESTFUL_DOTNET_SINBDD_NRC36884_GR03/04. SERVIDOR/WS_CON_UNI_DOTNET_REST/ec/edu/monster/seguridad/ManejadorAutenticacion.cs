using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.Net.Http.Headers;
using ec.edu.monster.controladores.recursos;
using ec.edu.monster.servicios;

namespace ec.edu.monster.seguridad
{
    /// <summary>
    /// Middleware de autenticación (RQF-007), equivalente al ContainerRequestFilter de Java. Lee
    /// el token de la cabecera "Authorization: Bearer &lt;token&gt;", lo valida con
    /// <see cref="GestorToken"/> y, si falla, corta la petición con el mismo cuerpo ErrorServicio
    /// que generan los servicios.
    /// </summary>
    public class ManejadorAutenticacion : IAuthorizationFilter
    {
        public const string PropiedadUsuario = "usuario";
        private const string Prefijo = "Bearer ";

        private readonly GestorToken gestorToken;

        public ManejadorAutenticacion(GestorToken gestorToken)
        {
            this.gestorToken = gestorToken;
        }

        public void OnAuthorization(AuthorizationFilterContext contexto)
        {
            try
            {
                string usuario = gestorToken.Validar(LeerToken(contexto));
                contexto.HttpContext.Items[PropiedadUsuario] = usuario;
            }
            catch (ExcepcionServicio e)
            {
                contexto.Result = ManejadorErrores.Respuesta(e);
            }
        }

        /// <summary>Devuelve el token de la cabecera Authorization o null si no hay.</summary>
        private static string LeerToken(AuthorizationFilterContext contexto)
        {
            string cabecera = contexto.HttpContext.Request.Headers[HeaderNames.Authorization];
            return cabecera != null && cabecera.StartsWith(Prefijo) ? cabecera.Substring(Prefijo.Length) : null;
        }
    }
}
