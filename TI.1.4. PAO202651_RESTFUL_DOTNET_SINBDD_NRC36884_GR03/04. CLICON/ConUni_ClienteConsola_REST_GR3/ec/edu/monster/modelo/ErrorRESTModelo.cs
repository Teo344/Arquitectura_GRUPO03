namespace ec.edu.monster.modelo
{
    /// <summary>Error de una respuesta REST que el controlador puede presentar en consola.</summary>
    public class ErrorRESTModelo : Exception
    {
        public int EstadoHttp { get; }
        public string? Codigo { get; }

        public ErrorRESTModelo(int estadoHttp, string? codigo, string mensaje) : base(mensaje)
        {
            EstadoHttp = estadoHttp;
            Codigo = codigo;
        }

        public ErrorRESTModelo(string mensaje, Exception? causa) : base(mensaje, causa)
        {
        }

        public bool RequiereNuevoLogin => EstadoHttp == 401 && (Codigo == "AUTH-001" || Codigo == "AUTH-002");
    }
}
