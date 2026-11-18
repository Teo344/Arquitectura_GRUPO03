namespace ec.edu.monster.config
{
    /// <summary>
    /// Parámetros externos de la aplicación, leídos de appsettings.json (equivale a
    /// config.properties del Taller 2.1). Sacarlos del código permite cambiar de entorno sin
    /// recompilar. La cadena de conexión está en la sección ConnectionStrings:SqlServerConnection.
    /// </summary>
    public sealed class Configuracion
    {
        private const int TimeoutPorDefecto = 15;

        public string ApiUrlBase { get; init; } = "";
        public string ApiClave { get; init; } = "";
        public TimeSpan ApiTimeout { get; init; } = TimeSpan.FromSeconds(TimeoutPorDefecto);
        public string UniversidadesUrlBase { get; init; } = "";

        /// <exception cref="InvalidOperationException">si falta una clave obligatoria</exception>
        public static Configuracion Cargar(IConfiguration configuracion)
        {
            return new Configuracion
            {
                ApiUrlBase = Requerido(configuracion, "RestCountries:UrlBase"),
                ApiClave = Requerido(configuracion, "RestCountries:Clave"),
                ApiTimeout = TimeSpan.FromSeconds(Entero(configuracion, "RestCountries:TimeoutSegundos")),
                UniversidadesUrlBase = Requerido(configuracion, "Universidades:UrlBase")
            };
        }

        private static string Requerido(IConfiguration configuracion, string clave)
        {
            string? valor = configuracion[clave];
            if (string.IsNullOrWhiteSpace(valor))
            {
                throw new InvalidOperationException("Falta la propiedad " + clave + " en appsettings.json");
            }
            return valor.Trim();
        }

        private static int Entero(IConfiguration configuracion, string clave)
        {
            string? valor = configuracion[clave];
            if (string.IsNullOrWhiteSpace(valor))
            {
                return TimeoutPorDefecto;
            }
            if (!int.TryParse(valor.Trim(), out int numero) || numero <= 0)
            {
                throw new InvalidOperationException("La propiedad " + clave + " debe ser un entero positivo");
            }
            return numero;
        }

        /// <summary>No muestra la clave de la API si el objeto llega a imprimirse en un log.</summary>
        public override string ToString()
        {
            return "Configuracion[ApiUrlBase=" + ApiUrlBase + ", ApiTimeout=" + ApiTimeout
                + ", UniversidadesUrlBase=" + UniversidadesUrlBase + "]";
        }
    }
}
