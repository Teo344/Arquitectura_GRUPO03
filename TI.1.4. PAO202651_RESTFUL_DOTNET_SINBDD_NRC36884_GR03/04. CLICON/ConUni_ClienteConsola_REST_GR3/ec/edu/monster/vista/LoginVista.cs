using System.Globalization;
using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.util;

namespace ec.edu.monster.vista
{
    public class LoginVista
    {
        private readonly MensajesVista mensajes = new MensajesVista();

        public async Task IniciarAsync()
        {
            MensajeBienvenida();
            mensajes.MostrarTitulo("=== SISTEMA DE CONVERSION DE UNIDADES - CLIENTE CONSOLA REST GR3 ===");

            LoginControlador controlador;
            try
            {
                controlador = new LoginControlador();
                mensajes.MostrarMensaje("Servidor REST configurado: " + controlador.EndpointActivo);
            }
            catch (ArgumentException ex)
            {
                mensajes.MostrarError(ex.Message);
                return;
            }

            while (true)
            {
                string usuario = EntradaUtil.LeerTextoNoVacio("Usuario: ");
                string clave = EntradaUtil.LeerTextoNoVacio("Contrasenia: ");
                try
                {
                    SesionModelo sesion = await controlador.AutenticarAsync(new LoginModelo(usuario, clave));
                    // Formato ISO-8601 en UTC, como Instant.toString() del cliente Java.
                    mensajes.MostrarMensaje("Inicio de sesion exitoso. Expira (UTC): " +
                        sesion.ExpiraEn.UtcDateTime.ToString("yyyy-MM-dd'T'HH:mm:ss'Z'", CultureInfo.InvariantCulture));
                    bool volverAlLogin;
                    try
                    {
                        volverAlLogin = await new ConversionVista(
                            controlador.CrearControladorConversion(),
                            controlador.CrearControladorCatalogo()).EjecutarMenuAsync();
                    }
                    finally
                    {
                        controlador.CerrarSesion();
                    }
                    if (volverAlLogin)
                    {
                        mensajes.MostrarMensaje("Sesion cerrada. Ingrese sus credenciales para continuar.");
                        continue;
                    }
                    mensajes.MostrarMensaje("Sesion finalizada.");
                    return;
                }
                catch (ErrorRESTModelo ex)
                {
                    mensajes.MostrarError(ex);
                }
                catch (ArgumentException ex)
                {
                    mensajes.MostrarError(ex.Message);
                }
            }
        }

        public void MensajeBienvenida()
        {
            mensajes.MostrarBanner(
@"+------------------------------------------------------------+
|    ____            _   _       _     ____  ____  _____     |
|   / ___|___  _ __ | | | |_ __ (_)   / ___||  _ \|___ /     |
|  | |   / _ \| '_ \| | | | '_ \| |  | |  _ | |_) | |_ \     |
|  | |__| (_) | | | | |_| | | | | |  | |_| ||  _ < ___) |    |
|   \____\___/|_| |_|\___/|_| |_|_|   \____||_| \_\____/     |
+------------------------------------------------------------+");
        }
    }
}
