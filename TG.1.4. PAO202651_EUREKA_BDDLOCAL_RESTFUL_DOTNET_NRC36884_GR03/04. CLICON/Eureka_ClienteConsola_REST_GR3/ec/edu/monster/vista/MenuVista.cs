using ec.edu.monster.controlador;
using ec.edu.monster.modelo;
using ec.edu.monster.util;

namespace ec.edu.monster.vista
{
    /// <summary>Menú de operaciones; solo se muestra mientras hay una sesión válida.</summary>
    public class MenuVista
    {
        private readonly MensajesVista mensajes = new MensajesVista();
        private readonly LoginControlador loginControlador;
        private readonly CuentaVista cuentaVista;
        private readonly TransaccionVista transaccionVista;

        public MenuVista(LoginControlador loginControlador)
        {
            this.loginControlador = loginControlador;
            cuentaVista = new CuentaVista(loginControlador.CrearControladorCuenta());
            transaccionVista = new TransaccionVista(loginControlador.CrearControladorTransaccion());
        }

        /// <summary>Devuelve true si el usuario cierra sesión (vuelve al login) y false si sale del programa.</summary>
        public async Task<bool> EjecutarMenuAsync()
        {
            while (true)
            {
                SesionModelo? sesion = loginControlador.Sesion;
                if (sesion == null) return true;

                mensajes.MostrarTitulo("MENU PRINCIPAL  |  " + sesion.NombreEmpleado + "  |  Sucursal " + sesion.Sucursal);
                mensajes.MostrarMensaje("  Consultas");
                mensajes.MostrarMensaje("    1. Consultar cuenta");
                mensajes.MostrarMensaje("    2. Consultar movimientos");
                mensajes.MostrarMensaje("  Transacciones");
                mensajes.MostrarMensaje("    3. Registrar deposito");
                mensajes.MostrarMensaje("    4. Registrar retiro");
                mensajes.MostrarMensaje("    5. Registrar transferencia");
                mensajes.MostrarMensaje("  Sesion");
                mensajes.MostrarMensaje("    6. Cerrar sesion");
                mensajes.MostrarMensaje("    0. Salir");

                int opcion = EntradaUtil.LeerEntero("Seleccione una opcion: ");
                try
                {
                    switch (opcion)
                    {
                        case 1: await cuentaVista.ConsultarCuentaAsync(); break;
                        case 2: await cuentaVista.ConsultarMovimientosAsync(); break;
                        case 3: await transaccionVista.ProcesarAsync(TipoTransaccion.Deposito); break;
                        case 4: await transaccionVista.ProcesarAsync(TipoTransaccion.Retiro); break;
                        case 5: await transaccionVista.ProcesarAsync(TipoTransaccion.Transferencia); break;
                        case 6: return true;
                        case 0: return false;
                        default: mensajes.MostrarError("Opcion no valida. Intente nuevamente."); break;
                    }
                }
                catch (ErrorServicioModelo ex)
                {
                    mensajes.MostrarErrorServicio(ex);
                    if (ex.RequiereNuevoLogin) return true;
                }
                catch (ArgumentException ex)
                {
                    mensajes.MostrarError(ex.Message);
                }
            }
        }
    }
}
