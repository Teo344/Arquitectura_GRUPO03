namespace ec.edu.monster.modelo
{
    public class SesionModelo
    {
        public string Usuario { get; }
        public string Token { get; }
        public string ExpiraEn { get; }

        public SesionModelo(string usuario, string token, string expiraEn)
        {
            Usuario = usuario;
            Token = token;
            ExpiraEn = expiraEn;
        }
    }
}
