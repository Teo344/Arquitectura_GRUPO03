package ec.edu.monster.modelo;

public class MensajesModelo {

    private static final String COLOR_ROJO = "\u001B[31m";
    private static final String COLOR_AMARILLO = "\u001B[33m";
    private static final String COLOR_RESET = "\u001B[0m";

    public void mostrarTitulo(String titulo) {
        System.out.println();
        System.out.println(COLOR_AMARILLO + titulo + COLOR_RESET);
        System.out.println("-".repeat(titulo.length()));
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    public void mostrarError(String mensaje) {
        System.out.println(COLOR_ROJO + "ERROR: " + mensaje + COLOR_RESET);
    }
}
