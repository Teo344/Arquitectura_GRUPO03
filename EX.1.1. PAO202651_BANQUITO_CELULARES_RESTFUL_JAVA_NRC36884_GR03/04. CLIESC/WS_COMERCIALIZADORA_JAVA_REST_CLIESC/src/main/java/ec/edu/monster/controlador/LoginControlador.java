package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.LoginVista;

/** Coordina la vista de acceso de los empleados y el inicio de sesión en la API (CU-01). */
public class LoginControlador extends ControladorBase {

    private final LoginVista vista;
    private ClienteRESTMonsterCell clienteRest;

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
                    clienteRest = new ClienteRESTMonsterCell();
                }
                SesionModelo sesion = clienteRest.iniciarSesion(usuarioLimpio, clave);
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.abrirPrincipal(clienteRest, sesion);
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
