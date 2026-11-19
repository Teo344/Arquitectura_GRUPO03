using System.Globalization;
using ec.edu.monster.controlador;
using ec.edu.monster.modelo.entidad;

namespace ec.edu.monster.vista.modelo
{
    /// <summary>
    /// Datos que el PaisControlador entrega a PaisesVista: la lista filtrada, los conteos de los
    /// filtros por región, la página visible y el formulario del panel lateral (nuevo o en edición).
    /// <para>
    /// El orden se aplica a <b>todas</b> las filas filtradas y recién después se corta la página: así
    /// ordenar por población muestra los más poblados de toda la lista y no solo los de la página.
    /// </para>
    /// </summary>
    public class PaisesVistaModelo
    {
        private static readonly StringComparer Espanol =
            StringComparer.Create(CultureInfo.GetCultureInfo("es-EC"), CompareOptions.IgnoreCase | CompareOptions.IgnoreNonSpace);

        /// <summary>Columnas por las que se puede ordenar (clave de la URL → criterio).</summary>
        public static readonly IReadOnlyDictionary<string, Func<IEnumerable<Pais>, bool, IEnumerable<Pais>>> Ordenes =
            new Dictionary<string, Func<IEnumerable<Pais>, bool, IEnumerable<Pais>>>
            {
                ["codigo"] = (p, d) => Por(p, x => x.Codigo, d),
                ["nombre"] = (p, d) => Por(p, x => x.Nombre, d),
                ["capital"] = (p, d) => Por(p, x => x.Capital, d),
                ["region"] = (p, d) => Por(p, x => x.Region?.Nombre(), d),
                ["subregion"] = (p, d) => Por(p, x => x.SubRegion, d),
                ["poblacion"] = (p, d) => d ? p.OrderByDescending(x => x.Poblacion) : p.OrderBy(x => x.Poblacion),
                ["area"] = (p, d) => d ? p.OrderByDescending(x => x.Area) : p.OrderBy(x => x.Area),
                ["moneda"] = (p, d) => Por(p, x => x.Moneda, d)
            };

        private Paginacion? paginacion;
        private IReadOnlyList<Pais>? ordenados;

        public FiltroPaises Filtro { get; set; } = FiltroPaises.Ninguno;

        public OpcionesLista Lista { get; set; } = new OpcionesLista();

        /// <summary>Todos los países que cumplen el filtro (todas las páginas).</summary>
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

        /// <summary>Id del país en edición; null si el panel registra uno nuevo o está cerrado.</summary>
        public int? IdEditando { get; set; }

        public string? CodigoEditando { get; set; }

        /// <summary>El panel lateral está abierto para registrar un país nuevo.</summary>
        public bool RegistrandoNuevo { get; set; }

        /// <summary>País recién guardado: se resalta y se muestra la página donde quedó.</summary>
        public int? Resaltar { get; set; }

        public IReadOnlyList<string> ErroresFormulario { get; set; } = Array.Empty<string>();

        public bool Editando => IdEditando != null;

        public bool PanelAbierto => Editando || RegistrandoNuevo;

        /// <summary>Clave de la columna ordenada (nombre si la URL no indica otra válida).</summary>
        public string Orden => Lista.Orden != null && Ordenes.ContainsKey(Lista.Orden.ToLowerInvariant())
            ? Lista.Orden.ToLowerInvariant() : "nombre";

        public bool Descendente => Lista.Orden != null && Lista.Descendente;

        public IReadOnlyList<Pais> Ordenados => ordenados ??= Ordenes[Orden](Paises, Descendente).ToList();

        /// <summary>
        /// Paginación de la tabla. Si hay un país por resaltar (recién guardado), se abre la página
        /// donde quedó, aunque la URL no la indique.
        /// </summary>
        public Paginacion Paginar(Func<int, int, string> url)
        {
            if (paginacion != null)
            {
                return paginacion;
            }
            int tamano = Lista.TamanoValido;
            int pagina = Lista.PaginaValida;
            int? idDestacado = Resaltar ?? IdEditando;
            if (idDestacado != null && Lista.Pagina <= 1)
            {
                int indice = Ordenados.ToList().FindIndex(p => p.Id == idDestacado);
                if (indice >= 0)
                {
                    pagina = indice / tamano + 1;
                }
            }
            paginacion = Paginacion.Crear(Ordenados.Count, pagina, tamano, url, "países");
            return paginacion;
        }

        // ------------------------------------------------------------ rutas

        /// <summary>Parámetros de la vista actual (búsqueda, región, página y orden) para enlaces y formularios.</summary>
        public Dictionary<string, string?> Ruta(bool conPagina = true)
        {
            Dictionary<string, string?> ruta = RutaFiltro(Filtro.Region);
            Lista.AgregarA(ruta, conPagina);
            if (conPagina && paginacion != null)
            {
                // La página efectiva (p. ej. la del país resaltado), no solo la pedida en la URL
                ruta.Remove("pagina");
                if (paginacion.Pagina > 1)
                {
                    ruta["pagina"] = paginacion.Pagina.ToString();
                }
            }
            return ruta;
        }

        /// <summary>Ruta al elegir otra región: conserva la búsqueda y el orden, y vuelve a la página 1.</summary>
        public Dictionary<string, string?> RutaRegion(Region? region)
        {
            Dictionary<string, string?> ruta = RutaFiltro(region);
            Lista.AgregarA(ruta, conPagina: false);
            return ruta;
        }

        /// <summary>Ruta al pulsar el encabezado de una columna: la misma columna invierte el orden.</summary>
        public Dictionary<string, string?> RutaOrden(string clave)
        {
            Dictionary<string, string?> ruta = RutaFiltro(Filtro.Region);
            bool mismaColumna = Orden == clave;
            bool numerica = clave is "poblacion" or "area";
            bool descendente = mismaColumna ? !Descendente : numerica;
            if (Lista.TamanoValido != OpcionesLista.TamanoPorDefecto)
            {
                ruta["tamano"] = Lista.TamanoValido.ToString();
            }
            ruta["orden"] = clave;
            ruta["dir"] = descendente ? "desc" : "asc";
            return ruta;
        }

        /// <summary>Valor de aria-sort del encabezado de una columna.</summary>
        public string AriaOrden(string clave) => Orden != clave ? "none" : Descendente ? "descending" : "ascending";

        private Dictionary<string, string?> RutaFiltro(Region? region)
        {
            Dictionary<string, string?> ruta = new Dictionary<string, string?>();
            if (Filtro.TieneTexto)
            {
                ruta["campo"] = Filtro.Campo.ToString().ToLowerInvariant();
                ruta["texto"] = Filtro.Texto;
            }
            if (region != null)
            {
                ruta["region"] = region.Value.ToString().ToLowerInvariant();
            }
            return ruta;
        }

        private static IEnumerable<Pais> Por(IEnumerable<Pais> paises, Func<Pais, string?> clave, bool descendente)
        {
            // Los vacíos siempre al final; los textos con las reglas del español (sin tildes ni mayúsculas)
            IOrderedEnumerable<Pais> conValor = paises.OrderBy(p => string.IsNullOrWhiteSpace(clave(p)));
            return descendente
                ? conValor.ThenByDescending(p => clave(p) ?? "", Espanol)
                : conValor.ThenBy(p => clave(p) ?? "", Espanol);
        }
    }
}
