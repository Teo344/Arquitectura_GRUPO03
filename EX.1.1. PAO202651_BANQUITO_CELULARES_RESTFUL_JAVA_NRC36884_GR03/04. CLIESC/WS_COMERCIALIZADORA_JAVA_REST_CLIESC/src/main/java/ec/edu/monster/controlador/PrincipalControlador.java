package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.PrincipalVista;
import ec.edu.monster.vista.PrincipalVista.Seccion;

/**
 * Controlador de la ventana principal: la barra lateral abre cada sección y
 * cierra la sesión (CU-02), como MenuControlador en el cliente web.
 */
public class PrincipalControlador extends ControladorBase {

    private final PrincipalVista vista;
    private final ClienteRESTMonsterCell clienteRest;

    public PrincipalControlador(PrincipalVista vista, ClienteRESTMonsterCell clienteRest) {
        this.vista = vista;
        this.clienteRest = clienteRest;
    }

    public void abrir(Seccion seccion) {
        vista.mostrarSeccion(seccion);
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
            // La sesión local se descarta de todas formas (CU-02)
        }
    }
}
