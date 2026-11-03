using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.util;

namespace ec.edu.monster.vista
{
    /// <summary>Pantalla de inicio de sesión (RQF-001) y ciclo login → menú → cierre de sesión.</summary>
    public class LoginVista
    {
        private readonly MensajesVista mensajes = new MensajesVista();

        public async Task IniciarAsync()
        {
            MensajeBienvenida();

            LoginControlador controlador;
            try
            {
                controlador = new LoginControlador();
            }
            catch (ArgumentException ex)
            {
                mensajes.MostrarError(ex.Message);
                mensajes.MostrarMensaje("Revise servidor.url en appsettings.json (por ejemplo http://localhost:8097/api)");
                return;
            }
            mensajes.MostrarMensaje("  Servidor REST: " + controlador.EndpointActivo);

            while (true)
            {
                mensajes.MostrarTitulo("INICIO DE SESION");
                string usuario = EntradaUtil.LeerTextoNoVacio("  Usuario : ");
                string clave = EntradaUtil.LeerClave("  Clave   : ");

                try
                {
                    SesionModelo sesion = await controlador.AutenticarAsync(new LoginModelo(usuario, clave));
                    MostrarSesion(sesion);

                    bool volverAlLogin = await new MenuVista(controlador).EjecutarMenuAsync();
                    await CerrarSesionAsync(controlador);
                    if (volverAlLogin)
                    {
                        mensajes.MostrarMensaje("Sesion cerrada. Ingrese sus credenciales para continuar.");
                        continue;
                    }
                    mensajes.MostrarMensaje("Sesion finalizada. Gracias por usar EurekaBank.");
                    return;
                }
                catch (ErrorServicioModelo ex)
                {
                    mensajes.MostrarErrorServicio(ex);
                }
                catch (ArgumentException ex)
                {
                    mensajes.MostrarError(ex.Message);
                }
            }
        }

        // RQF-002: si el servidor no responde, la sesión local ya quedó eliminada
        private async Task CerrarSesionAsync(LoginControlador controlador)
        {
            try
            {
                await controlador.CerrarSesionAsync();
            }
            catch (ErrorServicioModelo ex)
            {
                mensajes.MostrarErrorServicio(ex);
            }
        }

        private void MostrarSesion(SesionModelo sesion)
        {
            Console.WriteLine();
            mensajes.MostrarExito("Inicio de sesion exitoso.");
            mensajes.MostrarRecuadro(new[]
            {
                "Bienvenido(a), " + sesion.NombreEmpleado,
                "Empleado : " + sesion.CodigoEmpleado,
                "Sucursal : " + sesion.CodigoSucursal + " - " + sesion.Sucursal,
                "Sesion activa hasta las " + sesion.ExpiraEn.ToLocalTime().ToString("HH:mm")
                    + " (se renueva con cada operacion)"
            });
        }

        public void MensajeBienvenida()
        {
            mensajes.MostrarBanner(
@"+------------------------------------------------------------+
|                                                            |
|   _____   _   _   ____    _____   _  __     _              |
|  | ____| | | | | |  _ \  | ____| | |/ /    / \             |
|  |  _|   | | | | | |_) | |  _|   | ' /    / _ \            |
|  | |___  | |_| | |  _ <  | |___  | . \   / ___ \           |
|  |_____|  \___/  |_| \_\ |_____| |_|\_\ /_/   \_\  BANK    |
|                                                            |
|     Cliente Consola REST .NET  -  Grupo 03 (NRC 36884)     |
+------------------------------------------------------------+");
        }
    }
}
