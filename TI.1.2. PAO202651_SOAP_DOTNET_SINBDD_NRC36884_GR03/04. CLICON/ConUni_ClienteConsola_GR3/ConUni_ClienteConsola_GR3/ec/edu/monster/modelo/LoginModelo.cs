namespace ec.edu.monster.modelo
{
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
