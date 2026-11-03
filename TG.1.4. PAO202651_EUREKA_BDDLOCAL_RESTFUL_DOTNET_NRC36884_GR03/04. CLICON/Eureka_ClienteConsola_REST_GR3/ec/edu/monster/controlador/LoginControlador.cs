using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    /// <summary>Coordina el inicio y cierre de sesión y comparte el cliente REST con los demás controladores.</summary>
    public class LoginControlador
    {
        private readonly ClienteRESTEureka clienteRest;

        public LoginControlador()
        {
            clienteRest = new ClienteRESTEureka();
        }

        public Task<SesionModelo> AutenticarAsync(LoginModelo modelo)
        {
            if (modelo == null || string.IsNullOrWhiteSpace(modelo.Usuario) || string.IsNullOrEmpty(modelo.Contrasenia))
                throw new ArgumentException("Usuario y contraseña son obligatorios.");
            return clienteRest.IniciarSesionAsync(modelo.Usuario.Trim(), modelo.Contrasenia);
        }

        public CuentaControlador CrearControladorCuenta()
        {
            return new CuentaControlador(clienteRest);
        }

        public TransaccionControlador CrearControladorTransaccion()
        {
            return new TransaccionControlador(clienteRest);
        }

        public SesionModelo? Sesion => clienteRest.Sesion;

        public string EndpointActivo => clienteRest.EndpointActivo;

        public Task CerrarSesionAsync()
        {
            return clienteRest.CerrarSesionAsync();
        }
    }
}
