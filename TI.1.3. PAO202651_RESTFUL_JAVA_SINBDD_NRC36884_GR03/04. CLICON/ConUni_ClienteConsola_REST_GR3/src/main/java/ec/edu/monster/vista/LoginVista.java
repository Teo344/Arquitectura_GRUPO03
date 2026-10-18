package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.LoginModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.ErrorRESTModelo;
import ec.edu.monster.util.EntradaUtil;

public class LoginVista {

    private final MensajesVista mensajes = new MensajesVista();

    public void iniciar() {
        mensajeBienvenida();
        mensajes.mostrarTitulo(
                "=== SISTEMA DE CONVERSION DE UNIDADES - CLIENTE CONSOLA REST GR3 ===");

        final LoginControlador controlador;
        try {
            controlador = new LoginControlador();
            mensajes.mostrarMensaje("Servidor REST configurado: "
                    + controlador.getEndpointActivo());
        } catch (IllegalArgumentException ex) {
            mensajes.mostrarError(ex.getMessage());
            return;
        }

        while (true) {
            String usuario = EntradaUtil.leerTextoNoVacio("Usuario: ");
            String clave = EntradaUtil.leerTextoNoVacio("Contrasenia: ");
            try {
                SesionModelo sesion = controlador.autenticar(new LoginModelo(usuario, clave));
                mensajes.mostrarMensaje("Inicio de sesion exitoso. Expira (UTC): "
                        + sesion.getExpiraEn());

                boolean volverAlLogin = new ConversionVista(
                        controlador.crearControladorConversion(),
                        controlador.crearControladorCatalogo()).ejecutarMenu();
                controlador.cerrarSesion();
                if (volverAlLogin) {
                    mensajes.mostrarMensaje(
                            "Sesion cerrada. Ingrese sus credenciales para continuar.");
                    continue;
                }
                mensajes.mostrarMensaje("Sesion finalizada.");
                return;
            } catch (ErrorRESTModelo ex) {
                String codigo = ex.getCodigo() == null ? "" : ex.getCodigo() + ": ";
                mensajes.mostrarError(codigo + ex.getMessage());
            } catch (RuntimeException ex) {
                mensajes.mostrarError(ex.getMessage());
            }
        }
    }

    public void mensajeBienvenida() {
        String colorCian = "\u001B[36m";
        String colorReset = "\u001B[0m";
        String mensaje = """
        +------------------------------------------------------------+
        |    ____            _   _       _     ____  ____  _____     |
        |   / ___|___  _ __ | | | |_ __ (_)   / ___||  _ \\|___ /     |
        |  | |   / _ \\| '_ \\| | | | '_ \\| |  | |  _ | |_) | |_ \\     |
        |  | |__| (_) | | | | |_| | | | | |  | |_| ||  _ < ___) |    |
        |   \\____\\___/|_| |_|\\___/|_| |_|_|   \\____||_| \\_\\____/     |
        +------------------------------------------------------------+""";
        System.out.println(colorCian + mensaje + colorReset);
    }
}
