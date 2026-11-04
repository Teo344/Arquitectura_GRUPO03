package ec.edu.monster.controlador;

import android.os.Handler;
import android.os.Looper;
import ec.edu.monster.modelo.ErrorServicioModelo;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Comportamiento común de los controladores, el mismo de los móviles de
 * EurekaBank y del escritorio: las llamadas REST se ejecutan fuera del hilo de
 * la interfaz y sus resultados vuelven a él. Si la vista ya se cerró, las
 * respuestas que lleguen después se descartan.
 */
public abstract class ControladorBase {

    /** Lo que todo controlador necesita de su vista para mostrar la espera y los errores. */
    public interface Vista {

        void setProcesando(boolean procesando);

        void mostrarMensaje(String texto, boolean esError);

        /** AUTH-003: el token ya no es válido; la vista vuelve al inicio de sesión con este mensaje. */
        void volverAlLogin(String mensaje);
    }

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

    protected <T> void ejecutar(Vista vista, Supplier<T> llamada, Consumer<T> alResponder) {
        ejecutar(vista, vista::setProcesando, llamada, alResponder);
    }

    /**
     * Llama a la API en segundo plano y entrega la respuesta en pantalla.
     * espera indica qué parte de la vista muestra el indicador de carga
     * (un botón concreto cuando la pantalla tiene varias acciones).
     */
    protected <T> void ejecutar(Vista vista, Consumer<Boolean> espera, Supplier<T> llamada, Consumer<T> alResponder) {
        espera.accept(true);
        enSegundoPlano(() -> {
            try {
                T respuesta = llamada.get();
                enPantalla(() -> {
                    espera.accept(false);
                    alResponder.accept(respuesta);
                });
            } catch (RuntimeException ex) {
                enPantalla(() -> {
                    espera.accept(false);
                    mostrarError(vista, ex);
                });
            }
        });
    }

    protected static void mostrarError(Vista vista, RuntimeException error) {
        if (requiereNuevoLogin(error)) {
            vista.volverAlLogin(mensajeDe(error));
            return;
        }
        vista.mostrarMensaje(mensajeDe(error), true);
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
