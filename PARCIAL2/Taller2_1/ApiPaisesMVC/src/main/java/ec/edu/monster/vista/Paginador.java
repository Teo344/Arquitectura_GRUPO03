package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Pie de una tabla paginada: "Mostrando 11–20 de 250 países", el selector de
 * filas por página y los botones primera / anterior / números / siguiente /
 * última. Con muchas páginas muestra la primera, la última y las vecinas de
 * la actual, separadas por "…", para que el pie no crezca sin límite.
 *
 * @author carlo
 */
final class Paginador extends JPanel {

    /** Hasta este número de páginas se muestran todos los botones. */
    private static final int MAX_SIN_PUNTOS = 7;
    private static final int LADO_BOTON = 36;

    private final IntConsumer irA;
    private final JLabel rango = Componentes.etiqueta("", PEQUENO, TEXTO_SECUNDARIO);
    private final List<BotonPagina> tamanos = new ArrayList<>();
    private final JPanel numeros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    private final BotonPagina primera = BotonPagina.icono(Iconos.Nombre.PRIMERA, "Primera página");
    private final BotonPagina anterior = BotonPagina.icono(Iconos.Nombre.ANTERIOR, "Página anterior");
    private final BotonPagina siguiente = BotonPagina.icono(Iconos.Nombre.SIGUIENTE, "Página siguiente");
    private final BotonPagina ultima = BotonPagina.icono(Iconos.Nombre.ULTIMA, "Última página");
    private int actual;
    private int total = 1;

    Paginador(int[] opcionesTamano, int tamanoInicial, IntConsumer irA, IntConsumer cambiarTamano) {
        super(new BorderLayout(L, 0));
        this.irA = irA;
        setBackground(BLANCO);
        setBorder(new EmptyBorder(0, L, 0, L));
        setPreferredSize(new Dimension(0, 60));

        JPanel grupoTamano = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Componentes.suave(graphics);
                g.setColor(DIVISOR);
                g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f, 16, 16));
                g.dispose();
            }
        };
        grupoTamano.setOpaque(false);
        grupoTamano.getAccessibleContext().setAccessibleName("Filas por página");
        for (int opcion : opcionesTamano) {
            BotonPagina boton = BotonPagina.segmento(String.valueOf(opcion));
            boton.getAccessibleContext().setAccessibleName(opcion + " filas por página");
            boton.setActual(opcion == tamanoInicial);
            boton.addActionListener(e -> {
                tamanos.forEach(b -> b.setActual(b == boton));
                cambiarTamano.accept(opcion);
            });
            tamanos.add(boton);
            grupoTamano.add(boton);
        }

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izquierda.setOpaque(false);
        izquierda.add(rango);
        izquierda.add(Box.createHorizontalStrut(L));
        izquierda.add(Componentes.etiqueta("Filas por página", PEQUENO, TEXTO_SECUNDARIO));
        izquierda.add(Box.createHorizontalStrut(XS));
        izquierda.add(grupoTamano);
        add(Componentes.enCentro(izquierda), BorderLayout.WEST);

        primera.addActionListener(e -> irA.accept(0));
        anterior.addActionListener(e -> irA.accept(actual - 1));
        siguiente.addActionListener(e -> irA.accept(actual + 1));
        ultima.addActionListener(e -> irA.accept(total - 1));
        numeros.setOpaque(false);
        numeros.getAccessibleContext().setAccessibleName("Paginación");
        add(Componentes.enCentro(numeros), BorderLayout.EAST);
    }

    /**
     * @param pagina           página actual (desde 0)
     * @param paginas          total de páginas (al menos 1)
     * @param desde            índice de la primera fila visible (desde 0)
     * @param visibles         filas en esta página
     * @param filas            filas en todas las páginas
     * @param sustantivoPlural "países"
     */
    void actualizar(int pagina, int paginas, int desde, int visibles, int filas, String sustantivoPlural) {
        this.actual = pagina;
        this.total = paginas;
        rango.setText(filas == 0 ? "Sin resultados"
                : "Mostrando " + Formato.entero(desde + 1) + "–" + Formato.entero(desde + visibles) + " de "
                + Formato.entero(filas) + " " + sustantivoPlural);

        numeros.removeAll();
        numeros.add(primera);
        numeros.add(anterior);
        for (int n : paginasVisibles(pagina, paginas)) {
            if (n < 0) {
                JLabel puntos = Componentes.etiqueta("…", CUERPO, PLACEHOLDER);
                puntos.setHorizontalAlignment(SwingConstants.CENTER);
                puntos.setPreferredSize(new Dimension(20, LADO_BOTON));
                numeros.add(puntos);
                continue;
            }
            BotonPagina numero = BotonPagina.numero(n + 1);
            numero.setActual(n == pagina);
            numero.addActionListener(e -> irA.accept(n));
            numeros.add(numero);
        }
        numeros.add(siguiente);
        numeros.add(ultima);
        primera.setEnabled(pagina > 0);
        anterior.setEnabled(pagina > 0);
        siguiente.setEnabled(pagina < paginas - 1);
        ultima.setEnabled(pagina < paginas - 1);
        numeros.revalidate();
        numeros.repaint();
    }

    /** Números de página a mostrar (desde 0); -1 marca un salto "…". */
    static List<Integer> paginasVisibles(int actual, int total) {
        List<Integer> resultado = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            boolean visible = total <= MAX_SIN_PUNTOS || i == 0 || i == total - 1 || Math.abs(i - actual) <= 1;
            if (visible) {
                resultado.add(i);
            } else if (resultado.get(resultado.size() - 1) != -1) {
                resultado.add(-1);
            }
        }
        return resultado;
    }

    /** Botón cuadrado de 36 px: número de página, flecha o segmento del selector de tamaño. */
    private static final class BotonPagina extends JButton {

        private final boolean segmento;
        private boolean esActual;

        private BotonPagina(String texto, boolean segmento) {
            super(texto);
            this.segmento = segmento;
            setFont(PEQUENO_SEMINEGRITA);
            setForeground(TEXTO);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Componentes.MANO);
            setBorder(new EmptyBorder(0, XS, 0, XS));
        }

        static BotonPagina numero(int numero) {
            BotonPagina boton = new BotonPagina(String.valueOf(numero), false);
            boton.getAccessibleContext().setAccessibleName("Página " + numero);
            return boton;
        }

        static BotonPagina segmento(String texto) {
            return new BotonPagina(texto, true);
        }

        static BotonPagina icono(Iconos.Nombre icono, String descripcion) {
            BotonPagina boton = new BotonPagina(null, false);
            boton.setIcon(Iconos.icono(icono, TEXTO, 18));
            boton.setDisabledIcon(Iconos.icono(icono, DESHABILITADO_TEXTO, 18));
            boton.setToolTipText(descripcion);
            boton.getAccessibleContext().setAccessibleName(descripcion);
            return boton;
        }

        void setActual(boolean actual) {
            this.esActual = actual;
            setForeground(actual ? BLANCO : TEXTO);
            getAccessibleContext().setAccessibleDescription(actual ? "Actual" : null);
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamano = super.getPreferredSize();
            return segmento ? new Dimension(Math.max(40, tamano.width), 32)
                    : new Dimension(Math.max(LADO_BOTON, tamano.width), LADO_BOTON);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = Componentes.suave(graphics);
            float radio = segmento ? 14 : 16;
            RoundRectangle2D forma = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f,
                    radio, radio);
            Color fondo;
            if (esActual) {
                fondo = segmento ? AZUL_OSCURO : AZUL_ACCION;
            } else if (!isEnabled()) {
                fondo = SUPERFICIE;
            } else {
                fondo = getModel().isRollover() ? AZUL_SUAVE : BLANCO;
            }
            g.setColor(fondo);
            g.fill(forma);
            if (!segmento) {
                g.setColor(esActual ? AZUL_ACCION : isEnabled() ? DIVISOR : FONDO_SECCION);
                g.draw(forma);
            }
            if (isFocusOwner()) {
                g.setColor(AZUL_FOCO);
                g.setStroke(new BasicStroke(2f));
                g.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2f, getHeight() - 2f, radio, radio));
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
