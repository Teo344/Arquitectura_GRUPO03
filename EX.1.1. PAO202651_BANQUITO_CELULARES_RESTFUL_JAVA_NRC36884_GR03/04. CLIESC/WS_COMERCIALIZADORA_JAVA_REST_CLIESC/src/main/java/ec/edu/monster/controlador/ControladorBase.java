package ec.edu.monster.controlador;

import ec.edu.monster.modelo.ErrorServicioModelo;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.SwingUtilities;

/**
 * Comportamiento común de los controladores, el mismo de los clientes de
 * escritorio de EurekaBank: las llamadas REST se ejecutan fuera del hilo de
 * eventos de Swing y sus resultados vuelven a él. Si la vista ya se cerró, las
 * respuestas que lleguen después se descartan. Los mensajes llevan el código de
 * error de la API, como en el cliente web.
 */
public abstract class ControladorBase {

    /** Lo que todo controlador necesita de su vista para mostrar la espera y los errores. */
    public interface Vista {

        void setProcesando(boolean procesando);

        void mostrarMensaje(String texto, boolean esError);

        /** AUTH-003: el token ya no es válido; la vista vuelve al inicio de sesión con este mensaje. */
        void volverAlLogin(String mensaje);
    }

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
