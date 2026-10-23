namespace ec.edu.monster.modelo
{
    public class CategoriaModelo
    {
        public string Codigo { get; }
        public string Nombre { get; }
        public bool Convertible { get; }

        public CategoriaModelo(string codigo, string nombre, bool convertible)
        {
            Codigo = codigo;
            Nombre = nombre;
            Convertible = convertible;
        }
    }
}
