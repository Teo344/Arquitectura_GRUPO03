namespace ec.edu.monster.modelo
{
    public class UnidadModelo
    {
        public string Codigo { get; }
        public string Nombre { get; }
        public string Simbolo { get; }

        public UnidadModelo(string codigo, string nombre, string simbolo)
        {
            Codigo = codigo;
            Nombre = nombre;
            Simbolo = simbolo;
        }
    }
}
