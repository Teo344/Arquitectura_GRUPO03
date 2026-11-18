using ec.edu.monster.modelo.excepcion;
using ec.edu.monster.servicio;
using ec.edu.monster.vista.modelo;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Controlador de reportes (CU-08 / Actividad 5). Separado de PaisControlador para que cada
    /// pantalla dependa solo de lo que usa (ISP); el formato del reporte (tabla, gráfico) lo decide
    /// la vista.
    /// </summary>
    public class ReporteControlador : ControladorBase
    {
        private readonly PaisServicio servicio;

        public ReporteControlador(PaisServicio servicio)
        {
            this.servicio = servicio ?? throw new ArgumentNullException(nameof(servicio));
        }

        [HttpGet("reportes")]
        public async Task<IActionResult> Index()
        {
            ReportesVistaModelo modelo = new ReportesVistaModelo();
            try
            {
                modelo.Estadisticas = await servicio.GenerarEstadisticasAsync();
            }
            catch (ExcepcionAplicacion e)
            {
                modelo.Error = e.Message;
            }
            return Vista("ReportesVista", modelo);
        }
    }
}
