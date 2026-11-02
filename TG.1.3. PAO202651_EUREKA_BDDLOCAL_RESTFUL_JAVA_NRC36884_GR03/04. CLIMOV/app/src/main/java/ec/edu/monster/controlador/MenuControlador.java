package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.servicio.AutenticacionRestService;
import ec.edu.monster.vista.MenuActivity;

/** Controlador del menú principal: abre cada operación y cierra la sesión (RQF-002). */
public class MenuControlador extends ControladorBase {

    private final MenuActivity vista;
    private final AutenticacionRestService autenticacion;
    private final SesionModelo sesion;

    public MenuControlador(MenuActivity vista, AutenticacionRestService autenticacion, SesionModelo sesion) {
        this.vista = vista;
        this.autenticacion = autenticacion;
        this.sesion = sesion;
    }

    public void abrirConsultaCuenta() {
        vista.mostrarCuenta(sesion, false);
    }

    public void abrirMovimientos() {
        vista.mostrarCuenta(sesion, true);
    }

    public void abrirTransaccion(TipoTransaccion tipo) {
        vista.mostrarTransaccion(sesion, tipo);
    }

    /**
     * Invalida el token en el servidor. Aunque el servidor no responda o el
     * token ya haya caducado, la sesión local termina y se vuelve al login.
     */
    public void cerrarSesion() {
        vista.setProcesando(true);
        enSegundoPlano(() -> {
            try {
                autenticacion.cerrarSesion(sesion.getToken());
            } catch (RuntimeException ignorada) {
                // La sesión local se descarta de todas formas (RQF-002)
            }
            enPantalla(() -> vista.volverAlLogin("Sesión cerrada correctamente.", false));
        });
    }
}
