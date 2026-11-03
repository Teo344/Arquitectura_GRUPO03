package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ClienteWSEureka;
import ec.edu.monster.vista.LoginVista;

/** Coordina la vista de acceso y el servicio remoto de autenticación (RQF-001). */
public class LoginControlador extends ControladorBase {

    private final LoginVista vista;
    private ClienteWSEureka clienteWs;

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
                // Los puertos SOAP se crean al primer intento: leer los WSDL también es una llamada de red
                if (clienteWs == null) {
                    clienteWs = new ClienteWSEureka();
                }
                SesionModelo sesion = clienteWs.iniciarSesion(usuarioLimpio, clave);
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.abrirMenu(clienteWs, sesion);
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
