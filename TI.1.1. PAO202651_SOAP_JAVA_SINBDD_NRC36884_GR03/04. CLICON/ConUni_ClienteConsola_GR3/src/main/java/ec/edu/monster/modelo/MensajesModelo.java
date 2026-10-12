package ec.edu.monster.modelo;

import java.text.Normalizer;

public class MensajesModelo {

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

    // La consola no muestra bien tildes ni simbolos (Á, í, ², °) que llegan del servidor:
    // "Área" -> "Area", "m²" -> "m2", "°C" -> "C"
    private String sinTildes(String texto) {
        if (texto == null) {
            return null;
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x00-\\x7F]", "");
    }
}
