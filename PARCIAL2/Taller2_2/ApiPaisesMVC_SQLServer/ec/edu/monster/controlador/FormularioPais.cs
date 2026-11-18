namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Datos tal como los escribe el usuario en el formulario de país (todo texto). La vista solo
    /// envía los campos aquí (model binding); convertirlos y validarlos es trabajo del controlador y
    /// del servicio.
    /// </summary>
    public class FormularioPais
    {
        public string? Codigo { get; set; }
        public string? Nombre { get; set; }
        public string? Capital { get; set; }
        public string? Region { get; set; }
        public string? SubRegion { get; set; }
        public string? Poblacion { get; set; }
        public string? Area { get; set; }
        public string? Moneda { get; set; }

        public FormularioPais()
        {
        }

        public FormularioPais(string? codigo, string? nombre, string? capital, string? region,
            string? poblacion, string? moneda, string? subRegion = null, string? area = null)
        {
            Codigo = codigo;
            Nombre = nombre;
            Capital = capital;
            Region = region;
            SubRegion = subRegion;
            Poblacion = poblacion;
            Area = area;
            Moneda = moneda;
        }
    }
}
