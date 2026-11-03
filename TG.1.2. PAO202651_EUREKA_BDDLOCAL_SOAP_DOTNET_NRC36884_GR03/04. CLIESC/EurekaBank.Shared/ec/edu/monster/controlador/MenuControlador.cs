using System;
using System.Threading.Tasks;
using ec.edu.monster.modelo;
using ec.edu.monster.servicio;
using ec.edu.monster.vista;

namespace ec.edu.monster.controlador
{
    /// <summary>Controlador del menú principal: abre cada operación y cierra la sesión (RQF-002).</summary>
    public sealed class MenuControlador : ControladorBase
    {
        private readonly MenuVista vista;
        private readonly AutenticacionSoapService autenticacion;
        private readonly SesionModelo sesion;

        public MenuControlador(MenuVista vista, AutenticacionSoapService autenticacion, SesionModelo sesion)
        {
            this.vista = vista;
            this.autenticacion = autenticacion;
            this.sesion = sesion;
        }

        public void AbrirConsultaCuenta()
        {
            vista.MostrarCuenta(false);
        }

        public void AbrirMovimientos()
        {
            vista.MostrarCuenta(true);
        }

        public void AbrirTransaccion(TipoTransaccion tipo)
        {
            vista.MostrarTransaccion(tipo);
        }

        /// <summary>
        /// Invalida el token en el servidor. Aunque el servidor no responda o el token ya haya
        /// caducado, la sesión local termina y se vuelve al login.
        /// </summary>
        public async Task CerrarSesionAsync()
        {
            vista.SetProcesando(true);
            try
            {
                await autenticacion.CerrarSesionAsync(sesion.Token);
            }
            catch (Exception)
            {
                // La sesión local se descarta de todas formas (RQF-002)
            }
            vista.VolverAlLogin("Sesión cerrada correctamente.", false);
        }
    }
}
