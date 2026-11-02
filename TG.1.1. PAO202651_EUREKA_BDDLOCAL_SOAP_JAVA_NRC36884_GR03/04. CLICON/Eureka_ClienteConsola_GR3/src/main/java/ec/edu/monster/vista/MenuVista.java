package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.util.EntradaUtil;

/** Menú de operaciones; solo se muestra mientras hay una sesión válida. */
public class MenuVista {

    private final MensajesVista mensajes = new MensajesVista();
    private final LoginControlador loginControlador;
    private final CuentaVista cuentaVista;
    private final TransaccionVista transaccionVista;

    public MenuVista(LoginControlador loginControlador) {
        this.loginControlador = loginControlador;
        this.cuentaVista = new CuentaVista(loginControlador.crearControladorCuenta());
        this.transaccionVista = new TransaccionVista(loginControlador.crearControladorTransaccion());
    }

    // Devuelve true si el usuario cierra sesión (vuelve al login) y false si sale del programa
    public boolean ejecutarMenu() {
        while (true) {
            SesionModelo sesion = loginControlador.getSesion();
            if (sesion == null) {
                return true;
            }
            mensajes.mostrarTitulo("MENU PRINCIPAL  |  " + sesion.getNombreEmpleado()
                    + "  |  Sucursal " + sesion.getSucursal());
            mensajes.mostrarMensaje("  Consultas");
            mensajes.mostrarMensaje("    1. Consultar cuenta");
            mensajes.mostrarMensaje("    2. Consultar movimientos");
            mensajes.mostrarMensaje("  Transacciones");
            mensajes.mostrarMensaje("    3. Registrar deposito");
            mensajes.mostrarMensaje("    4. Registrar retiro");
            mensajes.mostrarMensaje("    5. Registrar transferencia");
            mensajes.mostrarMensaje("  Sesion");
            mensajes.mostrarMensaje("    6. Cerrar sesion");
            mensajes.mostrarMensaje("    0. Salir");

            int opcion = EntradaUtil.leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1 -> cuentaVista.consultarCuenta();
                    case 2 -> cuentaVista.consultarMovimientos();
                    case 3 -> transaccionVista.procesar(TipoTransaccion.DEPOSITO);
                    case 4 -> transaccionVista.procesar(TipoTransaccion.RETIRO);
                    case 5 -> transaccionVista.procesar(TipoTransaccion.TRANSFERENCIA);
                    case 6 -> {
                        return true;
                    }
                    case 0 -> {
                        return false;
                    }
                    default -> mensajes.mostrarError("Opcion no valida. Intente nuevamente.");
                }
            } catch (ErrorServicioModelo ex) {
                mensajes.mostrarErrorServicio(ex);
                if (ex.requiereNuevoLogin()) {
                    return true;
                }
            } catch (IllegalArgumentException ex) {
                mensajes.mostrarError(ex.getMessage());
            }
        }
    }
}
