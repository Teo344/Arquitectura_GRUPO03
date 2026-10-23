namespace ec.edu.monster.modelo
{
    public class SesionModelo
    {
        public string Usuario { get; }
        public string Token { get; }
        public DateTimeOffset ExpiraEn { get; }

        public SesionModelo(string usuario, string token, DateTimeOffset expiraEn)
        {
            Usuario = usuario;
            Token = token;
            ExpiraEn = expiraEn.ToUniversalTime();
        }
    }
}
