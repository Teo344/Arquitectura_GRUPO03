package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.servicio.AutenticacionSoapService;
import ec.edu.monster.vista.LoginView;
import ec.edu.monster.vista.MenuView;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.SwingWorker;

/** Coordina la vista de acceso y el servicio remoto de autenticación. */
public class LoginController {
    private final LoginView vista;
    private final AutenticacionSoapService autenticacion;

    public LoginController(LoginView vista, AutenticacionSoapService autenticacion) {
        this.vista = vista;
        this.autenticacion = autenticacion;
        vista.getBotonIngresar().addActionListener(this::iniciarSesion);
        vista.getCampoClave().addActionListener(this::iniciarSesion);
    }

    private void iniciarSesion(ActionEvent evento) {
        String usuario = vista.getCampoUsuario().getText().trim();
        char[] caracteresClave = vista.getCampoClave().getPassword();
        if (usuario.isEmpty() || caracteresClave.length == 0) {
            vista.mostrarMensaje("Ingresa tu usuario y contraseña.", true);
            Arrays.fill(caracteresClave, '\0');
            return;
        }

        String clave = new String(caracteresClave);
        Arrays.fill(caracteresClave, '\0');
        vista.setProcesando(true);
        vista.mostrarMensaje("Conectando con el servidor...", false);

        new SwingWorker<SesionUsuario, Void>() {
            @Override protected SesionUsuario doInBackground() throws Exception {
                return autenticacion.iniciarSesion(usuario, clave);
            }

            @Override protected void done() {
                vista.setProcesando(false);
                try {
                    SesionUsuario sesion = get();
                    MenuView menu = new MenuView(sesion);
                    new MenuController(menu);
                    vista.dispose();
                    menu.setVisible(true);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    vista.mostrarMensaje(mensajeAmigable(causa), true);
                    vista.getCampoClave().setText("");
                }
            }
        }.execute();
    }

    private String mensajeAmigable(Throwable error) {
        if (error instanceof ec.edu.monster.servicio.ExcepcionServicio soapError) {
            ec.edu.monster.servicio.ErrorServicio detalle = soapError.getFaultInfo();
            if (detalle != null && detalle.getMensaje() != null && !detalle.getMensaje().isBlank()) {
                return detalle.getMensaje();
            }
            return soapError.getMessage() == null ? "No se pudo autenticar." : soapError.getMessage();
        }
        if (error instanceof java.net.ConnectException
                || (error.getCause() instanceof java.net.ConnectException)) {
            return "No se pudo conectar con el servidor. Verifica que esté iniciado.";
        }
        String mensaje = error.getMessage();
        if (mensaje != null && (mensaje.contains("Connection refused") || mensaje.contains("ConnectException"))) {
            return "No se pudo conectar con el servidor. Verifica que esté iniciado.";
        }
        return mensaje == null || mensaje.isBlank() ? "Ocurrió un error al iniciar sesión." : mensaje;
    }
}
