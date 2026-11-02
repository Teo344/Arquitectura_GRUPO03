package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.LoginModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.util.EntradaUtil;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Pantalla de inicio de sesión (RQF-001) y ciclo login → menú → cierre de sesión. */
public class LoginVista {

    private static final DateTimeFormatter FORMATO_HORA
            = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private final MensajesVista mensajes = new MensajesVista();

    public void iniciar() {
        mensajeBienvenida();

        final LoginControlador controlador;
        try {
            controlador = new LoginControlador();
        } catch (ErrorServicioModelo | IllegalStateException | IllegalArgumentException ex) {
            mensajes.mostrarError(ex.getMessage());
            mensajes.mostrarMensaje("Revise servidor.properties o ejecute con "
                    + "-Dservidor.url=http://IP:8080/WS_EUREKA_JAVA_REST/api");
            return;
        }
        mensajes.mostrarMensaje("  Servidor REST: " + controlador.getEndpointActivo());

        while (true) {
            mensajes.mostrarTitulo("INICIO DE SESION");
            String usuario = EntradaUtil.leerTextoNoVacio("  Usuario : ");
            String clave = EntradaUtil.leerClave("  Clave   : ");

            try {
                SesionModelo sesion = controlador.autenticar(new LoginModelo(usuario, clave));
                mostrarSesion(sesion);

                boolean volverAlLogin = new MenuVista(controlador).ejecutarMenu();
                cerrarSesion(controlador);
                if (volverAlLogin) {
                    mensajes.mostrarMensaje("Sesion cerrada. Ingrese sus credenciales para continuar.");
                    continue;
                }
                mensajes.mostrarMensaje("Sesion finalizada. Gracias por usar EurekaBank.");
                return;
            } catch (ErrorServicioModelo ex) {
                mensajes.mostrarErrorServicio(ex);
            } catch (IllegalArgumentException ex) {
                mensajes.mostrarError(ex.getMessage());
            }
        }
    }

    // RQF-002: si el servidor no responde, la sesión local ya quedó eliminada
    private void cerrarSesion(LoginControlador controlador) {
        try {
            controlador.cerrarSesion();
        } catch (ErrorServicioModelo ex) {
            mensajes.mostrarErrorServicio(ex);
        }
    }

    private void mostrarSesion(SesionModelo sesion) {
        System.out.println();
        mensajes.mostrarExito("Inicio de sesion exitoso.");
        mensajes.mostrarRecuadro(List.of(
                "Bienvenido(a), " + sesion.getNombreEmpleado(),
                "Empleado : " + sesion.getCodigoEmpleado(),
                "Sucursal : " + sesion.getCodigoSucursal() + " - " + sesion.getSucursal(),
                "Sesion activa hasta las " + FORMATO_HORA.format(sesion.getExpiraEn())
                + " (se renueva con cada operacion)"));
    }

    public void mensajeBienvenida() {
        String colorCian = "\u001B[36m";
        String colorReset = "\u001B[0m";
        String mensaje = """
        +------------------------------------------------------------+
        |                                                            |
        |   _____   _   _   ____    _____   _  __     _              |
        |  | ____| | | | | |  _ \\  | ____| | |/ /    / \\             |
        |  |  _|   | | | | | |_) | |  _|   | ' /    / _ \\            |
        |  | |___  | |_| | |  _ <  | |___  | . \\   / ___ \\           |
        |  |_____|  \\___/  |_| \\_\\ |_____| |_|\\_\\ /_/   \\_\\  BANK    |
        |                                                            |
        |        Cliente Consola REST  -  Grupo 03 (NRC 36884)       |
        +------------------------------------------------------------+""";
        System.out.println(colorCian + mensaje + colorReset);
    }
}
