using System;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador
{
    /// <summary>Coordina la vista de acceso y el servicio remoto de autenticación (RQF-001).</summary>
    public sealed class LoginControlador : ControladorBase
    {
        private readonly LoginVista vista;
        private readonly AutenticacionRestService autenticacion;

        public LoginControlador(LoginVista vista, AutenticacionRestService autenticacion)
        {
            this.vista = vista;
            this.autenticacion = autenticacion;
        }

        public async Task IniciarSesionAsync(string? usuario, string? clave)
        {
            string usuarioLimpio = usuario?.Trim() ?? "";
            if (usuarioLimpio.Length == 0 || string.IsNullOrEmpty(clave))
            {
                vista.MostrarMensaje("Ingresa tu usuario y contraseña.", true);
                return;
            }

            vista.SetProcesando(true);
            vista.MostrarMensaje("Conectando con el servidor...", false);
            try
            {
                SesionModelo sesion = await autenticacion.IniciarSesionAsync(usuarioLimpio, clave);
                if (!Activo)
                {
                    return;
                }
                vista.SetProcesando(false);
                vista.AbrirMenu(sesion);
            }
            catch (Exception ex)
            {
                if (!Activo)
                {
                    return;
                }
                vista.SetProcesando(false);
                vista.MostrarMensaje(MensajeDe(ex), true);
                vista.LimpiarClave();
            }
        }
    }
}
