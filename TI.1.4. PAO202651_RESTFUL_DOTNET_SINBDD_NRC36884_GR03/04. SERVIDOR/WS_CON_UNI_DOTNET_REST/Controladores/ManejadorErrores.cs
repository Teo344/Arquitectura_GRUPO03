using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.AspNetCore.WebUtilities;
using WS_CON_UNI_DOTNET_REST.Servicios;

namespace WS_CON_UNI_DOTNET_REST.Controladores
{
    /// <summary>
    /// Convierte toda falla en una respuesta JSON con ErrorServicio (RQF-010), como el
    /// MapeadorExcepciones de Java: errores de token y de credenciales salen con 401,
    /// errores de datos con 400.
    /// </summary>
    public class ManejadorErrores : IExceptionFilter
    {
        public void OnException(ExceptionContext contexto)
        {
            contexto.Result = contexto.Exception is ExcepcionServicio negocio
                ? Respuesta(negocio)
                : Construir(StatusCodes.Status500InternalServerError,
                    new ErrorServicio("HTTP-500", "Error interno del servidor"));
            contexto.ExceptionHandled = true;
        }

        /// <summary>Respuesta de una ExcepcionServicio; la usan también ManejadorAutenticacion y el JSON inválido.</summary>
        public static IActionResult Respuesta(ExcepcionServicio error)
        {
            bool deAutenticacion = error.CodigoError.Codigo.StartsWith("AUTH");
            int estado = deAutenticacion ? StatusCodes.Status401Unauthorized : StatusCodes.Status400BadRequest;
            return Construir(estado, error.ErrorServicio);
        }

        /// <summary>Ruta inexistente, método no permitido, etc.: también estructurado (HTTP-404, HTTP-405...).</summary>
        public static Task EscribirEstadoHttp(HttpContext http)
        {
            int estado = http.Response.StatusCode;
            return http.Response.WriteAsJsonAsync(
                new ErrorServicio("HTTP-" + estado, ReasonPhrases.GetReasonPhrase(estado)));
        }

        private static IActionResult Construir(int estado, ErrorServicio cuerpo)
        {
            return new ObjectResult(cuerpo) { StatusCode = estado };
        }
    }
}
