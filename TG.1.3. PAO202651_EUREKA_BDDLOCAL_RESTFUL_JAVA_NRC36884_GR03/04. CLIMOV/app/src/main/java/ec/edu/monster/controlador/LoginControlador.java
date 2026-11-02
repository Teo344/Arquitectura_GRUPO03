package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.AutenticacionRestService;
import ec.edu.monster.vista.LoginActivity;

/** Coordina la vista de acceso y el servicio remoto de autenticación (RQF-001). */
public class LoginControlador extends ControladorBase {

    private final LoginActivity vista;
    private final AutenticacionRestService autenticacion;

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

        enSegundoPlano(() -> {
            try {
                SesionModelo sesion = autenticacion.iniciarSesion(usuarioLimpio, clave);
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.abrirMenu(sesion);
                });
            } catch (RuntimeException ex) {
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.mostrarMensaje(mensajeDe(ex), true);
                    vista.limpiarClave();
                });
            }
        });
    }
}
