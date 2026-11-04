package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.AutenticacionRestService;
import ec.edu.monster.vista.MenuActivity;

/**
 * Controlador del menú principal: abre cada sección y cierra la sesión (CU-02).
 * La venta en curso vive aquí, así se conserva al salir de la pantalla de venta.
 */
public class MenuControlador extends ControladorBase {

    private final MenuActivity vista;
    private final AutenticacionRestService autenticacion;
    private final SesionModelo sesion;
    private CarritoModelo carrito = new CarritoModelo();

    public MenuControlador(MenuActivity vista, AutenticacionRestService autenticacion, SesionModelo sesion) {
        this.vista = vista;
        this.autenticacion = autenticacion;
        this.sesion = sesion;
    }

    public void abrirVenta() {
        vista.mostrarVenta(sesion, carrito);
    }

    public void abrirFacturas() {
        vista.mostrarFacturas(sesion);
    }

    public void abrirTelefonos() {
        vista.mostrarTelefonos(sesion);
    }

    public void abrirClientes() {
        vista.mostrarClientes(sesion);
    }

    public void abrirSimulador() {
        vista.mostrarSimulador(sesion);
    }

    /** La pantalla de venta devuelve el carrito como quedó. */
    public void actualizarVenta(CarritoModelo carrito) {
        this.carrito = carrito;
        vista.mostrarUnidades(carrito.getArticulos());
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
                // La sesión local se descarta de todas formas (CU-02)
            }
            enPantalla(() -> vista.volverAlLogin("Sesión cerrada correctamente.", false));
        });
    }
}
