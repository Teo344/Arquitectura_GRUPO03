package ec.edu.monster.vista;

import ec.edu.monster.modelo.excepcion.ExcepcionAplicacion;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;

/**
 * Ejecuta una llamada al controlador fuera del hilo de eventos de Swing (la
 * red y MySQL congelarían la ventana) y devuelve el resultado a ese hilo.
 * <pre>
 * Tarea.de(controlador::importarPaises)
 *      .siempre(() -> boton.setCargando(false))
 *      .alTerminar(resultado -> ...)
 *      .alFallar(mensaje -> ...)
 *      .iniciar();
 * </pre>
 *
 * @param <T> tipo del resultado
 * @author carlo
 */
public final class Tarea<T> {

    private static final Logger LOG = Logger.getLogger(Tarea.class.getName());
    private static final String ERROR_INESPERADO
            = "Ocurrió un error inesperado. Intente nuevamente; si persiste, revise el registro de la aplicación.";

    private final Supplier<T> trabajo;
    private Runnable siempre = () -> {
    };
    private Consumer<T> alTerminar = resultado -> {
    };
    private Consumer<String> alFallar = mensaje -> {
    };

    private Tarea(Supplier<T> trabajo) {
        this.trabajo = Objects.requireNonNull(trabajo);
    }

    public static <T> Tarea<T> de(Supplier<T> trabajo) {
        return new Tarea<>(trabajo);
    }

    /** Para trabajos sin resultado (p. ej. eliminar). */
    public static Tarea<Void> deAccion(Runnable trabajo) {
        return new Tarea<>(() -> {
            trabajo.run();
            return null;
        });
    }

    /** Se ejecuta antes de alTerminar o alFallar (p. ej. quitar el indicador de carga). */
    public Tarea<T> siempre(Runnable accion) {
        this.siempre = accion;
        return this;
    }

    public Tarea<T> alTerminar(Consumer<T> accion) {
        this.alTerminar = accion;
        return this;
    }

    /** Recibe un mensaje listo para mostrar al usuario. */
    public Tarea<T> alFallar(Consumer<String> accion) {
        this.alFallar = accion;
        return this;
    }

    public void iniciar() {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() {
                return trabajo.get();
            }

            @Override
            protected void done() {
                siempre.run();
                try {
                    alTerminar.accept(get());
                } catch (ExecutionException e) {
                    alFallar.accept(mensajeDe(e.getCause()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    alFallar.accept("La operación fue interrumpida.");
                }
            }
        }.execute();
    }

    private static String mensajeDe(Throwable error) {
        if (error instanceof ExcepcionAplicacion conocida) {
            return conocida.getMessage();
        }
        LOG.log(Level.SEVERE, "Error no controlado en la vista", error);
        return ERROR_INESPERADO;
    }
}
