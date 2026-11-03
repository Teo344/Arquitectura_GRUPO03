using ec.edu.monster.modelo;

namespace ec.edu.monster.vista
{
    /// <summary>
    /// Datos que viajan entre pantallas (los "extras" de los Intent del cliente móvil): la sesión
    /// activa y el mensaje que debe mostrar el login al volver a él.
    /// </summary>
    public sealed class EstadoSesion
    {
        private string? mensaje;
        private bool mensajeEsError;

        public SesionModelo? Sesion { get; private set; }

        public void Iniciar(SesionModelo sesion)
        {
            Sesion = sesion;
            mensaje = null;
        }

        /// <summary>Descarta la sesión local y deja un mensaje para la pantalla de acceso.</summary>
        public void Terminar(string? mensaje, bool esError)
        {
            Sesion = null;
            this.mensaje = mensaje;
            mensajeEsError = esError;
        }

        /// <summary>Entrega el mensaje pendiente una sola vez.</summary>
        public (string? Texto, bool EsError) TomarMensaje()
        {
            (string?, bool) pendiente = (mensaje, mensajeEsError);
            mensaje = null;
            return pendiente;
        }
    }
}
