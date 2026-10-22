using System;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;

namespace ec.edu.monster.controlador
{
    /// <summary>Comparte la misma sesión SOAP con los controladores de conversión y catálogo.</summary>
    public class LoginControlador
    {
        private readonly ClienteSOAPConversor clienteSoap;

        public LoginControlador()
        {
            clienteSoap = new ClienteSOAPConversor();
        }

        public SesionModelo Autenticar(LoginModelo modelo)
        {
            if (modelo == null || string.IsNullOrWhiteSpace(modelo.Usuario) ||
                string.IsNullOrWhiteSpace(modelo.Contrasenia))
                throw new ArgumentException("Usuario y contraseña son obligatorios.");
            return clienteSoap.Login(modelo.Usuario, modelo.Contrasenia);
        }

        public ConversionControlador CrearControladorConversion()
        {
            return new ConversionControlador(clienteSoap);
        }

        public CatalogoControlador CrearControladorCatalogo()
        {
            return new CatalogoControlador(clienteSoap);
        }

        public string EndpointActivo => clienteSoap.EndpointActivo;

        public void CerrarSesion()
        {
            clienteSoap.CerrarSesion();
        }
    }
}
