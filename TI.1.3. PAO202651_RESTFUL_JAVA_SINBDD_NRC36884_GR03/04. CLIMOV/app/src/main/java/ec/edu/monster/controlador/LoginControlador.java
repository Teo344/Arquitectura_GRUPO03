package ec.edu.monster.controlador;

import android.os.Handler;
import android.os.Looper;
import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.servicio.AutenticacionRestService;
import ec.edu.monster.servicio.ExcepcionServicio;
import ec.edu.monster.vista.LoginActivity;

/** Coordina la vista de acceso y el servicio remoto de autenticación. */
public class LoginControlador {

    private final LoginActivity vista;
    private final AutenticacionRestService autenticacion;
    private final Handler hiloPrincipal = new Handler(Looper.getMainLooper());
    private volatile boolean activo = true;

    public LoginControlador(LoginActivity vista, AutenticacionRestService autenticacion) {
        this.vista = vista;
        this.autenticacion = autenticacion;
    }

    public void iniciarSesion(String usuario, String clave) {
        String usuarioLimpio = usuario == null ? "" : usuario.trim();
        if (usuarioLimpio.isEmpty() || clave == null || clave.isEmpty()) {
            vista.mostrarMensaje("Ingresa tu usuario y contraseña.", true);
            return;
        }

        vista.setProcesando(true);
        vista.mostrarMensaje("Conectando con el servidor...", false);

        // La llamada REST no puede ejecutarse en el hilo de la interfaz
        new Thread(() -> {
            try {
                SesionUsuario sesion = autenticacion.iniciarSesion(usuarioLimpio, clave);
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.abrirMenu(sesion);
                });
            } catch (Exception ex) {
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.mostrarMensaje(ExcepcionServicio.mensajeAmigable(ex), true);
                    vista.limpiarClave();
                });
            }
        }).start();
    }

    /** La vista se cerró: las respuestas que lleguen después se descartan. */
    public void detener() {
        activo = false;
        hiloPrincipal.removeCallbacksAndMessages(null);
    }

    private void enPantalla(Runnable accion) {
        hiloPrincipal.post(() -> {
            if (activo) {
                accion.run();
            }
        });
    }
}
