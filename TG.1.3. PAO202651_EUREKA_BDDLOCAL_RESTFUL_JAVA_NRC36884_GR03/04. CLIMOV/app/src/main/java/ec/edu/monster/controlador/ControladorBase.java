package ec.edu.monster.controlador;

import android.os.Handler;
import android.os.Looper;
import ec.edu.monster.modelo.ErrorServicioModelo;

/**
 * Comportamiento común de los controladores: las llamadas REST se ejecutan
 * fuera del hilo de la interfaz y sus resultados vuelven a él. Si la vista ya
 * se cerró, las respuestas que lleguen después se descartan.
 */
public abstract class ControladorBase {

    private final Handler hiloPrincipal = new Handler(Looper.getMainLooper());
    private volatile boolean activo = true;

    /** La llamada REST no puede ejecutarse en el hilo de la interfaz. */
    protected void enSegundoPlano(Runnable tarea) {
        new Thread(tarea).start();
    }

    protected void enPantalla(Runnable accion) {
        hiloPrincipal.post(() -> {
            if (activo) {
                accion.run();
            }
        });
    }

    /** La vista se cerró: las respuestas que lleguen después se descartan. */
    public void detener() {
        activo = false;
        hiloPrincipal.removeCallbacksAndMessages(null);
    }

    /** AUTH-003: el token ya no es válido y hay que volver a iniciar sesión. */
    protected static boolean requiereNuevoLogin(Throwable error) {
        return error instanceof ErrorServicioModelo && ((ErrorServicioModelo) error).requiereNuevoLogin();
    }

    /** Mensaje para el empleado con el código del ERS cuando el servidor lo envía. */
    protected static String mensajeDe(Throwable error) {
        String mensaje = error == null ? null : error.getMessage();
        if (mensaje == null || mensaje.trim().isEmpty()) {
            mensaje = "No se pudo completar la operación.";
        }
        if (error instanceof ErrorServicioModelo && ((ErrorServicioModelo) error).getCodigo() != null) {
            return mensaje + " (" + ((ErrorServicioModelo) error).getCodigo() + ")";
        }
        return mensaje;
    }
}
