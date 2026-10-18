package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.vista.MenuActivity;

/** Controlador del menú principal; conecta las opciones a las vistas correspondientes. */
public class MenuControlador {

    private final MenuActivity vista;
    private final SesionUsuario sesion;

    public MenuControlador(MenuActivity vista, SesionUsuario sesion) {
        this.vista = vista;
        this.sesion = sesion;
    }

    public void abrirConversion() {
        vista.mostrarConversion(sesion);
    }

    public void abrirCatalogo() {
        vista.mostrarCatalogo(sesion);
    }

    public void cerrarSesion() {
        vista.volverAlLogin();
    }
}
