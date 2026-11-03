package ec.edu.monster;

import ec.edu.monster.vista.Ventana;
import javax.swing.SwingUtilities;

/**
 * Cliente de escritorio de EurekaBank (RESTful Java, Grupo 03). Misma identidad
 * visual y mismo MVC que el cliente móvil de TG.1.3.
 */
public class Eureka_ClienteEscritorio_REST_GR3 {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ventana().abrirLogin(null, false));
    }
}
