using ec.edu.monster.modelo.entidad;

namespace ec.edu.monster.vista.modelo
{
    /// <summary>Datos de UniversidadesVista (actividad avanzada).</summary>
    public class UniversidadesVistaModelo
    {
        /// <summary>País elegido en los filtros; null para todos.</summary>
        public string? PaisFiltro { get; set; }

        /// <summary>Texto buscado en el nombre, el dominio o el sitio web; null para no filtrar.</summary>
        public string? Texto { get; set; }

        public bool TieneTexto => !string.IsNullOrWhiteSpace(Texto);

        /// <summary>Valor inicial del campo para importar.</summary>
        public string PaisImportar { get; set; } = "Ecuador";

        public IReadOnlyList<Universidad> Universidades { get; set; } = Array.Empty<Universidad>();

        public IReadOnlyDictionary<string, long> ConteoPorPais { get; set; } = new Dictionary<string, long>();

        public long TotalGuardadas => ConteoPorPais.Values.Sum();

        public string? ErrorCarga { get; set; }

        /// <summary>Página y filas por página de la tabla.</summary>
        public OpcionesLista Lista { get; set; } = new OpcionesLista();
    }
}
