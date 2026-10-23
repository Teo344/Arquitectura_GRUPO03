namespace ec.edu.monster.modelo
{
    public class LoginModelo
    {
        public string Usuario { get; set; } = string.Empty;
        public string Contrasenia { get; set; } = string.Empty;

        public LoginModelo() { }

        public LoginModelo(string usuario, string contrasenia)
        {
            Usuario = usuario;
            Contrasenia = contrasenia;
        }
    }
}
