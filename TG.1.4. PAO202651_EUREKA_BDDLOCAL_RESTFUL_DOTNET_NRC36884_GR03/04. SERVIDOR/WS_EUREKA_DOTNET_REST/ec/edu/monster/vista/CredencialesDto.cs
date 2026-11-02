namespace ec.edu.monster.vista
{
    /// <summary>Cuerpo de POST api/autenticacion/login. Equipo es opcional: el nombre del equipo del cliente para logsession.</summary>
    public class CredencialesDto
    {
        public string Usuario { get; set; }
        public string Clave { get; set; }
        public string Equipo { get; set; }
    }
}
