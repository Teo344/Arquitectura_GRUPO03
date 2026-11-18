namespace ec.edu.monster.modelo.entidad
{
    /// <summary>
    /// Universidad obtenida de la API pública Hipolabs (actividad avanzada del taller). Se guarda en
    /// la tabla Universidades; el par nombre + país es único para que reimportar no duplique.
    /// </summary>
    public class Universidad
    {
        private string? nombre;
        private string? dominio;
        private string? sitioWeb;
        private string? pais;

        public int Id { get; set; }

        public string? Nombre
        {
            get => nombre;
            set => nombre = Limpiar(value);
        }

        /// <summary>Primer dominio de la lista (p. ej. epn.edu.ec).</summary>
        public string? Dominio
        {
            get => dominio;
            set => dominio = Limpiar(value);
        }

        /// <summary>Primera página web de la lista.</summary>
        public string? SitioWeb
        {
            get => sitioWeb;
            set => sitioWeb = Limpiar(value);
        }

        /// <summary>Nombre del país en inglés, como lo envía la API (p. ej. Ecuador).</summary>
        public string? Pais
        {
            get => pais;
            set => pais = Limpiar(value);
        }

        public static Universidad Nueva(string? nombre, string? dominio, string? sitioWeb, string? pais)
        {
            return new Universidad { Nombre = nombre, Dominio = dominio, SitioWeb = sitioWeb, Pais = pais };
        }

        private static string? Limpiar(string? texto)
        {
            if (texto == null)
            {
                return null;
            }
            string recortado = texto.Trim();
            return recortado.Length == 0 ? null : recortado;
        }
    }
}
