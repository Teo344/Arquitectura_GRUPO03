namespace ec.edu.monster.modelo
{
    /// <summary>Credenciales que el empleado ingresa en la pantalla de inicio de sesión.</summary>
    public class LoginModelo
    {
        public string Usuario { get; set; }
        public string Contrasenia { get; set; }

        public LoginModelo()
        {
        }

        public LoginModelo(string usuario, string contrasenia)
        {
            Usuario = usuario;
            Contrasenia = contrasenia;
        }
    }
}
