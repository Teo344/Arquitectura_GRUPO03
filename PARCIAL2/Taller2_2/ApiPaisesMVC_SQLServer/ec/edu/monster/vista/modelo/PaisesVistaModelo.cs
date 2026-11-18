using ec.edu.monster.controlador;
using ec.edu.monster.modelo.entidad;

namespace ec.edu.monster.vista.modelo
{
    /// <summary>
    /// Datos que el PaisControlador entrega a PaisesVista: la lista filtrada, los conteos de los
    /// filtros por región y el formulario (nuevo o en edición).
    /// </summary>
    public class PaisesVistaModelo
    {
        public FiltroPaises Filtro { get; set; } = FiltroPaises.Ninguno;

        public IReadOnlyList<Pais> Paises { get; set; } = Array.Empty<Pais>();

        /// <summary>Países por región en el orden de las regiones (filtros dinámicos).</summary>
        public IReadOnlyList<KeyValuePair<Region, long>> ConteoRegiones { get; set; } =
            Array.Empty<KeyValuePair<Region, long>>();

        public long TotalGuardados => ConteoRegiones.Sum(c => c.Value);

        /// <summary>Motivo por el que no se pudo leer la base de datos; null si se leyó.</summary>
        public string? ErrorCarga { get; set; }

        /// <summary>Error que no pertenece al formulario (p. ej. el país elegido ya no existe).</summary>
        public string? ErrorGeneral { get; set; }

        public FormularioPais Formulario { get; set; } = new FormularioPais();

        /// <summary>Id del país en edición; null si el formulario registra uno nuevo.</summary>
        public int? IdEditando { get; set; }

        public string? CodigoEditando { get; set; }

        public IReadOnlyList<string> ErroresFormulario { get; set; } = Array.Empty<string>();

        public bool Editando => IdEditando != null;

        /// <summary>Parámetros de la búsqueda actual, para conservarlos en enlaces y formularios.</summary>
        public Dictionary<string, string?> RutaFiltro(Region? region = null, bool cambiarRegion = false)
        {
            Region? elegida = cambiarRegion ? region : Filtro.Region;
            Dictionary<string, string?> ruta = new Dictionary<string, string?>();
            if (Filtro.TieneTexto)
            {
                ruta["campo"] = Filtro.Campo.ToString().ToLowerInvariant();
                ruta["texto"] = Filtro.Texto;
            }
            if (elegida != null)
            {
                ruta["region"] = elegida.Value.ToString().ToLowerInvariant();
            }
            return ruta;
        }
    }
}
