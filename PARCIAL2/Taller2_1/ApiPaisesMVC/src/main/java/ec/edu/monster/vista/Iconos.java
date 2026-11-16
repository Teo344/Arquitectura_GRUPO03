package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;

/**
 * Iconos vectoriales de 24 x 24 (trazos de Material Icons) dibujados con
 * Java2D: se ven nítidos a cualquier tamaño y en cualquier color. Mismo
 * mecanismo que los clientes de escritorio de los talleres anteriores.
 *
 * @author carlo
 */
public final class Iconos {

    public enum Nombre {
        PUBLICO("M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM11,19.93c-3.95,-0.49 -7,-3.85"
                + " -7,-7.93 0,-0.62 0.08,-1.21 0.21,-1.79L9,15v1c0,1.1 0.9,2 2,2v1.93zM17.9,17.39c-0.26,-0.81 -1,-1.39"
                + " -1.9,-1.39h-1v-3c0,-0.55 -0.45,-1 -1,-1H8v-2h2c0.55,0 1,-0.45 1,-1V7h2c1.1,0 2,-0.9 2,-2v-0.41"
                + "c2.93,1.19 5,4.06 5,7.41 0,2.08 -0.8,3.97 -2.1,5.39z"),
        TABLA("M3,14h4v-4H3v4zM3,19h4v-4H3v4zM3,9h4V5H3v4zM8,14h13v-4H8v4zM8,19h13v-4H8v4zM8,5v4h13V5H8z"),
        BUSCAR("M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16"
                + " 9.5,16c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99"
                + " 5,9.5S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z"),
        GRAFICO("M5,9.2h3V19H5zM10.6,5h2.8v14h-2.8zM16.2,13H19v6h-2.8z"),
        DESCARGAR("M19,9h-4V3H9v6H5l7,7 7,-7zM5,18v2h14v-2H5z"),
        AGREGAR("M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"),
        GUARDAR("M17,3H5c-1.11,0 -2,0.9 -2,2v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2V7l-4,-4zM12,19c-1.66,0 -3,-1.34"
                + " -3,-3s1.34,-3 3,-3 3,1.34 3,3 -1.34,3 -3,3zM15,9H5V5h10v4z"),
        ELIMINAR("M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z"),
        ACTUALIZAR("M17.65,6.35C16.2,4.9 14.21,4 12,4c-4.42,0 -7.99,3.58 -7.99,8s3.57,8 7.99,8c3.73,0 6.84,-2.55"
                + " 7.73,-6h-2.08c-0.82,2.33 -3.04,4 -5.65,4 -3.31,0 -6,-2.69 -6,-6s2.69,-6 6,-6c1.66,0 3.14,0.69"
                + " 4.22,1.78L13,11h7V4l-2.35,2.35z"),
        BASE_DATOS("M2,20h20v-4H2v4zM4,17h2v2H4v-2zM2,4v4h20V4H2zM6,7H4V5h2v2zM2,14h20v-4H2v4zM4,11h2v2H4v-2z"),
        LLAVE("M12.65,10C11.83,7.67 9.61,6 7,6c-3.31,0 -6,2.69 -6,6s2.69,6 6,6c2.61,0 4.83,-1.67 5.65,-4H17v4h4v-4h2"
                + "v-4H12.65zM7,14c-1.1,0 -2,-0.9 -2,-2s0.9,-2 2,-2 2,0.9 2,2 -0.9,2 -2,2z"),
        PERSONAS("M16,11c1.66,0 2.99,-1.34 2.99,-3S17.66,5 16,5c-1.66,0 -3,1.34 -3,3s1.34,3 3,3zM8,11c1.66,0 2.99,-1.34"
                + " 2.99,-3S9.66,5 8,5C6.34,5 5,6.34 5,8s1.34,3 3,3zM8,13c-2.33,0 -7,1.17 -7,3.5V19h14v-2.5"
                + "c0,-2.33 -4.67,-3.5 -7,-3.5zM16,13c-0.29,0 -0.62,0.02 -0.97,0.05 1.16,0.84 1.97,1.97 1.97,3.45V19h6"
                + "v-2.5c0,-2.33 -4.67,-3.5 -7,-3.5z"),
        UBICACION("M12,2C8.13,2 5,5.13 5,9c0,5.25 7,13 7,13s7,-7.75 7,-13c0,-3.87 -3.13,-7 -7,-7zM12,11.5"
                + "c-1.38,0 -2.5,-1.12 -2.5,-2.5s1.12,-2.5 2.5,-2.5 2.5,1.12 2.5,2.5 -1.12,2.5 -2.5,2.5z"),
        ENTRAR("M12,4l-1.41,1.41L16.17,11H4v2h12.17l-5.58,5.59L12,20l8,-8z"),
        SALIR("M17,7l-1.41,1.41L18.17,11H8v2h10.17l-2.58,2.58L17,17l5,-5zM4,5h8V3H4c-1.1,0 -2,0.9 -2,2v14"
                + "c0,1.1 0.9,2 2,2h8v-2H4V5z"),
        CERRAR("M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z"),
        EXPANDIR("M16.59,8.59L12,13.17 7.41,8.59 6,10l6,6 6,-6z"),
        FLECHA_ARRIBA("M4,12l1.41,1.41L11,7.83V20h2V7.83l5.58,5.59L20,12l-8,-8 -8,8z"),
        FLECHA_ABAJO("M20,12l-1.41,-1.41L13,16.17V4h-2v12.17l-5.58,-5.59L4,12l8,8 8,-8z"),
        INFO("M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2L11,7h2v2z"),
        ERROR("M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-2h2v2zM13,13h-2L11,7h2v6z"),
        EXITO("M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,17l-5,-5 1.41,-1.41L10,14.17"
                + "l7.59,-7.59L19,8l-9,9z"),
        ADVERTENCIA("M1,21h22L12,2 1,21zM13,18h-2v-2h2v2zM13,14h-2v-4h2v4z");

        private final String trazo;

        Nombre(String trazo) {
            this.trazo = trazo;
        }
    }

    private static final Pattern ELEMENTO = Pattern.compile("[MmLlHhVvCcSsZz]|-?(?:\\d+\\.?\\d*|\\.\\d+)");
    private static final Map<Nombre, Shape> FORMAS = new EnumMap<>(Nombre.class);

    private Iconos() {
    }

    public static Icon icono(Nombre nombre, Color color, int tamano) {
        return new IconoVectorial(forma(nombre), color, tamano);
    }

    /** Dibuja el icono dentro del cuadro (x, y, tamano). */
    public static void pintar(Graphics2D g, Nombre nombre, Color color, int x, int y, int tamano) {
        Graphics2D copia = (Graphics2D) g.create();
        copia.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        copia.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        copia.translate(x, y);
        copia.scale(tamano / 24.0, tamano / 24.0);
        copia.setColor(color);
        copia.fill(forma(nombre));
        copia.dispose();
    }

    private static synchronized Shape forma(Nombre nombre) {
        return FORMAS.computeIfAbsent(nombre, n -> leerTrazo(n.trazo));
    }

    /**
     * Convierte un trazo SVG (subconjunto: M, L, H, V, C, S y Z, absolutos y
     * relativos) en una figura de Java2D.
     */
    private static Shape leerTrazo(String trazo) {
        List<String> elementos = new ArrayList<>();
        Matcher buscador = ELEMENTO.matcher(trazo);
        while (buscador.find()) {
            elementos.add(buscador.group());
        }

        Path2D.Double figura = new Path2D.Double(Path2D.WIND_NON_ZERO);
        double x = 0, y = 0, inicioX = 0, inicioY = 0, controlX = 0, controlY = 0;
        char comando = 'M';
        char anterior = ' ';
        int i = 0;
        while (i < elementos.size()) {
            String elemento = elementos.get(i);
            if (Character.isLetter(elemento.charAt(0))) {
                comando = elemento.charAt(0);
                i++;
                if (comando == 'Z' || comando == 'z') {
                    figura.closePath();
                    x = inicioX;
                    y = inicioY;
                    anterior = comando;
                    continue;
                }
            }
            boolean relativo = Character.isLowerCase(comando);
            double baseX = relativo ? x : 0;
            double baseY = relativo ? y : 0;
            switch (Character.toUpperCase(comando)) {
                case 'M' -> {
                    x = baseX + numero(elementos, i++);
                    y = baseY + numero(elementos, i++);
                    figura.moveTo(x, y);
                    inicioX = x;
                    inicioY = y;
                    // Los pares que siguen a un M son líneas
                    comando = relativo ? 'l' : 'L';
                }
                case 'L' -> {
                    x = baseX + numero(elementos, i++);
                    y = baseY + numero(elementos, i++);
                    figura.lineTo(x, y);
                }
                case 'H' -> {
                    x = baseX + numero(elementos, i++);
                    figura.lineTo(x, y);
                }
                case 'V' -> {
                    y = baseY + numero(elementos, i++);
                    figura.lineTo(x, y);
                }
                case 'C' -> {
                    double x1 = baseX + numero(elementos, i++);
                    double y1 = baseY + numero(elementos, i++);
                    controlX = baseX + numero(elementos, i++);
                    controlY = baseY + numero(elementos, i++);
                    x = baseX + numero(elementos, i++);
                    y = baseY + numero(elementos, i++);
                    figura.curveTo(x1, y1, controlX, controlY, x, y);
                }
                case 'S' -> {
                    // El primer punto de control es el reflejo del último de la curva anterior
                    boolean seguida = "CcSs".indexOf(anterior) >= 0;
                    double x1 = seguida ? 2 * x - controlX : x;
                    double y1 = seguida ? 2 * y - controlY : y;
                    controlX = baseX + numero(elementos, i++);
                    controlY = baseY + numero(elementos, i++);
                    x = baseX + numero(elementos, i++);
                    y = baseY + numero(elementos, i++);
                    figura.curveTo(x1, y1, controlX, controlY, x, y);
                }
                default -> throw new IllegalArgumentException("Comando de trazo no soportado: " + comando);
            }
            anterior = comando;
        }
        return figura;
    }

    private static double numero(List<String> elementos, int indice) {
        return Double.parseDouble(elementos.get(indice));
    }

    private record IconoVectorial(Shape forma, Color color, int tamano) implements Icon {

        @Override
        public void paintIcon(Component componente, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.translate(x, y);
            g.scale(tamano / 24.0, tamano / 24.0);
            g.setColor(componente != null && !componente.isEnabled() ? Tema.DESHABILITADO_TEXTO : color);
            g.fill(forma);
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return tamano;
        }

        @Override
        public int getIconHeight() {
            return tamano;
        }
    }
}
