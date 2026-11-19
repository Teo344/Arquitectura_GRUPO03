namespace ec.edu.monster.vista.modelo
{
    /// <summary>
    /// Página, filas por página y orden de una tabla, leídos de la URL (?pagina=2&amp;tamano=25&amp;orden=poblacion&amp;dir=desc).
    /// Los valores inválidos se corrigen en lugar de fallar: un enlace viejo nunca rompe la pantalla.
    /// </summary>
    public sealed class OpcionesLista
    {
        public static readonly IReadOnlyList<int> TamanosPermitidos = new[] { 10, 25, 50 };
        public const int TamanoPorDefecto = 10;

        public int Pagina { get; set; } = 1;

        public int Tamano { get; set; } = TamanoPorDefecto;

        /// <summary>Clave de la columna por la que se ordena; null usa el orden por defecto de la tabla.</summary>
        public string? Orden { get; set; }

        /// <summary>"asc" o "desc".</summary>
        public string? Dir { get; set; }

        public int TamanoValido => TamanosPermitidos.Contains(Tamano) ? Tamano : TamanoPorDefecto;

        public int PaginaValida => Math.Max(1, Pagina);

        public bool Descendente => string.Equals(Dir, "desc", StringComparison.OrdinalIgnoreCase);

        /// <summary>Parámetros de la URL que difieren de los valores por defecto.</summary>
        public void AgregarA(IDictionary<string, string?> ruta, bool conPagina = true)
        {
            if (conPagina && PaginaValida > 1)
            {
                ruta["pagina"] = PaginaValida.ToString();
            }
            if (TamanoValido != TamanoPorDefecto)
            {
                ruta["tamano"] = TamanoValido.ToString();
            }
            if (!string.IsNullOrWhiteSpace(Orden))
            {
                ruta["orden"] = Orden;
                ruta["dir"] = Descendente ? "desc" : "asc";
            }
        }
    }

    /// <summary>Un botón de página; <see cref="Numero"/> null representa el salto "…".</summary>
    public sealed record EnlacePagina(int? Numero, string? Url, bool Actual);

    /// <summary>Una opción de "Filas por página".</summary>
    public sealed record EnlaceTamano(int Tamano, string Url, bool Actual);

    /// <summary>
    /// Pie de una tabla paginada (componentes/_Paginador.cshtml): "Mostrando 11–20 de 250 países", el
    /// selector de filas por página y los botones primera / anterior / números / siguiente / última.
    /// Con muchas páginas muestra la primera, la última y las vecinas de la actual, separadas por "…".
    /// </summary>
    public sealed class Paginacion
    {
        /// <summary>Hasta este número de páginas se muestran todos los botones.</summary>
        private const int MaxSinPuntos = 7;

        public int Total { get; private init; }
        public int Pagina { get; private init; }
        public int TotalPaginas { get; private init; }
        public int Tamano { get; private init; }
        public int Desde { get; private init; }
        public int Hasta { get; private init; }
        public string Sustantivo { get; private init; } = "";
        public string? UrlPrimera { get; private init; }
        public string? UrlAnterior { get; private init; }
        public string? UrlSiguiente { get; private init; }
        public string? UrlUltima { get; private init; }
        public IReadOnlyList<EnlacePagina> Paginas { get; private init; } = Array.Empty<EnlacePagina>();
        public IReadOnlyList<EnlaceTamano> Tamanos { get; private init; } = Array.Empty<EnlaceTamano>();

        /// <param name="total">filas en todas las páginas</param>
        /// <param name="pagina">página pedida (desde 1); se ajusta al rango válido</param>
        /// <param name="tamano">filas por página</param>
        /// <param name="url">URL de una página: (página, filas por página) → enlace</param>
        /// <param name="sustantivo">cómo se llaman las filas ("países")</param>
        public static Paginacion Crear(int total, int pagina, int tamano, Func<int, int, string> url, string sustantivo)
        {
            int paginas = Math.Max(1, (total + tamano - 1) / tamano);
            int actual = Math.Clamp(pagina, 1, paginas);
            int desde = (actual - 1) * tamano;

            List<EnlacePagina> numeros = new List<EnlacePagina>();
            for (int n = 1; n <= paginas; n++)
            {
                bool visible = paginas <= MaxSinPuntos || n == 1 || n == paginas || Math.Abs(n - actual) <= 1;
                if (visible)
                {
                    numeros.Add(new EnlacePagina(n, url(n, tamano), n == actual));
                }
                else if (numeros[^1].Numero != null)
                {
                    numeros.Add(new EnlacePagina(null, null, false));
                }
            }

            return new Paginacion
            {
                Total = total,
                Pagina = actual,
                TotalPaginas = paginas,
                Tamano = tamano,
                Desde = total == 0 ? 0 : desde + 1,
                Hasta = Math.Min(desde + tamano, total),
                Sustantivo = sustantivo,
                UrlPrimera = actual > 1 ? url(1, tamano) : null,
                UrlAnterior = actual > 1 ? url(actual - 1, tamano) : null,
                UrlSiguiente = actual < paginas ? url(actual + 1, tamano) : null,
                UrlUltima = actual < paginas ? url(paginas, tamano) : null,
                Paginas = numeros,
                // Al cambiar el tamaño se mantiene a la vista la primera fila que se estaba viendo
                Tamanos = OpcionesLista.TamanosPermitidos
                    .Select(t => new EnlaceTamano(t, url(desde / t + 1, t), t == tamano))
                    .ToList()
            };
        }

        /// <summary>Las filas de la página actual.</summary>
        public IReadOnlyList<T> Cortar<T>(IReadOnlyList<T> filas)
        {
            return filas.Skip((Pagina - 1) * Tamano).Take(Tamano).ToList();
        }
    }
}
