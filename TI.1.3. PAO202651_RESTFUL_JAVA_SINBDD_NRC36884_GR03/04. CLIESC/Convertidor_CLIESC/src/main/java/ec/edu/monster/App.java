package ec.edu.monster;

import ec.edu.monster.controlador.LoginController;
import ec.edu.monster.servicio.ApiRest;
import ec.edu.monster.vista.LoginView;
import ec.edu.monster.vista.MenuView;
import ec.edu.monster.controlador.MenuController;
import javax.swing.SwingUtilities;

/** Punto de entrada del cliente de escritorio MVC. */
public final class App {
    private App() {}
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ApiRest api = new ApiRest(System.getProperty("api.base",
                    "http://localhost:8080/WS_CON_UNI_JAVA_REST/api"));
            LoginView login = new LoginView();
            new LoginController(login, api, sesion -> {
                MenuView menu = new MenuView(sesion);
                new MenuController(menu, api);
                menu.setVisible(true);
            });
            login.setVisible(true);
        });
    }
}
