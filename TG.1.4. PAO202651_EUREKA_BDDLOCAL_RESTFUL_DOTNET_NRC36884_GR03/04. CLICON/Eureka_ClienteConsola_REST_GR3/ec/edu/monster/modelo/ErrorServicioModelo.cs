namespace ec.edu.monster.modelo
{
    /// <summary>
    /// Error que devuelve el servidor en el JSON ErrorServicio {estado, codigo, mensaje}, o un error
    /// de comunicación (sin codigo).
    /// </summary>
    public class ErrorServicioModelo : Exception
    {
        private const string SesionInvalida = "AUTH-003";

        public string? Codigo { get; }

        public ErrorServicioModelo(string? codigo, string mensaje) : base(mensaje)
        {
            Codigo = codigo;
        }

        /// <summary>El token ya no es válido: el cliente debe volver al inicio de sesión.</summary>
        public bool RequiereNuevoLogin => Codigo == SesionInvalida;
    }
}
