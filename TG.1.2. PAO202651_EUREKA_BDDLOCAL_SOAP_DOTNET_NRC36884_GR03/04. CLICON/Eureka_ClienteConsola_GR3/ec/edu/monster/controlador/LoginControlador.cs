using System;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    /// <summary>Coordina el inicio y cierre de sesión y comparte el cliente SOAP con los demás controladores.</summary>
    public class LoginControlador
    {
        private readonly ClienteWSEureka clienteWs;

        public LoginControlador()
        {
            clienteWs = new ClienteWSEureka();
        }

        public SesionModelo Autenticar(LoginModelo modelo)
        {
            if (modelo == null || string.IsNullOrWhiteSpace(modelo.Usuario) || string.IsNullOrEmpty(modelo.Contrasenia))
                throw new ArgumentException("Usuario y contraseña son obligatorios.");
            return clienteWs.IniciarSesion(modelo.Usuario.Trim(), modelo.Contrasenia);
        }

        public CuentaControlador CrearControladorCuenta()
        {
            return new CuentaControlador(clienteWs);
        }

        public TransaccionControlador CrearControladorTransaccion()
        {
            return new TransaccionControlador(clienteWs);
        }

        public SesionModelo Sesion => clienteWs.Sesion;

        public string EndpointActivo => clienteWs.EndpointActivo;

        public void CerrarSesion()
        {
            clienteWs.CerrarSesion();
        }
    }
}
