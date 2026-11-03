package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ErrorServicioModelo;
import javax.swing.SwingUtilities;

/**
 * Comportamiento común de los controladores: las llamadas REST se ejecutan
 * fuera del hilo de eventos de Swing y sus resultados vuelven a él. Si la vista
 * ya se cerró, las respuestas que lleguen después se descartan.
 */
public abstract class ControladorBase {

    private volatile boolean activo = true;

    /** La llamada REST no puede ejecutarse en el hilo de eventos: congelaría la ventana. */
    protected void enSegundoPlano(Runnable tarea) {
        Thread hilo = new Thread(tarea, "llamada-rest");
        hilo.setDaemon(true);
        hilo.start();
    }

    protected void enPantalla(Runnable accion) {
        SwingUtilities.invokeLater(() -> {
            if (activo) {
                accion.run();
            }
        });
    }

    /** La vista se cerró: las respuestas que lleguen después se descartan. */
    public void detener() {
        activo = false;
    }

    /** AUTH-003: el token ya no es válido y hay que volver a iniciar sesión. */
    protected static boolean requiereNuevoLogin(Throwable error) {
        return error instanceof ErrorServicioModelo servicio && servicio.requiereNuevoLogin();
    }

    /** Mensaje para el empleado con el código del ERS cuando el servidor lo envía. */
    protected static String mensajeDe(Throwable error) {
        String mensaje = error == null ? null : error.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            mensaje = "No se pudo completar la operación.";
        }
        if (error instanceof ErrorServicioModelo servicio && servicio.getCodigo() != null) {
            return mensaje + " (" + servicio.getCodigo() + ")";
        }
        return mensaje;
    }
}
