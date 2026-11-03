package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ClienteRESTEureka;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Cuando la sesión web termina (por inactividad o al invalidarla) también se
 * invalida el token en el servidor REST (RQF-002). Es el equivalente de
 * MenuControlador.salir() del escritorio al cerrar la ventana.
 */
@WebListener
public class EscuchaSesion implements HttpSessionListener {

    @Override
    public void sessionDestroyed(HttpSessionEvent evento) {
        if (evento.getSession().getAttribute(ControladorBase.ATRIBUTO_CLIENTE) instanceof ClienteRESTEureka clienteRest) {
            try {
                clienteRest.cerrarSesion();
            } catch (RuntimeException ignorada) {
                // El token caduca solo en el servidor de todas formas
            }
        }
    }
}
