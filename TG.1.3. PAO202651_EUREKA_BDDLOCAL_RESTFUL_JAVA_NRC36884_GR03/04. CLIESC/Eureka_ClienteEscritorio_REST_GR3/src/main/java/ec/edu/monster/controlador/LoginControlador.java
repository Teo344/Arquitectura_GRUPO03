package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ClienteRESTEureka;
import ec.edu.monster.vista.LoginVista;

/** Coordina la vista de acceso y el servicio remoto de autenticación (RQF-001). */
public class LoginControlador extends ControladorBase {

    private final LoginVista vista;
    private ClienteRESTEureka clienteRest;

    public LoginControlador(LoginVista vista) {
        this.vista = vista;
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
                // El cliente REST se crea al primer intento: si falta la configuración, el error se ve en pantalla
                if (clienteRest == null) {
                    clienteRest = new ClienteRESTEureka();
                }
                SesionModelo sesion = clienteRest.iniciarSesion(usuarioLimpio, clave);
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.abrirMenu(clienteRest, sesion);
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
