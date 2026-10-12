package ec.edu.monster.controlador;

import ec.edu.monster.vista.MenuView;
import java.awt.event.ActionEvent;

/** Controlador del menú principal; conecta las opciones a las vistas correspondientes. */
public class MenuController {
    private final MenuView vista;

    public MenuController(MenuView vista) {
        this.vista = vista;
        vista.getBotonConvertir().addActionListener(this::abrirConversion);
        vista.getBotonCatalogo().addActionListener(this::abrirCatalogo);
        vista.getBotonCerrarSesion().addActionListener(this::cerrarSesion);
    }

    private void abrirConversion(ActionEvent evento) {
        ec.edu.monster.vista.ConversionView conversion = new ec.edu.monster.vista.ConversionView(vista.getSesion());
        new ConversionController(conversion, new ec.edu.monster.servicio.ConversionSoapService());
        vista.dispose();
        conversion.setVisible(true);
    }

    private void abrirCatalogo(ActionEvent evento) {
        ec.edu.monster.vista.CatalogoView catalogo = new ec.edu.monster.vista.CatalogoView(vista.getSesion());
        new CatalogoController(catalogo, new ec.edu.monster.servicio.ConversionSoapService());
        vista.dispose();
        catalogo.setVisible(true);
    }

    private void cerrarSesion(ActionEvent evento) {
        ec.edu.monster.vista.LoginView login = new ec.edu.monster.vista.LoginView();
        new LoginController(login, new ec.edu.monster.servicio.AutenticacionSoapService());
        vista.dispose();
        login.setVisible(true);
    }
}
