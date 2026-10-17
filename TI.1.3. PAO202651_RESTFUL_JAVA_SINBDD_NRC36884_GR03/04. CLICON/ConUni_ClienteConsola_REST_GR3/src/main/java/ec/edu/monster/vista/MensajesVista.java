package ec.edu.monster.vista;

import java.text.Normalizer;

/** Presentación de mensajes de consola, siguiendo el estilo de TI.1.1. */
public class MensajesVista {

    private static final String COLOR_ROJO = "\u001B[31m";
    private static final String COLOR_AMARILLO = "\u001B[33m";
    private static final String COLOR_RESET = "\u001B[0m";

    public void mostrarTitulo(String titulo) {
        String texto = sinTildes(titulo);
        System.out.println();
        System.out.println(COLOR_AMARILLO + texto + COLOR_RESET);
        System.out.println("-".repeat(texto.length()));
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(sinTildes(mensaje));
    }

    public void mostrarError(String mensaje) {
        System.out.println(COLOR_ROJO + "ERROR: " + sinTildes(mensaje) + COLOR_RESET);
    }

    private String sinTildes(String texto) {
        if (texto == null) {
            return "Error desconocido";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x00-\\x7F]", "");
    }
}
