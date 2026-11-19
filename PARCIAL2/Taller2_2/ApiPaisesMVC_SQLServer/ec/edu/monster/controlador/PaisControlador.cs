using System.Globalization;
using System.Text.RegularExpressions;
using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using ec.edu.monster.servicio;
using ec.edu.monster.vista.modelo;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Routing;

namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Controlador de la gestión de países (CU-01 a CU-07). Recibe las acciones de la vista,
    /// convierte la entrada de texto en objetos del modelo y delega en <see cref="PaisServicio"/>.
    /// No contiene SQL, JSON ni reglas de negocio.
    /// Las fallas llegan como ExcepcionAplicacion con un mensaje listo para mostrar.
    /// </summary>
    public class PaisControlador : ControladorBase
    {
        private static readonly Regex Separadores = new Regex(@"[\s.,'_]");

        private readonly PaisServicio servicio;

        public PaisControlador(PaisServicio servicio)
        {
            this.servicio = servicio ?? throw new ArgumentNullException(nameof(servicio));
        }

        // ------------------------------------------------ CU-04, Actividades 3 y 4

        /// <summary>
        /// CU-04: lista paginada con búsqueda (Actividad 3) y filtro por región (Actividad 4).
        /// </summary>
        /// <param name="resaltar">id del país recién guardado: se resalta y se abre su página</param>
        [HttpGet("paises")]
        public async Task<IActionResult> Index(string? campo, string? texto, string? region,
            [FromQuery] OpcionesLista lista, int? resaltar)
        {
            PaisesVistaModelo modelo = await CargarAsync(campo, texto, region, lista);
            modelo.Resaltar = resaltar;
            return Vista("PaisesVista", modelo);
        }

        /// <summary>CU-03: el mismo listado con el panel lateral abierto para registrar un país.</summary>
        [HttpGet("paises/nuevo")]
        public async Task<IActionResult> Nuevo(string? campo, string? texto, string? region, [FromQuery] OpcionesLista lista)
        {
            PaisesVistaModelo modelo = await CargarAsync(campo, texto, region, lista);
            modelo.RegistrandoNuevo = true;
            return Vista("PaisesVista", modelo);
        }

        /// <summary>CU-04: el mismo listado con el país elegido en el panel lateral para editarlo.</summary>
        [HttpGet("paises/{id:int}")]
        public async Task<IActionResult> Editar(int id, string? campo, string? texto, string? region,
            [FromQuery] OpcionesLista lista)
        {
            PaisesVistaModelo modelo = await CargarAsync(campo, texto, region, lista);
            try
            {
                Pais pais = await servicio.ObtenerPorIdAsync(id);
                modelo.IdEditando = pais.Id;
                modelo.CodigoEditando = pais.Codigo;
                modelo.Formulario = AFormulario(pais);
            }
            catch (ExcepcionAplicacion e)
            {
                modelo.ErrorGeneral = e.Message;
            }
            return Vista("PaisesVista", modelo);
        }

        // ------------------------------------------------ CU-01

        [HttpPost("paises/importar")]
        public async Task<IActionResult> Importar()
        {
            try
            {
                ResultadoImportacion resultado = await servicio.ImportarDesdeApiAsync();
                string mensaje = "Importación completa: " + vista.Formato.Paises(resultado.Recibidos) + " recibidos · "
                    + vista.Formato.Entero(resultado.Nuevos) + " nuevos · "
                    + vista.Formato.Entero(resultado.Actualizados) + " actualizados.";
                if (resultado.Recibidos <= 1)
                {
                    AvisarAdvertencia(mensaje + " Si usas la clave demo, la API solo devuelve un país de muestra.");
                }
                else
                {
                    AvisarExito(mensaje);
                }
            }
            catch (ExcepcionAplicacion e)
            {
                AvisarError("No se pudo importar: " + e.Message);
            }
            return RedirectToAction(nameof(Index));
        }

        // ------------------------------------------------ CU-03, CU-05, CU-06

        [HttpPost("paises/registrar")]
        public async Task<IActionResult> Registrar(FormularioPais formulario, [FromQuery] string? campo, [FromQuery] string? texto,
            [FromQuery] string? region, [FromQuery] OpcionesLista lista)
        {
            try
            {
                Pais pais = await servicio.RegistrarAsync(Convertir(null, formulario));
                AvisarExito("País registrado: " + pais.Nombre + " (" + pais.Codigo + ").");
                // El panel se cierra y la tabla muestra la página donde quedó el país, resaltado
                return RedirectToAction(nameof(Index), Vuelta(campo, texto, region, lista, pais.Id));
            }
            catch (ExcepcionAplicacion e)
            {
                PaisesVistaModelo modelo = await CargarAsync(campo, texto, region, lista);
                modelo.RegistrandoNuevo = true;
                modelo.Formulario = formulario;
                modelo.ErroresFormulario = Errores(e);
                return Vista("PaisesVista", modelo);
            }
        }

        [HttpPost("paises/{id:int}/actualizar")]
        public async Task<IActionResult> Actualizar(int id, FormularioPais formulario, [FromQuery] string? campo,
            [FromQuery] string? texto, [FromQuery] string? region, [FromQuery] OpcionesLista lista)
        {
            try
            {
                Pais pais = await servicio.ActualizarAsync(Convertir(id, formulario));
                AvisarExito("Cambios guardados en " + pais.Nombre + ".");
                return RedirectToAction(nameof(Index), Vuelta(campo, texto, region, lista, id));
            }
            catch (ExcepcionNoEncontrado e)
            {
                AvisarError(e.Message);
                return RedirectToAction(nameof(Index), Vuelta(campo, texto, region, lista));
            }
            catch (ExcepcionAplicacion e)
            {
                PaisesVistaModelo modelo = await CargarAsync(campo, texto, region, lista);
                modelo.IdEditando = id;
                modelo.CodigoEditando = formulario.Codigo?.Trim().ToUpperInvariant();
                modelo.Formulario = formulario;
                modelo.ErroresFormulario = Errores(e);
                return Vista("PaisesVista", modelo);
            }
        }

        [HttpPost("paises/{id:int}/eliminar")]
        public async Task<IActionResult> Eliminar(int id, [FromQuery] string? campo, [FromQuery] string? texto,
            [FromQuery] string? region, [FromQuery] OpcionesLista lista)
        {
            try
            {
                await servicio.EliminarAsync(id);
                AvisarExito("País eliminado.");
            }
            catch (ExcepcionAplicacion e)
            {
                AvisarError(e.Message);
            }
            return RedirectToAction(nameof(Index), Vuelta(campo, texto, region, lista));
        }

        // ------------------------------------------------ CU-02 (Actividad 1)

        [HttpGet("busqueda")]
        public async Task<IActionResult> Busqueda(string? consulta)
        {
            BusquedaVistaModelo modelo = new BusquedaVistaModelo { Nombre = consulta };
            if (consulta != null)
            {
                try
                {
                    modelo.Resultado = await servicio.BuscarPorNombreAsync(consulta);
                }
                catch (ExcepcionAplicacion e)
                {
                    modelo.Error = e.Message;
                }
                try
                {
                    // Para indicar en cada resultado si ya está guardado; si SQL Server falla, solo no se indica
                    modelo.CodigosGuardados = (await servicio.BuscarAsync(FiltroPaises.Ninguno))
                        .Select(p => p.Codigo).ToHashSet(StringComparer.OrdinalIgnoreCase);
                }
                catch (ExcepcionAplicacion)
                {
                    modelo.CodigosGuardados = null;
                }
            }
            if (TempData.Peek(BusquedaVistaModelo.ClaveGuardado) is string guardado)
            {
                modelo.CodigoGuardado = guardado;
            }
            return Vista("BusquedaVista", modelo);
        }

        /// <summary>CU-02: guarda un país elegido de los resultados de la búsqueda.</summary>
        [HttpPost("busqueda/guardar")]
        public async Task<IActionResult> GuardarResultado(FormularioPais formulario, string? consulta)
        {
            try
            {
                Pais pais = Convertir(null, formulario);
                ResultadoImportacion resultado = await servicio.GuardarResultadoAsync(pais);
                AvisarExito(resultado.Nuevos > 0
                    ? pais.Nombre + " se guardó en la base de datos."
                    : pais.Nombre + " ya existía: se actualizaron sus datos.");
                TempData[BusquedaVistaModelo.ClaveGuardado] = pais.Codigo;
            }
            catch (ExcepcionAplicacion e)
            {
                AvisarError("No se pudo guardar: " + e.Message);
            }
            return RedirectToAction(nameof(Busqueda), new { consulta });
        }

        // ------------------------------------------------ apoyo

        private async Task<PaisesVistaModelo> CargarAsync(string? campo, string? texto, string? region, OpcionesLista lista)
        {
            FiltroPaises filtro = Filtro(campo, texto, region);
            PaisesVistaModelo modelo = new PaisesVistaModelo { Filtro = filtro, Lista = lista };
            try
            {
                modelo.ConteoRegiones = await servicio.ContarPorRegionAsync();
                modelo.Paises = await servicio.BuscarAsync(filtro);
            }
            catch (ExcepcionAplicacion e)
            {
                modelo.ErrorCarga = e.Message;
            }
            return modelo;
        }

        /// <summary>
        /// Parámetros para volver a la lista después de un POST: la misma búsqueda, región, página y
        /// orden, y opcionalmente el país por resaltar.
        /// </summary>
        private static RouteValueDictionary Vuelta(string? campo, string? texto, string? region, OpcionesLista lista,
            int? resaltar = null)
        {
            Dictionary<string, string?> ruta = new Dictionary<string, string?>();
            if (!string.IsNullOrWhiteSpace(texto))
            {
                ruta["campo"] = campo;
                ruta["texto"] = texto;
            }
            if (!string.IsNullOrWhiteSpace(region))
            {
                ruta["region"] = region;
            }
            // Si hay un país por resaltar, la vista abre la página donde quedó
            lista.AgregarA(ruta, conPagina: resaltar == null);
            if (resaltar != null)
            {
                ruta["resaltar"] = resaltar.ToString();
            }
            return new RouteValueDictionary(ruta);
        }

        /// <summary>Opciones para el selector de regiones.</summary>
        public static IReadOnlyList<Region> Regiones() => RegionExtensiones.Todas;

        internal static FiltroPaises Filtro(string? campo, string? texto, string? region)
        {
            CampoBusqueda campoBusqueda = Enum.TryParse(campo, true, out CampoBusqueda valor)
                && Enum.IsDefined(valor) ? valor : CampoBusqueda.NOMBRE;
            Region? regionElegida = null;
            if (!string.IsNullOrWhiteSpace(region))
            {
                try
                {
                    regionElegida = RegionExtensiones.DesdeTexto(region);
                }
                catch (ExcepcionValidacion)
                {
                    regionElegida = null;
                }
            }
            return new FiltroPaises(campoBusqueda, string.IsNullOrWhiteSpace(texto) ? null : texto.Trim(), regionElegida);
        }

        /// <summary>
        /// Convierte el formulario en un Pais. Reúne los errores de formato de región, población y
        /// área; las demás reglas las valida el servicio.
        /// </summary>
        internal Pais Convertir(int? id, FormularioPais formulario)
        {
            ArgumentNullException.ThrowIfNull(formulario);
            List<string> errores = new List<string>();

            Region? region = null;
            try
            {
                region = RegionExtensiones.DesdeTexto(formulario.Region);
            }
            catch (ExcepcionValidacion e)
            {
                errores.AddRange(e.Errores);
            }

            long poblacion = 0;
            try
            {
                poblacion = ConvertirPoblacion(formulario.Poblacion);
            }
            catch (ExcepcionValidacion e)
            {
                errores.AddRange(e.Errores);
            }

            double area = 0;
            try
            {
                area = ConvertirArea(formulario.Area);
            }
            catch (ExcepcionValidacion e)
            {
                errores.AddRange(e.Errores);
            }

            if (errores.Count > 0)
            {
                throw new ExcepcionValidacion(errores);
            }
            Pais pais = Pais.Nuevo(formulario.Codigo, formulario.Nombre, formulario.Capital, region, poblacion,
                formulario.Moneda, formulario.SubRegion, area);
            return id == null ? pais : pais.ConId(id.Value);
        }

        /// <summary>
        /// Acepta separadores de miles comunes ("18.001.000", "18,001,000", "18 001 000"): la población
        /// siempre es un número entero.
        /// </summary>
        internal static long ConvertirPoblacion(string? texto)
        {
            if (string.IsNullOrWhiteSpace(texto))
            {
                throw new ExcepcionValidacion("La población es obligatoria.");
            }
            string digitos = Separadores.Replace(texto.Trim(), "");
            if (!long.TryParse(digitos, NumberStyles.AllowLeadingSign, CultureInfo.InvariantCulture, out long numero))
            {
                throw new ExcepcionValidacion("La población debe ser un número entero.");
            }
            return numero;
        }

        /// <summary>
        /// Área en km², opcional (vacía = 0). Admite decimales con punto o coma y espacios como
        /// separador de miles ("276 841,5").
        /// </summary>
        internal static double ConvertirArea(string? texto)
        {
            if (string.IsNullOrWhiteSpace(texto))
            {
                return 0;
            }
            string normalizado = Regex.Replace(texto.Trim(), @"\s", "").Replace(',', '.');
            if (!double.TryParse(normalizado, NumberStyles.AllowLeadingSign | NumberStyles.AllowDecimalPoint,
                    CultureInfo.InvariantCulture, out double area) || double.IsInfinity(area))
            {
                throw new ExcepcionValidacion("El área debe ser un número (km², decimales con punto o coma).");
            }
            return area;
        }

        internal static FormularioPais AFormulario(Pais pais)
        {
            return new FormularioPais(pais.Codigo, pais.Nombre, pais.Capital, pais.Region?.Nombre(),
                pais.Poblacion.ToString(CultureInfo.InvariantCulture), pais.Moneda, pais.SubRegion,
                pais.Area == 0 ? "" : pais.Area.ToString("0.##", CultureInfo.InvariantCulture));
        }

        private static IReadOnlyList<string> Errores(ExcepcionAplicacion e)
        {
            return e is ExcepcionValidacion validacion ? validacion.Errores : new[] { e.Message };
        }
    }
}
