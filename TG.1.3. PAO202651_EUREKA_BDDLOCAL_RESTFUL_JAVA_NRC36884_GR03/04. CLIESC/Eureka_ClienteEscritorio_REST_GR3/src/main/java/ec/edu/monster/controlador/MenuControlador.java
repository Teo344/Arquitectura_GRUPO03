package ec.edu.monster.controlador;

import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.servicio.ClienteRESTEureka;
import ec.edu.monster.vista.MenuVista;

/** Controlador del menú principal: abre cada operación y cierra la sesión (RQF-002). */
public class MenuControlador extends ControladorBase {

    private final MenuVista vista;
    private final ClienteRESTEureka clienteRest;

    public MenuControlador(MenuVista vista, ClienteRESTEureka clienteRest) {
        this.vista = vista;
        this.clienteRest = clienteRest;
    }

    public void abrirConsultaCuenta() {
        vista.mostrarCuenta(clienteRest, false);
    }

    public void abrirMovimientos() {
        vista.mostrarCuenta(clienteRest, true);
    }

    public void abrirTransaccion(TipoTransaccion tipo) {
        vista.mostrarTransaccion(clienteRest, tipo);
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
            clienteRest.cerrarSesion();
        } catch (RuntimeException ignorada) {
            // La sesión local se descarta de todas formas (RQF-002)
        }
    }
}
