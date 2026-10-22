using System;
using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.util;

namespace ec.edu.monster.vista
{
    public class LoginVista
    {
        private readonly MensajesVista mensajes = new MensajesVista();

        public void Iniciar()
        {
            MensajeBienvenida();
            mensajes.MostrarTitulo("=== SISTEMA DE CONVERSION DE UNIDADES - CLIENTE CONSOLA GR3 ===");

            LoginControlador controlador;
            try
            {
                controlador = new LoginControlador();
                mensajes.MostrarMensaje("Servidor SOAP configurado: " + controlador.EndpointActivo);
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
                    SesionModelo sesion = controlador.Autenticar(new LoginModelo(usuario, clave));
                    mensajes.MostrarMensaje("Inicio de sesion exitoso. Token: " + sesion.Token);

                    bool volverAlLogin = new ConversionVista(
                        controlador.CrearControladorConversion(),
                        controlador.CrearControladorCatalogo()).EjecutarMenu();
                    controlador.CerrarSesion();
                    if (volverAlLogin)
                    {
                        mensajes.MostrarMensaje("Sesion cerrada. Ingrese sus credenciales para continuar.");
                        continue;
                    }
                    mensajes.MostrarMensaje("Sesion finalizada.");
                    return;
                }
                catch (ErrorSOAPModelo ex)
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
