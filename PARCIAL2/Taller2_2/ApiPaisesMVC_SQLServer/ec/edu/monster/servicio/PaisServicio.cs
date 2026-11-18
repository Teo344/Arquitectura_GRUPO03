using System.Text.RegularExpressions;
using ec.edu.monster.modelo.api;
using ec.edu.monster.modelo.dao;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using Microsoft.Extensions.Logging.Abstractions;

namespace ec.edu.monster.servicio
{
    /// <summary>
    /// Reglas de negocio de los países: valida, orquesta la API y la base de datos y arma los
    /// reportes. Recibe sus dependencias por constructor como interfaces, así se prueba con dobles en
    /// memoria sin red ni SQL Server.
    /// </summary>
    public class PaisServicio
    {
        /// <summary>Tamaño de las columnas NVARCHAR(100) de la tabla Paises.</summary>
        internal const int LongitudMaxima = 100;

        private static readonly Regex CodigoIso = new Regex("^[A-Z]{3}$");

        private readonly IPaisDAO dao;
        private readonly IPaisApiCliente api;
        private readonly ILogger<PaisServicio> log;

        public PaisServicio(IPaisDAO dao, IPaisApiCliente api, ILogger<PaisServicio>? log = null)
        {
            this.dao = dao ?? throw new ArgumentNullException(nameof(dao));
            this.api = api ?? throw new ArgumentNullException(nameof(api));
            this.log = log ?? NullLogger<PaisServicio>.Instance;
        }

        /// <summary>
        /// Comprueba la base de datos e informa cómo está configurada la API. No consulta la API para
        /// no gastar la cuota mensual de solicitudes.
        /// </summary>
        public async Task<EstadoSistema> VerificarEstadoAsync()
        {
            bool demostracion = api.EsModoDemostracion;
            try
            {
                return new EstadoSistema(true, await dao.ContarAsync(), null, demostracion);
            }
            catch (ExcepcionPersistencia e)
            {
                return new EstadoSistema(false, 0, e.Message, demostracion);
            }
        }

        /// <summary>
        /// CU-01: trae todos los países de la API y los guarda. Reimportar no duplica: los países ya
        /// registrados (mismo código) se actualizan.
        /// </summary>
        /// <exception cref="ExcepcionApiExterna">si la API falla; en ese caso la base no se toca</exception>
        public async Task<ResultadoImportacion> ImportarDesdeApiAsync()
        {
            Dictionary<string, Pais> porCodigo = new Dictionary<string, Pais>();
            List<Pais> enOrden = new List<Pais>();
            foreach (Pais pais in await api.ObtenerTodosAsync())
            {
                List<string> errores = Validar(pais);
                if (errores.Count == 0)
                {
                    if (porCodigo.TryAdd(pais.Codigo!, pais))
                    {
                        enOrden.Add(pais);
                    }
                }
                else
                {
                    log.LogWarning("Se omite {Nombre} de la importación: {Errores}", pais.Nombre, string.Join(" ", errores));
                }
            }
            return await dao.GuardarOActualizarAsync(enOrden);
        }

        /// <summary>
        /// CU-02 (Actividad 1): busca en la API. Si la API no está disponible, responde con los países
        /// ya guardados (patrón fallback) e indica el motivo en el resultado.
        /// </summary>
        public async Task<ResultadoBusqueda> BuscarPorNombreAsync(string? nombre)
        {
            if (string.IsNullOrWhiteSpace(nombre))
            {
                throw new ExcepcionValidacion("Ingrese el nombre del país a buscar.");
            }
            string texto = nombre.Trim();
            try
            {
                return ResultadoBusqueda.DesdeApi(await api.BuscarPorNombreAsync(texto));
            }
            catch (ExcepcionApiExterna e)
            {
                log.LogWarning("API no disponible, se responde con la base local: {Mensaje}", e.Message);
                return ResultadoBusqueda.DesdeBaseLocal(await dao.BuscarPorNombreAsync(texto),
                    e.Message + " Se muestran los países guardados localmente.");
            }
        }

        /// <summary>
        /// CU-02 (opcional): guarda un resultado de la búsqueda en la API. Si el código ya existe,
        /// actualiza sus datos.
        /// </summary>
        public Task<ResultadoImportacion> GuardarResultadoAsync(Pais pais)
        {
            ValidarOLanzar(pais);
            return dao.GuardarOActualizarAsync(new[] { pais });
        }

        /// <summary>CU-03: registra un país ingresado por el usuario.</summary>
        public async Task<Pais> RegistrarAsync(Pais pais)
        {
            ArgumentNullException.ThrowIfNull(pais);
            if (!pais.EsNuevo)
            {
                throw new ExcepcionValidacion("Un país nuevo no debe tener id.");
            }
            ValidarOLanzar(pais);
            if (await dao.BuscarPorCodigoAsync(pais.Codigo!) != null)
            {
                throw CodigoDuplicado(pais.Codigo);
            }
            return await dao.InsertarAsync(pais);
        }

        /// <summary>CU-04: país por id.</summary>
        public async Task<Pais> ObtenerPorIdAsync(int id)
        {
            return await dao.BuscarPorIdAsync(id) ?? throw NoEncontrado(id);
        }

        /// <summary>CU-04: todos los países, ordenados por nombre.</summary>
        public Task<IReadOnlyList<Pais>> ListarTodosAsync()
        {
            return dao.ListarTodosAsync();
        }

        /// <summary>
        /// CU-04 y CU-07 (Actividades 3 y 4): países que cumplen la búsqueda y el filtro de región.
        /// Sin criterios, devuelve todos.
        /// </summary>
        public Task<IReadOnlyList<Pais>> BuscarAsync(FiltroPaises? filtro)
        {
            if (filtro == null || filtro.EstaVacio)
            {
                return dao.ListarTodosAsync();
            }
            if (filtro.TieneTexto && filtro.Texto!.Trim().Length > LongitudMaxima)
            {
                throw new ExcepcionValidacion("El texto a buscar no puede superar " + LongitudMaxima + " caracteres.");
            }
            return dao.BuscarAsync(filtro);
        }

        /// <summary>CU-05: actualiza un país existente.</summary>
        public async Task<Pais> ActualizarAsync(Pais pais)
        {
            ArgumentNullException.ThrowIfNull(pais);
            if (pais.EsNuevo)
            {
                throw new ExcepcionValidacion("Seleccione el país que desea actualizar.");
            }
            ValidarOLanzar(pais);
            Pais? otro = await dao.BuscarPorCodigoAsync(pais.Codigo!);
            if (otro != null && otro.Id != pais.Id)
            {
                throw CodigoDuplicado(pais.Codigo);
            }
            if (!await dao.ActualizarAsync(pais))
            {
                throw NoEncontrado(pais.Id);
            }
            return pais;
        }

        /// <summary>CU-06: elimina un país.</summary>
        public async Task EliminarAsync(int id)
        {
            if (!await dao.EliminarAsync(id))
            {
                throw NoEncontrado(id);
            }
        }

        /// <summary>CU-07 (Actividad 4): países de una región.</summary>
        public Task<IReadOnlyList<Pais>> BuscarPorRegionAsync(Region? region)
        {
            if (region == null)
            {
                throw new ExcepcionValidacion("Seleccione una región.");
            }
            return dao.BuscarPorRegionAsync(region.Value);
        }

        /// <summary>
        /// Cantidad de países por región para los filtros dinámicos (Actividad 4), en el orden de las
        /// regiones e incluidas las que no tienen países.
        /// </summary>
        public async Task<IReadOnlyList<KeyValuePair<Region, long>>> ContarPorRegionAsync()
        {
            IReadOnlyDictionary<Region, long> conteo = await dao.ContarPorRegionAsync();
            return RegionExtensiones.Todas
                .Select(r => new KeyValuePair<Region, long>(r, conteo.TryGetValue(r, out long total) ? total : 0))
                .ToList();
        }

        /// <summary>
        /// CU-08 (Actividad 5): países por región, país más poblado, región con más países y promedio
        /// poblacional por región.
        /// </summary>
        public async Task<EstadisticasPaises> GenerarEstadisticasAsync()
        {
            // Se ordena aquí (y no se confía en el orden del DAO): mayor cantidad primero y, ante
            // empate, el orden del enum
            List<KeyValuePair<Region, long>> porRegion = (await dao.ContarPorRegionAsync())
                .Where(e => e.Value > 0)
                .OrderByDescending(e => e.Value)
                .ThenBy(e => e.Key)
                .ToList();

            long total = porRegion.Sum(e => e.Value);
            if (total == 0)
            {
                return EstadisticasPaises.Vacias();
            }
            Region regionConMasPaises = porRegion[0].Key;
            Pais? masPoblado = await dao.ObtenerMasPobladoAsync();
            List<KeyValuePair<Region, double>> promedios = (await dao.PromedioPoblacionPorRegionAsync())
                .OrderByDescending(e => e.Value)
                .ThenBy(e => e.Key)
                .ToList();
            return new EstadisticasPaises(porRegion, total, masPoblado, regionConMasPaises, promedios);
        }

        // ------------------------------------------------------------ validación

        /// <summary>
        /// Reglas de un país válido. Devuelve todos los errores encontrados (lista vacía si es válido).
        /// </summary>
        internal List<string> Validar(Pais pais)
        {
            List<string> errores = new List<string>();
            if (pais.Codigo == null || !CodigoIso.IsMatch(pais.Codigo))
            {
                errores.Add("El código debe tener 3 letras (ISO alfa-3, p. ej. ECU).");
            }
            if (pais.Nombre == null)
            {
                errores.Add("El nombre es obligatorio.");
            }
            if (pais.Region == null)
            {
                errores.Add("La región es obligatoria.");
            }
            if (pais.Poblacion < 0)
            {
                errores.Add("La población no puede ser negativa.");
            }
            if (pais.Area < 0 || double.IsNaN(pais.Area) || double.IsInfinity(pais.Area))
            {
                errores.Add("El área no puede ser negativa.");
            }
            ValidarLongitud(pais.Nombre, "El nombre", errores);
            ValidarLongitud(pais.Capital, "La capital", errores);
            ValidarLongitud(pais.SubRegion, "La subregión", errores);
            ValidarLongitud(pais.Moneda, "La moneda", errores);
            return errores;
        }

        private void ValidarOLanzar(Pais pais)
        {
            ArgumentNullException.ThrowIfNull(pais);
            List<string> errores = Validar(pais);
            if (errores.Count > 0)
            {
                throw new ExcepcionValidacion(errores);
            }
        }

        private static void ValidarLongitud(string? valor, string campo, List<string> errores)
        {
            if (valor != null && valor.Length > LongitudMaxima)
            {
                errores.Add(campo + " no puede superar " + LongitudMaxima + " caracteres.");
            }
        }

        private static ExcepcionValidacion CodigoDuplicado(string? codigo)
        {
            return new ExcepcionValidacion("Ya existe un país con el código " + codigo + ".");
        }

        private static ExcepcionNoEncontrado NoEncontrado(int id)
        {
            return new ExcepcionNoEncontrado("No existe un país con id " + id + ".");
        }
    }
}
