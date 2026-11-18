namespace ec.edu.monster.modelo.entidad
{
    /// <summary>
    /// País almacenado en la tabla Paises. Las propiedades solo normalizan los textos (recortan
    /// espacios, código en mayúsculas, vacíos como null); las reglas de negocio se validan en
    /// PaisServicio.
    /// </summary>
    public class Pais
    {
        private string? codigo;
        private string? nombre;
        private string? capital;
        private string? subRegion;
        private string? moneda;

        /// <summary>Clave generada por SQL Server; 0 si aún no se guarda.</summary>
        public int Id { get; set; }

        /// <summary>Código ISO 3166-1 alfa-3 (p. ej. ECU), único.</summary>
        public string? Codigo
        {
            get => codigo;
            set => codigo = Limpiar(value)?.ToUpperInvariant();
        }

        /// <summary>Nombre común.</summary>
        public string? Nombre
        {
            get => nombre;
            set => nombre = Limpiar(value);
        }

        /// <summary>Capital principal; null si no tiene.</summary>
        public string? Capital
        {
            get => capital;
            set => capital = Limpiar(value);
        }

        /// <summary>Región geográfica; null solo mientras no se valida.</summary>
        public Region? Region { get; set; }

        public string? SubRegion
        {
            get => subRegion;
            set => subRegion = Limpiar(value);
        }

        /// <summary>Número de habitantes.</summary>
        public long Poblacion { get; set; }

        /// <summary>Área territorial en km².</summary>
        public double Area { get; set; }

        /// <summary>Nombre de la moneda principal; null si no tiene.</summary>
        public string? Moneda
        {
            get => moneda;
            set => moneda = Limpiar(value);
        }

        public bool EsNuevo => Id == 0;

        /// <summary>País que todavía no existe en la base de datos.</summary>
        public static Pais Nuevo(string? codigo, string? nombre, string? capital, Region? region,
            long poblacion, string? moneda, string? subRegion = null, double area = 0)
        {
            return new Pais
            {
                Codigo = codigo,
                Nombre = nombre,
                Capital = capital,
                Region = region,
                SubRegion = subRegion,
                Poblacion = poblacion,
                Area = area,
                Moneda = moneda
            };
        }

        /// <summary>Copia con otro id (el original no cambia).</summary>
        public Pais ConId(int nuevoId)
        {
            Pais copia = Copiar();
            copia.Id = nuevoId;
            return copia;
        }

        public Pais Copiar()
        {
            return (Pais)MemberwiseClone();
        }

        /// <summary>Copia los datos (no el id) de otro país; se usa al actualizar.</summary>
        public void CopiarDatosDe(Pais otro)
        {
            Codigo = otro.Codigo;
            Nombre = otro.Nombre;
            Capital = otro.Capital;
            Region = otro.Region;
            SubRegion = otro.SubRegion;
            Poblacion = otro.Poblacion;
            Area = otro.Area;
            Moneda = otro.Moneda;
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
