using ec.edu.monster.modelo;
using ec.edu.monster.seguridad;

namespace ec.edu.monster.servicios
{
    /// <summary>Login y cambio de clave (RQF-001). Es el único servicio que usa <see cref="GestorToken"/>.</summary>
    public class ServicioAutenticacion
    {
        /// <summary>Login y cambio de clave comparten el candado para que un login con la clave vieja no quede vivo.</summary>
        private static readonly object Candado = new object();

        private readonly GestorToken gestorToken;

        public ServicioAutenticacion(GestorToken gestorToken)
        {
            this.gestorToken = gestorToken;
        }

        public TokenEmitido Login(string usuario, string clave)
        {
            lock (Candado)
            {
                ValidarCredenciales(usuario, clave);
                return gestorToken.Generar(usuario.Trim());
            }
        }

        /// <summary>La clave actual autentica al usuario, así que no requiere token. Cierra todas las sesiones.</summary>
        public void CambiarClave(string usuario, string claveActual, string claveNueva)
        {
            lock (Candado)
            {
                ValidarCredenciales(usuario, claveActual);
                if (string.IsNullOrWhiteSpace(claveNueva))
                {
                    throw new ExcepcionServicio(CodigoError.Data001);
                }
                gestorToken.CambiarClave(claveNueva);
            }
        }

        private void ValidarCredenciales(string usuario, string clave)
        {
            if (usuario == null || clave == null || !gestorToken.CredencialesValidas(usuario.Trim(), clave))
            {
                throw new ExcepcionServicio(CodigoError.Auth003);
            }
        }
    }
}
