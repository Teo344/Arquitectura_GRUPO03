using ec.edu.monster.modelo.entidad;
using ec.edu.monster.servicio;
using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Controlador de la pantalla de inicio: informa si la base de datos responde y cómo está
    /// configurada la API antes de entrar a trabajar.
    /// </summary>
    [Route("")]
    public class InicioControlador : ControladorBase
    {
        private readonly PaisServicio servicio;

        public InicioControlador(PaisServicio servicio)
        {
            this.servicio = servicio ?? throw new ArgumentNullException(nameof(servicio));
        }

        [HttpGet("")]
        public async Task<IActionResult> Index()
        {
            EstadoSistema estado = await servicio.VerificarEstadoAsync();
            return Vista("InicioVista", estado);
        }
    }
}
