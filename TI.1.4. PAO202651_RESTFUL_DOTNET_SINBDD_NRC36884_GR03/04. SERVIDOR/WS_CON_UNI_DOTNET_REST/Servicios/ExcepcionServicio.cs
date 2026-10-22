using System;
using WS_CON_UNI_DOTNET_REST.Modelo;

namespace WS_CON_UNI_DOTNET_REST.Servicios
{
    /// <summary>
    /// Cuerpo JSON de toda respuesta de error (RQF-010); ManejadorAutenticacion responde el
    /// mismo formato cuando rechaza el token.
    /// </summary>
    public class ErrorServicio
    {
        public string Estado { get; set; } = "ERROR";
        public string Codigo { get; set; }
        public string Mensaje { get; set; }

        public ErrorServicio(string codigo, string mensaje)
        {
            Codigo = codigo;
            Mensaje = mensaje;
        }
    }

    /// <summary>
    /// Excepción de negocio. ManejadorErrores la convierte en una respuesta HTTP cuyo cuerpo
    /// es <see cref="ErrorServicio"/>.
    /// </summary>
    public class ExcepcionServicio : Exception
    {
        public CodigoError CodigoError { get; }

        public ExcepcionServicio(CodigoError codigoError) : base(codigoError.Mensaje)
        {
            CodigoError = codigoError;
        }

        public ErrorServicio ErrorServicio => new ErrorServicio(CodigoError.Codigo, CodigoError.Mensaje);
    }
}
