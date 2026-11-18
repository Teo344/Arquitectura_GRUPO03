using Microsoft.AspNetCore.Mvc;

namespace ec.edu.monster.controlador
{
    /// <summary>
    /// Base de los controladores: ubica las vistas en ec/edu/monster/vista y guarda los avisos que
    /// sobreviven a la redirección después de un POST (patrón Post-Redirect-Get).
    /// </summary>
    public abstract class ControladorBase : Controller
    {
        /// <summary>Carpeta de las vistas Razor (equivale al paquete vista del Taller 2.1).</summary>
        protected const string CarpetaVistas = "~/ec/edu/monster/vista/";

        public const string ClaveExito = "aviso-exito";
        public const string ClaveError = "aviso-error";
        public const string ClaveAdvertencia = "aviso-advertencia";

        protected ViewResult Vista(string nombre, object? modelo)
        {
            return View(CarpetaVistas + nombre + ".cshtml", modelo);
        }

        protected void AvisarExito(string mensaje)
        {
            TempData[ClaveExito] = mensaje;
        }

        protected void AvisarError(string mensaje)
        {
            TempData[ClaveError] = mensaje;
        }

        protected void AvisarAdvertencia(string mensaje)
        {
            TempData[ClaveAdvertencia] = mensaje;
        }
    }
}
