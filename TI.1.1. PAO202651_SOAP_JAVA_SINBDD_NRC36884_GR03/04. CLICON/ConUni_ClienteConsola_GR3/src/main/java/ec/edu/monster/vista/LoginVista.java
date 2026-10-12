package ec.edu.monster.vista;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.controlador.ConversionControlador;
import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.LoginModelo;
import ec.edu.monster.modelo.MensajesModelo;
import ec.edu.monster.util.EntradaUtil;
import jakarta.xml.ws.soap.SOAPFaultException;

public class LoginVista {

    private final MensajesModelo mensajes = new MensajesModelo();
    private LoginControlador loginControlador;

    public void iniciar() {
        this.mensajeBienvenida();
        mensajes.mostrarTitulo("=== SISTEMA DE CONVERSION DE UNIDADES - CLIENTE CONSOLA GR3 ===");

        try {
            this.loginControlador = new LoginControlador();
            mensajes.mostrarMensaje("Servidor SOAP detectado: " + loginControlador.getEndpointActivo());
        } catch (Exception ex) {
            mensajes.mostrarError(ex.getMessage());
            mensajes.mostrarMensaje(
                    "Si el contexto es distinto, pruebe ejecutar con -Dws.base.url=http://localhost:8080/TU_CONTEXTO"
            );
            return;
        }

        while (true) {
            String usuario = EntradaUtil.leerTextoNoVacio("Usuario: ");
            String contrasenia = EntradaUtil.leerTextoNoVacio("Contrasenia: ");

            LoginModelo loginModelo = new LoginModelo(usuario, contrasenia);

            try {
                String token = loginControlador.autenticar(loginModelo);
                mensajes.mostrarMensaje("Inicio de sesion exitoso. Token: " + token);

                ConversionControlador controladorConversion = loginControlador.crearControladorConversion();
                CatalogoControlador controladorCatalogo = loginControlador.crearControladorCatalogo();
                boolean cerrarSesion = new ConversionVista(controladorConversion, controladorCatalogo).ejecutarMenu();

                loginControlador.cerrarSesion();
                if (cerrarSesion) {
                    mensajes.mostrarMensaje("Sesion cerrada. Ingrese sus credenciales para continuar.");
                    continue;
                }
                mensajes.mostrarMensaje("Sesion finalizada.");
                return;
            } catch (SOAPFaultException ex) {
                mensajes.mostrarError(ex.getFault().getFaultString());
            } catch (Exception ex) {
                mensajes.mostrarError(ex.getMessage());
            }
        }
    }
    public void mensajeBienvenida() {
        // Códigos ANSI para darle color en la consola
        String COLOR_CIAN = "\u001B[36m";
        String COLOR_RESET = "\u001B[0m";

        // Bloque de texto (Java 15+) con las barras invertidas ya escapadas (\\)
        String mensaje = """
        ╔────────────────────────────────────────────────────────────╗
        │    ____            _   _       _     ____  ____  _____     │
        │   / ___|___  _ __ | | | |_ __ (_)   / ___||  _ \\|___ /     │
        │  | |   / _ \\| '_ \\| | | | '_ \\| |  | |  _ | |_) | |_ \\     │
        │  | |__| (_) | | | | |_| | | | | |  | |_| ||  _ < ___) |    │
        │   \\____\\___/|_| |_|\\___/|_| |_|_|   \\____||_| \\_\\____/     │
        ╚────────────────────────────────────────────────────────────╝""";

        // Imprimir con el color aplicado y luego restaurar al color normal
        System.out.println(COLOR_CIAN + mensaje + COLOR_RESET);
    }
}
