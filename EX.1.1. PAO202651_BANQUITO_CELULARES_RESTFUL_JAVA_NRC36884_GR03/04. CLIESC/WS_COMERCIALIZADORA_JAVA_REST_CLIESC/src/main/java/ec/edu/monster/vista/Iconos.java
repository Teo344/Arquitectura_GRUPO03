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
 * Iconos de los clientes del grupo: los mismos trazos vectoriales de 24 x 24
 * del cliente web (WEB-INF/tags/icono.tag) dibujados con Java2D, así se ven
 * nítidos a cualquier tamaño y en cualquier color.
 */
public final class Iconos {

    public enum Nombre {
        BANCO("M4,10h3v7H4zM10.5,10h3v7h-3zM2,19h20v3H2zM17,10h3v7h-3zM12,1L2,6v2h20V6z"),
        CELULAR("M17,1.01L7,1c-1.1,0 -2,0.9 -2,2v18c0,1.1 0.9,2 2,2h10c1.1,0 2,-0.9 2,-2V3c0,-1.1 -0.9,-1.99 -2,-1.99z"
                + "M17,19H7V5h10v14z"),
        CARRITO("M7,18c-1.1,0 -1.99,0.9 -1.99,2S5.9,22 7,22s2,-0.9 2,-2 -0.9,-2 -2,-2zM1,2v2h2l3.6,7.59 -1.35,2.45"
                + "c-0.16,0.28 -0.25,0.61 -0.25,0.96 0,1.1 0.9,2 2,2h12v-2H7.42c-0.14,0 -0.25,-0.11 -0.25,-0.25l0.03,-0.12"
                + " 0.9,-1.63h7.45c0.75,0 1.41,-0.41 1.75,-1.03l3.58,-6.49c0.08,-0.14 0.12,-0.31 0.12,-0.48 0,-0.55"
                + " -0.45,-1 -1,-1H5.21l-0.94,-2H1zM17,18c-1.1,0 -1.99,0.9 -1.99,2s0.89,2 1.99,2 2,-0.9 2,-2 -0.9,-2 -2,-2z"),
        FACTURA("M18,17H6v-2h12v2zM18,13H6v-2h12v2zM18,9H6V7h12v2zM3,22l1.5,-1.5L6,22l1.5,-1.5L9,22l1.5,-1.5L12,22"
                + "l1.5,-1.5L15,22l1.5,-1.5L18,22l1.5,-1.5L21,22V2l-1.5,1.5L18,2l-1.5,1.5L15,2l-1.5,1.5L12,2l-1.5,1.5"
                + "L9,2 7.5,3.5 6,2 4.5,3.5 3,2v20z"),
        CATALOGO("M4,11h5V5H4v6zM4,18h5v-6H4v6zM10,18h5v-6h-5v6zM16,18h5v-6h-5v6zM10,11h5V5h-5v6zM16,5v6h5V5h-5z"),
        CREDITO("M20,4H4c-1.11,0 -1.99,0.89 -1.99,2L2,18c0,1.11 0.89,2 2,2h16c1.11,0 2,-0.89 2,-2V6c0,-1.11 -0.89,-2 -2,-2z"
                + "M20,18H4v-6h16v6zM20,8H4V6h16v2z"),
        CALCULADORA("M19,3H5C3.9,3 3,3.9 3,5v14c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V5C21,3.9 20.1,3 19,3zM13.03,7.06"
                + "L14.09,6l1.41,1.41L16.91,6l1.06,1.06 -1.41,1.41 1.41,1.41 -1.06,1.06L15.5,9.54l-1.41,1.41 -1.06,-1.06"
                + " 1.41,-1.41L13.03,7.06zM6.25,7.72h5v1.5h-5V7.72zM11.5,16h-2v2H8v-2H6v-1.5h2v-2h1.5v2h2V16zM18,17.25h-5"
                + "v-1.5h5V17.25zM18,14.75h-5v-1.5h5V14.75z"),
        PERSONA("M12,12c2.21,0 4,-1.79 4,-4s-1.79,-4 -4,-4 -4,1.79 -4,4 1.79,4 4,4zM12,14c-2.67,0 -8,1.34 -8,4v2h16v-2"
                + "c0,-2.66 -5.33,-4 -8,-4z"),
        BUSCAR("M15.5,14h-0.79l-0.28,-0.27C15.41,12.59 16,11.11 16,9.5 16,5.91 13.09,3 9.5,3S3,5.91 3,9.5 5.91,16 9.5,16"
                + "c1.61,0 3.09,-0.59 4.23,-1.57l0.27,0.28v0.79l5,4.99L20.49,19l-4.99,-5zM9.5,14C7.01,14 5,11.99 5,9.5"
                + "S7.01,5 9.5,5 14,7.01 14,9.5 11.99,14 9.5,14z"),
        AGREGAR("M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"),
        QUITAR("M19,13H5v-2h14v2z"),
        EDITAR("M3,17.25V21h3.75L17.81,9.94l-3.75,-3.75L3,17.25zM20.71,7.04c0.39,-0.39 0.39,-1.02 0,-1.41l-2.34,-2.34"
                + "c-0.39,-0.39 -1.02,-0.39 -1.41,0l-1.83,1.83 3.75,3.75 1.83,-1.83z"),
        ELIMINAR("M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z"),
        IMPRIMIR("M19,8H5c-1.66,0 -3,1.34 -3,3v6h4v4h12v-4h4v-6c0,-1.66 -1.34,-3 -3,-3zM16,19H8v-5h8v5zM19,12"
                + "c-0.55,0 -1,-0.45 -1,-1s0.45,-1 1,-1 1,0.45 1,1 -0.45,1 -1,1zM18,3H6v4h12V3z"),
        CERRAR("M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z"),
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
