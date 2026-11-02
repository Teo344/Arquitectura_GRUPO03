using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.Net.Http.Headers;
using ec.edu.monster.controladores.recursos;
using ec.edu.monster.servicios;

namespace ec.edu.monster.seguridad
{
    /// <summary>
    /// Middleware de autenticación (RN-06), equivalente al ContainerRequestFilter de Java. Lee el
    /// token de la cabecera "Authorization: Bearer &lt;token&gt;", lo valida con
    /// <see cref="GestorToken"/> y deja la <see cref="Sesion"/> en HttpContext.Items para que el
    /// controlador registre los movimientos con el empleado autenticado. Si falla, corta la
    /// petición con el mismo cuerpo ErrorServicio que generan los servicios.
    /// </summary>
    public class ManejadorAutenticacion : IAuthorizationFilter
    {
        public const string PropiedadSesion = "sesion";
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
                Sesion sesion = gestorToken.Validar(LeerToken(contexto.HttpContext.Request.Headers[HeaderNames.Authorization]));
                contexto.HttpContext.Items[PropiedadSesion] = sesion;
            }
            catch (ExcepcionServicio e)
            {
                contexto.Result = ManejadorErrores.Respuesta(e);
            }
        }

        /// <summary>
        /// Devuelve el token de una cabecera Authorization o null si no hay. Lo usa también
        /// cerrar-sesion, que no pasa por este filtro.
        /// </summary>
        public static string LeerToken(string cabecera)
        {
            return cabecera != null && cabecera.StartsWith(Prefijo) ? cabecera.Substring(Prefijo.Length) : null;
        }
    }
}
