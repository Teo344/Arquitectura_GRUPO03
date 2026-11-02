using System.Diagnostics;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.AspNetCore.WebUtilities;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;

namespace ec.edu.monster.controladores.recursos
{
    /// <summary>
    /// Convierte toda falla en una respuesta JSON con ErrorServicio (RNF-009), como el
    /// MapeadorExcepciones de Java: errores de sesión y de credenciales salen con 401, fallas de
    /// la base de datos (SYS-001) con 500 y los demás errores de datos o de negocio con 400.
    /// </summary>
    public class ManejadorErrores : IExceptionFilter
    {
        public void OnException(ExceptionContext contexto)
        {
            if (contexto.Exception is ExcepcionServicio negocio)
            {
                contexto.Result = Respuesta(negocio);
            }
            else
            {
                Trace.TraceError("Error no controlado: {0}", contexto.Exception);
                contexto.Result = Respuesta(new ExcepcionServicio(CodigoError.Sys001));
            }
            contexto.ExceptionHandled = true;
        }

        /// <summary>Respuesta de una ExcepcionServicio; la usan también ManejadorAutenticacion y el JSON inválido.</summary>
        public static IActionResult Respuesta(ExcepcionServicio error)
        {
            int estado;
            if (error.CodigoError.Codigo.StartsWith("AUTH"))
            {
                estado = StatusCodes.Status401Unauthorized;
            }
            else if (error.CodigoError == CodigoError.Sys001)
            {
                estado = StatusCodes.Status500InternalServerError;
            }
            else
            {
                estado = StatusCodes.Status400BadRequest;
            }
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
