package ec.edu.monster.vista;

import ec.edu.monster.modelo.ErrorServicioModelo;
import java.text.Normalizer;
import java.util.List;

/** Presentación de mensajes de consola, siguiendo el estilo de TI.1.1 y TI.1.3. */
public class MensajesVista {

    private static final String COLOR_ROJO = "\u001B[31m";
    private static final String COLOR_VERDE = "\u001B[32m";
    private static final String COLOR_AMARILLO = "\u001B[33m";
    private static final String COLOR_CIAN = "\u001B[36m";
    private static final String COLOR_RESET = "\u001B[0m";

    private static final int ANCHO_RECUADRO = 62;

    public void mostrarTitulo(String titulo) {
        String texto = sinTildes(titulo);
        System.out.println();
        System.out.println(COLOR_AMARILLO + texto + COLOR_RESET);
        System.out.println("-".repeat(texto.length()));
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(sinTildes(mensaje));
    }

    public void mostrarExito(String mensaje) {
        System.out.println(COLOR_VERDE + sinTildes(mensaje) + COLOR_RESET);
    }

    public void mostrarError(String mensaje) {
        System.out.println(COLOR_ROJO + "ERROR: " + sinTildes(mensaje) + COLOR_RESET);
    }

    /** Muestra el código del ERS (AUTH-001, CRE-002, ...) junto al mensaje del servidor. */
    public void mostrarErrorServicio(ErrorServicioModelo error) {
        String codigo = error.getCodigo() == null ? "" : "[" + error.getCodigo() + "] ";
        mostrarError(codigo + error.getMessage());
    }

    /** Recuadro de texto en cian: pantalla de inicio, datos de sesión y fichas. */
    public void mostrarRecuadro(List<String> lineas) {
        String borde = "+" + "-".repeat(ANCHO_RECUADRO - 2) + "+";
        StringBuilder texto = new StringBuilder(borde).append(System.lineSeparator());
        for (String linea : lineas) {
            String contenido = sinTildes(linea);
            if (contenido.length() > ANCHO_RECUADRO - 4) {
                contenido = contenido.substring(0, ANCHO_RECUADRO - 4);
            }
            texto.append("| ").append(String.format("%-" + (ANCHO_RECUADRO - 4) + "s", contenido))
                    .append(" |").append(System.lineSeparator());
        }
        texto.append(borde);
        System.out.println(COLOR_CIAN + texto + COLOR_RESET);
    }

    // La consola no muestra bien tildes ni la letra ñ: "Sesión" -> "Sesion", "Rumiñahui" -> "Ruminahui"
    static String sinTildes(String texto) {
        if (texto == null) {
            return "Error desconocido";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^\\x00-\\x7F]", "");
    }
}
