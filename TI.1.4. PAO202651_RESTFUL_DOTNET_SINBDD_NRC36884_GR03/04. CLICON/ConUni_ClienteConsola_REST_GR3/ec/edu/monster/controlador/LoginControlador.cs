using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    /// <summary>Coordina el login y comparte una sola sesión con los demás controladores.</summary>
    public class LoginControlador
    {
        private readonly ClienteRESTConversor clienteRest;

        public LoginControlador()
        {
            clienteRest = new ClienteRESTConversor();
        }

        public Task<SesionModelo> AutenticarAsync(LoginModelo modelo)
        {
            if (modelo == null || string.IsNullOrWhiteSpace(modelo.Usuario) ||
                string.IsNullOrWhiteSpace(modelo.Contrasenia))
                throw new ArgumentException("Usuario y contraseña son obligatorios.");
            return clienteRest.LoginAsync(modelo.Usuario, modelo.Contrasenia);
        }

        public ConversionControlador CrearControladorConversion() => new ConversionControlador(clienteRest);

        public CatalogoControlador CrearControladorCatalogo() => new CatalogoControlador(clienteRest);

        public string EndpointActivo => clienteRest.EndpointActivo;

        public void CerrarSesion() => clienteRest.CerrarSesion();
    }
}
