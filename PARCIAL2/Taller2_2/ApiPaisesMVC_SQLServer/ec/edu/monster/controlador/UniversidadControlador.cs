using ec.edu.monster.modelo.entidad;
using ec.edu.monster.modelo.excepcion;
using ec.edu.monster.servicio;
using ec.edu.monster.vista.modelo;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Controlador de universidades (actividad avanzada, CU-09 y CU-10): importa desde la API de
    /// Hipolabs y muestra lo guardado en SQL Server.
    /// </summary>
    public class UniversidadControlador : ControladorBase
    {
        private const string PaisPorDefecto = "Ecuador";

        private readonly UniversidadServicio servicio;

        public UniversidadControlador(UniversidadServicio servicio)
        {
            this.servicio = servicio ?? throw new ArgumentNullException(nameof(servicio));
        }

        /// <summary>CU-10: universidades guardadas, filtradas por país.</summary>
        [HttpGet("universidades")]
        public async Task<IActionResult> Index(string? pais)
        {
            UniversidadesVistaModelo modelo = new UniversidadesVistaModelo
            {
                PaisFiltro = string.IsNullOrWhiteSpace(pais) ? null : pais.Trim(),
                PaisImportar = string.IsNullOrWhiteSpace(pais) ? PaisPorDefecto : pais.Trim()
            };
            try
            {
                modelo.ConteoPorPais = await servicio.ContarPorPaisAsync();
                modelo.Universidades = await servicio.ListarAsync(modelo.PaisFiltro);
            }
            catch (ExcepcionAplicacion e)
            {
                modelo.ErrorCarga = e.Message;
            }
            return Vista("UniversidadesVista", modelo);
        }

        /// <summary>CU-09: importa las universidades de un país.</summary>
        [HttpPost("universidades/importar")]
        public async Task<IActionResult> Importar(string? pais)
        {
            try
            {
                ResultadoImportacion resultado = await servicio.ImportarPorPaisAsync(pais);
                if (resultado.Recibidos == 0)
                {
                    AvisarAdvertencia("La API no tiene universidades para «" + pais?.Trim()
                        + "». Escribe el país en inglés, por ejemplo «Ecuador», «Peru» o «Spain».");
                }
                else
                {
                    AvisarExito("Importación completa: " + vista.Formato.Entero(resultado.Recibidos) + " recibidas · "
                        + vista.Formato.Entero(resultado.Nuevos) + " nuevas · "
                        + vista.Formato.Entero(resultado.Actualizados) + " actualizadas.");
                }
            }
            catch (ExcepcionAplicacion e)
            {
                AvisarError("No se pudo importar: " + e.Message);
            }
            return RedirectToAction(nameof(Index), new { pais = pais?.Trim() });
        }
    }
}
