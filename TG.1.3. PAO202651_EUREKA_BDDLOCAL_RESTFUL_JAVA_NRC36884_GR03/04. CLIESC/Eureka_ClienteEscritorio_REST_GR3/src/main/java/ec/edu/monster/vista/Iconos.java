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
 * Iconos del cliente móvil (res/drawable/ic_*.xml): los mismos trazos
 * vectoriales de 24 x 24 dibujados con Java2D, así se ven nítidos a cualquier
 * tamaño y en cualquier color.
 */
public final class Iconos {

    public enum Nombre {
        BANCO("M4,10h3v7H4zM10.5,10h3v7h-3zM2,19h20v3H2zM17,10h3v7h-3zM12,1L2,6v2h20V6z"),
        CUENTA("M21,18v1c0,1.1 -0.9,2 -2,2H5c-1.11,0 -2,-0.9 -2,-2V5c0,-1.1 0.89,-2 2,-2h14c1.1,0 2,0.9 2,2v1h-9"
                + "c-1.11,0 -2,0.9 -2,2v8c0,1.1 0.89,2 2,2h9zM12,16h10V8H12v8zM16,13.5c-0.83,0 -1.5,-0.67 -1.5,-1.5"
                + "s0.67,-1.5 1.5,-1.5 1.5,0.67 1.5,1.5 -0.67,1.5 -1.5,1.5z"),
        MOVIMIENTOS("M19.5,3.5L18,2l-1.5,1.5L15,2l-1.5,1.5L12,2l-1.5,1.5L9,2L7.5,3.5L6,2v14H3v3c0,1.66 1.34,3 3,3h12"
                + "c1.66,0 3,-1.34 3,-3V2L19.5,3.5zM19,19c0,0.55 -0.45,1 -1,1s-1,-0.45 -1,-1v-3H8V5h11V19zM9,7h6v2H9z"
                + "M16,7h2v2h-2zM9,10h6v2H9zM16,10h2v2h-2z"),
        DEPOSITO("M20,12l-1.41,-1.41L13,16.17V4h-2v12.17l-5.58,-5.59L4,12l8,8 8,-8z"),
        RETIRO("M4,12l1.41,1.41L11,7.83V20h2V7.83l5.58,5.59L20,12l-8,-8 -8,8z"),
        TRANSFERENCIA("M6.99,11L3,15l3.99,4v-3H14v-2H6.99v-3zM21,9l-3.99,-4v3H10v2h7.01v3L21,9z"),
        SALIR("M17,7l-1.41,1.41L18.17,11H8v2h10.17l-2.58,2.58L17,17l5,-5zM4,5h8V3H4c-1.1,0 -2,0.9 -2,2v14"
                + "c0,1.1 0.9,2 2,2h8v-2H4V5z"),
        VOLVER("M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z"),
        INFO("M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-6h2v6zM13,9h-2L11,7h2v2z"),
        ERROR("M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM13,17h-2v-2h2v2zM13,13h-2L11,7h2v6z"),
        EXITO("M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM10,17l-5,-5 1.41,-1.41L10,14.17"
                + "l7.59,-7.59L19,8l-9,9z"),
        ADVERTENCIA("M1,21h22L12,2 1,21zM13,18h-2v-2h2v2zM13,14h-2v-4h2v4z"),
        VER("M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73,-4.39 -6,-7.5 -11,-7.5z"
                + "M12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5zM12,9c-1.66,0 -3,1.34 -3,3"
                + "s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z"),
        OCULTAR("M12,7c2.76,0 5,2.24 5,5 0,0.65 -0.13,1.26 -0.36,1.83l2.92,2.92c1.51,-1.26 2.7,-2.89 3.43,-4.75"
                + " -1.73,-4.39 -6,-7.5 -11,-7.5 -1.4,0 -2.74,0.25 -3.98,0.7l2.16,2.16C10.74,7.13 11.35,7 12,7z"
                + "M2,4.27l2.28,2.28 0.46,0.46C3.08,8.3 1.78,10.02 1,12c1.73,4.39 6,7.5 11,7.5 1.55,0 3.03,-0.3 4.38,-0.84"
                + "l0.42,0.42L19.73,22 21,20.73 3.27,3 2,4.27zM7.53,9.8l1.55,1.55c-0.05,0.21 -0.08,0.43 -0.08,0.65"
                + " 0,1.66 1.34,3 3,3 0.22,0 0.44,-0.03 0.65,-0.08l1.55,1.55c-0.67,0.33 -1.41,0.53 -2.2,0.53"
                + " -2.76,0 -5,-2.24 -5,-5 0,-0.79 0.2,-1.53 0.53,-2.2zM11.84,9.02l3.15,3.15 0.02,-0.16"
                + "c0,-1.66 -1.34,-3 -3,-3l-0.17,0.01z");

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
     * Convierte el pathData de Android (subconjunto SVG: M, L, H, V, C, S y Z,
     * absolutos y relativos) en una figura de Java2D.
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
