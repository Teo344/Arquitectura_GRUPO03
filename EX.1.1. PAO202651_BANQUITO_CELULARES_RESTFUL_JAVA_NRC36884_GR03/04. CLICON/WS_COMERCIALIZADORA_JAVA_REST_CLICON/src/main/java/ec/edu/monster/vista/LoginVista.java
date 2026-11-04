package ec.edu.monster.vista;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.LoginModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.util.EntradaUtil;
import java.util.List;

/** Pantalla de inicio de sesión (CU-01) y ciclo login → menú → cierre de sesión (CU-02). */
public class LoginVista {

    private final MensajesVista mensajes = new MensajesVista();

    public void iniciar() {
        mensajeBienvenida();

        final LoginControlador controlador;
        try {
            controlador = new LoginControlador();
        } catch (ErrorServicioModelo | IllegalStateException | IllegalArgumentException ex) {
            mensajes.mostrarError(ex.getMessage());
            mensajes.mostrarMensaje("Revise servidor.properties o ejecute con "
                    + "-Dservidor.url=http://IP:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1");
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
                mensajes.mostrarMensaje("Sesion finalizada. Gracias por usar MonsterCell.");
                return;
            } catch (ErrorServicioModelo ex) {
                mensajes.mostrarErrorServicio(ex);
            } catch (IllegalArgumentException ex) {
                mensajes.mostrarError(ex.getMessage());
            }
        }
    }

    // CU-02: si el servidor no responde, la sesión local ya quedó eliminada
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
                "Bienvenido(a), " + Formato.valorO(sesion.getNombre()),
                "Usuario : " + Formato.valorO(sesion.getUsuario()),
                "Sesion activa hasta las " + Formato.hora(sesion.getExpiraEn()),
                "(se renueva con cada operacion)"));
    }

    public void mensajeBienvenida() {
        String colorCian = "\u001B[36m";
        String colorReset = "\u001B[0m";
        String mensaje = """
        +------------------------------------------------------------+
        |                                                            |
        |   __  __  ___  _  _  ___ _____ ___ ___                     |
        |  |  \\/  |/ _ \\| \\| |/ __|_   _| __| _ \\                    |
        |  | |\\/| | (_) | .` |\\__ \\ | | | _||   /                    |
        |  |_|  |_|\\___/|_|\\_||___/ |_| |___|_|_\\   CELL             |
        |                                                            |
        |        Cliente Consola REST  -  Grupo 03 (NRC 36884)       |
        +------------------------------------------------------------+""";
        System.out.println(colorCian + mensaje + colorReset);
    }
}
