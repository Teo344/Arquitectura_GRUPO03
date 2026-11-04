package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.util.EntradaUtil;

/** Menú principal, con las secciones de los clientes web y de escritorio; solo se muestra con una sesión válida. */
public class MenuVista {

    private final MensajesVista mensajes = new MensajesVista();
    private final LoginControlador loginControlador;
    private final VentaVista ventaVista;
    private final FacturasVista facturasVista;
    private final TelefonosVista telefonosVista;
    private final ClientesVista clientesVista;
    private final SimuladorVista simuladorVista;

    public MenuVista(LoginControlador loginControlador) {
        this.loginControlador = loginControlador;
        this.ventaVista = new VentaVista(loginControlador.crearControladorVenta());
        this.facturasVista = new FacturasVista(loginControlador.crearControladorFactura());
        this.telefonosVista = new TelefonosVista(loginControlador.crearControladorTelefono());
        this.clientesVista = new ClientesVista(loginControlador.crearControladorCredito());
        this.simuladorVista = new SimuladorVista(loginControlador.crearControladorSimulador());
    }

    // Devuelve true si el usuario cierra sesión (vuelve al login) y false si sale del programa
    public boolean ejecutarMenu() {
        while (true) {
            SesionModelo sesion = loginControlador.getSesion();
            if (sesion == null) {
                return true;
            }
            mensajes.mostrarTitulo("MENU PRINCIPAL  |  " + Formato.valorO(sesion.getNombre())
                    + "  |  Usuario " + Formato.valorO(sesion.getUsuario()));
            mensajes.mostrarMensaje("  Ventas");
            mensajes.mostrarMensaje("    1. Nueva venta" + ventaVista.textoVentaEnCurso());
            mensajes.mostrarMensaje("    2. Facturas");
            mensajes.mostrarMensaje("  Catalogo");
            mensajes.mostrarMensaje("    3. Telefonos");
            mensajes.mostrarMensaje("  Credito BanQuito");
            mensajes.mostrarMensaje("    4. Consultar cliente");
            mensajes.mostrarMensaje("    5. Simulador de cuotas");
            mensajes.mostrarMensaje("  Sesion");
            mensajes.mostrarMensaje("    6. Cerrar sesion");
            mensajes.mostrarMensaje("    0. Salir");

            int opcion = EntradaUtil.leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1 -> ventaVista.ejecutar();
                    case 2 -> facturasVista.ejecutar();
                    case 3 -> telefonosVista.ejecutar();
                    case 4 -> clientesVista.consultar();
                    case 5 -> simuladorVista.simular();
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
