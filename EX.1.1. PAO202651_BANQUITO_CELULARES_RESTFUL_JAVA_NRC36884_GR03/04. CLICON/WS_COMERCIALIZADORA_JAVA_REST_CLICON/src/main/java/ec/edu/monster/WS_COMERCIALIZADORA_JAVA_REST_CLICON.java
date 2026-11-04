package ec.edu.monster;

import ec.edu.monster.vista.LoginVista;

/**
 * Cliente de consola de la comercializadora MonsterCell (RESTful Java,
 * Grupo 03). Consume la API WS_COMERCIALIZADORA_JAVA_REST con el mismo MVC de
 * los clientes web y de escritorio.
 *
 * @author revil
 */
public class WS_COMERCIALIZADORA_JAVA_REST_CLICON {

    public static void main(String[] args) {
        new LoginVista().iniciar();
    }
}
