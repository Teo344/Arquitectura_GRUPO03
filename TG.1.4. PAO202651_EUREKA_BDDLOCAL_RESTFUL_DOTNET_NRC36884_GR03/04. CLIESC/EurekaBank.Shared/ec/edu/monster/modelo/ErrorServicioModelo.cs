using System;

namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Error que devuelve el servidor en el cuerpo JSON de error (estado ERROR, codigo y
    /// mensaje), o un error de comunicación (sin codigo).
    /// </summary>
    public sealed class ErrorServicioModelo : Exception
    {
        private const string SesionInvalida = "AUTH-003";

        public string? Codigo { get; }

        public ErrorServicioModelo(string? codigo, string mensaje) : base(mensaje)
        {
            Codigo = codigo;
        }

        public ErrorServicioModelo(string mensaje, Exception causa) : base(mensaje, causa)
        {
        }

        /// <summary>El token ya no es válido: el cliente debe volver al inicio de sesión.</summary>
        public bool RequiereNuevoLogin
        {
            get { return Codigo == SesionInvalida; }
        }
    }
}
