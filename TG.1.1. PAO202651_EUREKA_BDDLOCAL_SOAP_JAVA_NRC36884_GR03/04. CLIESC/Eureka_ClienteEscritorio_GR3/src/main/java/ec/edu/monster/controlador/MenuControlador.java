package ec.edu.monster.controlador;

import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.servicio.ClienteWSEureka;
import ec.edu.monster.vista.MenuVista;

/** Controlador del menú principal: abre cada operación y cierra la sesión (RQF-002). */
public class MenuControlador extends ControladorBase {

    private final MenuVista vista;
    private final ClienteWSEureka clienteWs;

    public MenuControlador(MenuVista vista, ClienteWSEureka clienteWs) {
        this.vista = vista;
        this.clienteWs = clienteWs;
    }

    public void abrirConsultaCuenta() {
        vista.mostrarCuenta(clienteWs, false);
    }

    public void abrirMovimientos() {
        vista.mostrarCuenta(clienteWs, true);
    }

    public void abrirTransaccion(TipoTransaccion tipo) {
        vista.mostrarTransaccion(clienteWs, tipo);
    }

    /**
     * Invalida el token en el servidor. Aunque el servidor no responda o el
     * token ya haya caducado, la sesión local termina y se vuelve al login.
     */
    public void cerrarSesion() {
        vista.setProcesando(true);
        enSegundoPlano(() -> {
            invalidarToken();
            enPantalla(() -> vista.volverAlLogin("Sesión cerrada correctamente.", false));
        });
    }

    /** Al cerrar la ventana con la sesión abierta también se invalida el token. */
    public void salir() {
        vista.setProcesando(true);
        enSegundoPlano(() -> {
            invalidarToken();
            enPantalla(vista::cerrarAplicacion);
        });
    }

    private void invalidarToken() {
        try {
            clienteWs.cerrarSesion();
        } catch (RuntimeException ignorada) {
            // La sesión local se descarta de todas formas (RQF-002)
        }
    }
}
