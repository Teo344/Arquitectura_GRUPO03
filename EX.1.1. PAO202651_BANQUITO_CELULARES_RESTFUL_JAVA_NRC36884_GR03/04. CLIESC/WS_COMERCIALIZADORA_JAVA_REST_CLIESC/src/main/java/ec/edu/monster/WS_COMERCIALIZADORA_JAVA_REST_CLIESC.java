package ec.edu.monster;

import ec.edu.monster.vista.Ventana;
import javax.swing.SwingUtilities;

/**
 * Cliente de escritorio de la comercializadora MonsterCell (RESTful Java,
 * Grupo 03). Consume la API WS_COMERCIALIZADORA_JAVA_REST con el mismo MVC y
 * la misma identidad visual del cliente web.
 */
public class WS_COMERCIALIZADORA_JAVA_REST_CLIESC {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ventana().abrirLogin(null, false));
    }
}
