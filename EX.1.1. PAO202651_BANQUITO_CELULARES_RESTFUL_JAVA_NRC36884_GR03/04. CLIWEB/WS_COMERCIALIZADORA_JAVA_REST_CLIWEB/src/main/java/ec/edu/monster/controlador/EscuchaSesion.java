package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Cuando la sesión web termina (por inactividad o al invalidarla) también se
 * invalida el token en la API REST de la comercializadora, para que no quede
 * abierto en el servidor.
 */
@WebListener
public class EscuchaSesion implements HttpSessionListener {

    @Override
    public void sessionDestroyed(HttpSessionEvent evento) {
        if (evento.getSession().getAttribute(ControladorBase.ATRIBUTO_CLIENTE) instanceof ClienteRESTMonsterCell clienteRest) {
            try {
                clienteRest.cerrarSesion();
            } catch (RuntimeException ignorada) {
                // El token caduca solo en el servidor de todas formas
            }
        }
    }
}
