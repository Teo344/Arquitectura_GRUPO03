package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;

/**
 * Componentes de la identidad "Azul" reutilizados de los clientes de
 * escritorio anteriores (TG.1.3) y construidos solo con los valores de
 * {@link Tema}. Se agregaron el selector desplegable, las alertas por tipo y
 * la confirmación de acciones destructivas que necesita esta aplicación.
 *
 * @author carlo
 */
public final class Componentes {

    /** Espacio que deja cada superficie para su sombra; todas lo usan para quedar alineadas. */
    static final int MARGEN_SOMBRA = 6;

    static final Cursor MANO = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    private Componentes() {
    }

    // ================================================================ Contenedores

    /**
     * Columna de componentes: cada hijo ocupa todo el ancho, con su alto
     * preferido, y se separa del anterior por un espacio. Los componentes
     * ocultos no ocupan lugar ni dejan su espacio.
     */
    public static class Pila extends JPanel {

        public Pila() {
            super(new LayoutPila());
            setOpaque(false);
        }

        public Pila agregar(Component hijo) {
            return agregar(hijo, 0);
        }

        public Pila agregar(Component hijo, int espacioArriba) {
            add(hijo, new Ubicacion(espacioArriba, false));
            return this;
        }

        public Pila agregarCentrado(Component hijo, int espacioArriba) {
            add(hijo, new Ubicacion(espacioArriba, true));
            return this;
        }
    }

    private record Ubicacion(int espacioArriba, boolean centrado) {
    }

    /** A diferencia de GridBagLayout, nunca cambia a los tamaños mínimos: los campos conservan su alto. */
    private static final class LayoutPila implements LayoutManager2 {

        private final Map<Component, Ubicacion> ubicaciones = new HashMap<>();

        @Override
        public void addLayoutComponent(Component componente, Object restricciones) {
            ubicaciones.put(componente, restricciones instanceof Ubicacion u ? u : new Ubicacion(0, false));
        }

        @Override
        public void addLayoutComponent(String nombre, Component componente) {
            ubicaciones.put(componente, new Ubicacion(0, false));
        }

        @Override
        public void removeLayoutComponent(Component componente) {
            ubicaciones.remove(componente);
        }

        @Override
        public Dimension preferredLayoutSize(Container padre) {
            Insets bordes = padre.getInsets();
            int anchoUtil = padre.getWidth() - bordes.left - bordes.right;
            int ancho = 0;
            int alto = 0;
            for (Component hijo : padre.getComponents()) {
                if (!hijo.isVisible()) {
                    continue;
                }
                Ubicacion ubicacion = ubicaciones.getOrDefault(hijo, new Ubicacion(0, false));
                if (anchoUtil > 0 && !ubicacion.centrado()) {
                    hijo.setSize(anchoUtil, hijo.getHeight());
                }
                Dimension preferido = hijo.getPreferredSize();
                ancho = Math.max(ancho, preferido.width);
                alto += ubicacion.espacioArriba() + preferido.height;
            }
            return new Dimension(ancho + bordes.left + bordes.right, alto + bordes.top + bordes.bottom);
        }

        @Override
        public Dimension minimumLayoutSize(Container padre) {
            return new Dimension(0, preferredLayoutSize(padre).height);
        }

        @Override
        public Dimension maximumLayoutSize(Container padre) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        @Override
        public void layoutContainer(Container padre) {
            Insets bordes = padre.getInsets();
            int anchoUtil = Math.max(0, padre.getWidth() - bordes.left - bordes.right);
            int y = bordes.top;
            for (Component hijo : padre.getComponents()) {
                if (!hijo.isVisible()) {
                    continue;
                }
                Ubicacion ubicacion = ubicaciones.getOrDefault(hijo, new Ubicacion(0, false));
                y += ubicacion.espacioArriba();
                if (ubicacion.centrado()) {
                    Dimension preferido = hijo.getPreferredSize();
                    int ancho = Math.min(preferido.width, anchoUtil);
                    hijo.setBounds(bordes.left + (anchoUtil - ancho) / 2, y, ancho, preferido.height);
                    y += preferido.height;
                } else {
                    hijo.setSize(anchoUtil, hijo.getHeight());
                    int alto = hijo.getPreferredSize().height;
                    hijo.setBounds(bordes.left, y, anchoUtil, alto);
                    y += alto;
                }
            }
        }

        @Override
        public float getLayoutAlignmentX(Container padre) {
            return 0.5f;
        }

        @Override
        public float getLayoutAlignmentY(Container padre) {
            return 0f;
        }

        @Override
        public void invalidateLayout(Container padre) {
        }
    }

    /** Superficie redondeada con fondo, borde y sombra opcional: tarjetas, alertas, chips y estados vacíos. */
    public static class Caja extends Pila {

        private Color fondo;
        private Color borde;
        private float grosor = 1f;
        private final int radio;
        private final int elevacion;
        private final int margen;

        public Caja(Color fondo, Color borde, int radio, int elevacion, int margen, int relleno) {
            this.fondo = fondo;
            this.borde = borde;
            this.radio = radio;
            this.elevacion = elevacion;
            this.margen = margen;
            setRelleno(relleno, relleno, relleno, relleno);
        }

        public final void setRelleno(int arriba, int izquierda, int abajo, int derecha) {
            setBorder(new EmptyBorder(margen + arriba, margen + izquierda, margen + abajo, margen + derecha));
        }

        public void setColores(Color fondo, Color borde) {
            this.fondo = fondo;
            this.borde = borde;
            repaint();
        }

        public void setGrosor(float grosor) {
            this.grosor = grosor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = suave(graphics);
            float x = margen;
            float y = margen;
            float ancho = getWidth() - 2f * margen;
            float alto = getHeight() - 2f * margen;
            // Sombra con el tinte #12306A: capas translúcidas que crecen hacia abajo
            for (int capa = elevacion; capa >= 1; capa--) {
                g.setColor(new Color(TINTE_SOMBRA.getRed(), TINTE_SOMBRA.getGreen(), TINTE_SOMBRA.getBlue(), 6));
                g.fill(new RoundRectangle2D.Float(x - capa, y - capa + 2, ancho + 2f * capa, alto + 2f * capa,
                        2f * (radio + capa), 2f * (radio + capa)));
            }
            if (fondo != null) {
                g.setColor(fondo);
                g.fill(new RoundRectangle2D.Float(x, y, ancho, alto, 2f * radio, 2f * radio));
            }
            if (borde != null) {
                float mitad = grosor / 2f;
                g.setColor(borde);
                g.setStroke(new BasicStroke(grosor));
                g.draw(new RoundRectangle2D.Float(x + mitad, y + mitad, ancho - grosor, alto - grosor,
                        2f * radio - grosor, 2f * radio - grosor));
            }
            g.dispose();
        }
    }

    /** Tarjeta: fondo blanco, borde #D3DCEA, radio 16, sombra y padding 24. */
    public static Caja tarjeta() {
        return new Caja(BLANCO, DIVISOR, RADIO_TARJETA, 4, MARGEN_SOMBRA, L);
    }

    /** Fila horizontal de componentes a su tamaño natural, alineados a la izquierda. */
    public static JPanel fila(int espacio, Component... hijos) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setOpaque(false);
        for (int i = 0; i < hijos.length; i++) {
            if (i > 0) {
                panel.add(Box.createHorizontalStrut(espacio));
            }
            panel.add(hijos[i]);
        }
        return panel;
    }

    /** Centra un componente vertical y horizontalmente dentro de su celda. */
    public static JPanel enCentro(Component hijo) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.add(hijo);
        return panel;
    }

    /** Centra el contenido y limita su ancho en pantallas grandes. Sigue el ancho de la ventana al desplazarse. */
    public static JPanel centrado(JComponent hijo, int maxAncho, int margen, boolean centrarVertical) {
        return new Centrado(hijo, maxAncho, margen, margen, centrarVertical);
    }

    private static final class Centrado extends JPanel implements Scrollable {

        Centrado(JComponent hijo, int maxAncho, int margen, int margenVertical, boolean centrarVertical) {
            setOpaque(false);
            setLayout(new LayoutManager() {
                @Override
                public void addLayoutComponent(String nombre, Component componente) {
                }

                @Override
                public void removeLayoutComponent(Component componente) {
                }

                @Override
                public Dimension preferredLayoutSize(Container padre) {
                    int ancho = padre.getWidth() > 0 ? anchoHijo(padre) : maxAncho;
                    hijo.setSize(ancho, hijo.getHeight());
                    return new Dimension(Math.min(maxAncho, hijo.getPreferredSize().width) + 2 * margen,
                            hijo.getPreferredSize().height + 2 * margenVertical);
                }

                @Override
                public Dimension minimumLayoutSize(Container padre) {
                    return new Dimension(0, 0);
                }

                @Override
                public void layoutContainer(Container padre) {
                    int ancho = anchoHijo(padre);
                    hijo.setSize(ancho, hijo.getHeight());
                    int alto = hijo.getPreferredSize().height;
                    int x = (padre.getWidth() - ancho) / 2;
                    int y = margenVertical;
                    if (centrarVertical) {
                        y = Math.max(margenVertical, (padre.getHeight() - alto) / 2);
                    }
                    hijo.setBounds(x, y, ancho, alto);
                }

                private int anchoHijo(Container padre) {
                    return Math.max(0, Math.min(maxAncho, padre.getWidth() - 2 * margen));
                }
            });
            add(hijo);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientacion, int direccion) {
            return M;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientacion, int direccion) {
            return visible.height - XXL;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        // Si hay espacio de sobra el contenido llena la ventana (y se centra); si no, aparece la barra
        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof JViewport visor && visor.getHeight() > getPreferredSize().height;
        }
    }

    /** Dos columnas que reparten el ancho en la proporción dada. Si llenarAlto, ambas ocupan todo el alto. */
    public static JPanel dosColumnas(JComponent izquierda, JComponent derecha, double fraccion, int espacio,
            boolean llenarAlto) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new LayoutManager() {
            @Override
            public void addLayoutComponent(String nombre, Component componente) {
            }

            @Override
            public void removeLayoutComponent(Component componente) {
            }

            @Override
            public Dimension preferredLayoutSize(Container padre) {
                int[] anchos = anchos(padre.getWidth() > 0 ? padre.getWidth()
                        : izquierda.getPreferredSize().width + derecha.getPreferredSize().width + espacio);
                izquierda.setSize(anchos[0], izquierda.getHeight());
                derecha.setSize(anchos[1], derecha.getHeight());
                int alto = Math.max(izquierda.getPreferredSize().height, derecha.getPreferredSize().height);
                return new Dimension(izquierda.getPreferredSize().width + derecha.getPreferredSize().width + espacio,
                        alto);
            }

            @Override
            public Dimension minimumLayoutSize(Container padre) {
                return new Dimension(0, 0);
            }

            @Override
            public void layoutContainer(Container padre) {
                int[] anchos = anchos(padre.getWidth());
                izquierda.setSize(anchos[0], izquierda.getHeight());
                derecha.setSize(anchos[1], derecha.getHeight());
                int altoIzquierda = llenarAlto ? padre.getHeight() : izquierda.getPreferredSize().height;
                int altoDerecha = llenarAlto ? padre.getHeight() : derecha.getPreferredSize().height;
                izquierda.setBounds(0, 0, anchos[0], altoIzquierda);
                derecha.setBounds(anchos[0] + espacio, 0, anchos[1], altoDerecha);
            }

            private int[] anchos(int total) {
                int disponible = Math.max(0, total - espacio);
                int primero = (int) Math.round(disponible * fraccion);
                return new int[]{primero, disponible - primero};
            }
        });
        panel.add(izquierda);
        panel.add(derecha);
        return panel;
    }

    /** Desplazamiento vertical con la barra delgada del tema. */
    public static JScrollPane desplazable(JComponent contenido) {
        JScrollPane panel = new JScrollPane(contenido, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.setBorder(null);
        panel.setOpaque(false);
        panel.getViewport().setOpaque(false);
        estilizarBarra(panel);
        return panel;
    }

    static void estilizarBarra(JScrollPane panel) {
        panel.getVerticalScrollBar().setUnitIncrement(M);
        panel.getVerticalScrollBar().setUI(new BarraDesplazamiento());
        panel.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        panel.getVerticalScrollBar().setOpaque(false);
        panel.getHorizontalScrollBar().setUnitIncrement(M);
        panel.getHorizontalScrollBar().setUI(new BarraDesplazamiento());
        panel.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
        panel.getHorizontalScrollBar().setOpaque(false);
    }

    private static final class BarraDesplazamiento extends BasicScrollBarUI {

        @Override
        protected JButton createDecreaseButton(int orientacion) {
            return botonVacio();
        }

        @Override
        protected JButton createIncreaseButton(int orientacion) {
            return botonVacio();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent componente, Rectangle area) {
            // Pista transparente: se ve el fondo de la superficie que la contiene
        }

        @Override
        protected void paintThumb(Graphics graphics, JComponent componente, Rectangle area) {
            if (area.isEmpty()) {
                return;
            }
            Graphics2D g = suave(graphics);
            g.setColor(isThumbRollover() ? PLACEHOLDER : BORDE_CAMPO);
            g.fillRoundRect(area.x + 3, area.y + 2, area.width - 6, area.height - 4, 8, 8);
            g.dispose();
        }

        private static JButton botonVacio() {
            JButton boton = new JButton();
            boton.setPreferredSize(new Dimension(0, 0));
            boton.setMinimumSize(new Dimension(0, 0));
            boton.setMaximumSize(new Dimension(0, 0));
            return boton;
        }
    }

    // ================================================================ Textos

    public static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(fuente);
        etiqueta.setForeground(color);
        return etiqueta;
    }

    public static JLabel sobretitulo(String texto) {
        return sobretitulo(texto, AZUL_ACCION);
    }

    public static JLabel sobretitulo(String texto, Color color) {
        return etiqueta(texto.toUpperCase(Locale.ROOT), SOBRETITULO, color);
    }

    /**
     * Encabezado de página: sobretítulo, título H3 y una línea de apoyo a la
     * izquierda; las acciones de la página, alineadas abajo a la derecha.
     */
    public static JPanel encabezadoPagina(String sobretitulo, String titulo, Texto apoyo, JComponent... acciones) {
        JPanel encabezado = new JPanel(new BorderLayout(L, 0));
        encabezado.setOpaque(false);
        Pila textos = new Pila()
                .agregar(sobretitulo(sobretitulo))
                .agregar(etiqueta(titulo, TITULAR_PEQUENO, TEXTO), XXS);
        if (apoyo != null) {
            textos.agregar(apoyo, XXS);
        }
        encabezado.add(textos, BorderLayout.CENTER);
        if (acciones.length > 0) {
            JPanel derecha = new JPanel(new BorderLayout());
            derecha.setOpaque(false);
            derecha.add(fila(XS, acciones), BorderLayout.SOUTH);
            encabezado.add(derecha, BorderLayout.EAST);
        }
        return encabezado;
    }

    /** Texto de varias líneas que se ajusta al ancho disponible (JLabel no lo hace por sí solo). */
    public static class Texto extends JComponent {

        private static final int ANCHO_PREFERIDO = 320;

        private String texto;
        private final int alineacion;

        public Texto(String texto, Font fuente, Color color) {
            this(texto, fuente, color, SwingConstants.LEFT);
        }

        public Texto(String texto, Font fuente, Color color, int alineacion) {
            this.texto = texto == null ? "" : texto;
            this.alineacion = alineacion;
            setFont(fuente);
            setForeground(color);
        }

        public void setTexto(String texto) {
            this.texto = texto == null ? "" : texto;
            revalidate();
            repaint();
        }

        /** Los lectores de pantalla lo anuncian como etiqueta con su texto. */
        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleJComponent() {
                    @Override
                    public AccessibleRole getAccessibleRole() {
                        return AccessibleRole.LABEL;
                    }

                    @Override
                    public String getAccessibleName() {
                        return texto;
                    }
                };
            }
            return accessibleContext;
        }

        @Override
        public Dimension getPreferredSize() {
            int natural = anchoNatural();
            int ancho = getWidth() > 0 ? getWidth() : Math.min(natural, ANCHO_PREFERIDO);
            return new Dimension(Math.min(natural, ANCHO_PREFERIDO), alturaPara(ancho));
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(0, getPreferredSize().height);
        }

        // Al cambiar de ancho cambia el número de líneas: se vuelve a calcular el alto
        @Override
        @SuppressWarnings("deprecation")
        public void reshape(int x, int y, int ancho, int alto) {
            int anterior = getWidth();
            super.reshape(x, y, ancho, alto);
            if (ancho != anterior && alturaPara(ancho) != alto) {
                SwingUtilities.invokeLater(this::revalidate);
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = suave(graphics);
            g.setFont(getFont());
            g.setColor(getForeground());
            FontMetrics medidas = g.getFontMetrics();
            Insets bordes = getInsets();
            int anchoUtil = getWidth() - bordes.left - bordes.right;
            int y = bordes.top + medidas.getAscent();
            for (String linea : lineas(anchoUtil)) {
                int anchoLinea = medidas.stringWidth(linea);
                int x = switch (alineacion) {
                    case SwingConstants.CENTER -> bordes.left + (anchoUtil - anchoLinea) / 2;
                    case SwingConstants.RIGHT -> bordes.left + anchoUtil - anchoLinea;
                    default -> bordes.left;
                };
                g.drawString(linea, x, y);
                y += medidas.getHeight();
            }
            g.dispose();
        }

        private int anchoNatural() {
            FontMetrics medidas = getFontMetrics(getFont());
            Insets bordes = getInsets();
            int maximo = 0;
            for (String parrafo : texto.split("\n", -1)) {
                maximo = Math.max(maximo, medidas.stringWidth(parrafo));
            }
            return maximo + bordes.left + bordes.right;
        }

        private int alturaPara(int ancho) {
            Insets bordes = getInsets();
            int lineas = lineas(Math.max(1, ancho - bordes.left - bordes.right)).size();
            return lineas * getFontMetrics(getFont()).getHeight() + bordes.top + bordes.bottom;
        }

        private List<String> lineas(int ancho) {
            FontMetrics medidas = getFontMetrics(getFont());
            List<String> resultado = new ArrayList<>();
            for (String parrafo : texto.split("\n", -1)) {
                StringBuilder linea = new StringBuilder();
                for (String palabra : parrafo.split(" ")) {
                    if (palabra.isEmpty()) {
                        continue;
                    }
                    String prueba = linea.isEmpty() ? palabra : linea + " " + palabra;
                    if (medidas.stringWidth(prueba) <= ancho) {
                        linea.setLength(0);
                        linea.append(prueba);
                        continue;
                    }
                    if (!linea.isEmpty()) {
                        resultado.add(linea.toString());
                        linea.setLength(0);
                    }
                    // Una palabra más ancha que la línea se corta por caracteres
                    for (char caracter : palabra.toCharArray()) {
                        if (!linea.isEmpty() && medidas.stringWidth(linea.toString() + caracter) > ancho) {
                            resultado.add(linea.toString());
                            linea.setLength(0);
                        }
                        linea.append(caracter);
                    }
                }
                resultado.add(linea.toString());
            }
            return resultado;
        }
    }

    // ================================================================ Botones

    public enum EstiloBoton {
        /** Fondo #1D56CC y texto blanco, 48 de alto. Uno por vista. */
        PRIMARIO,
        /** Fondo #EDF4FF, texto #1744A3 y borde #B8D2FF. */
        SECUNDARIO,
        /** Fondo #C0392B y texto blanco: acciones destructivas. */
        PELIGRO,
        /** Solo texto #1744A3, para cancelar. */
        TEXTO
    }

    /** Botón redondeado (radio 10) con icono opcional y estado de carga con indicador giratorio. */
    public static class Boton extends JButton {

        private final EstiloBoton estilo;
        private final String textoNormal;
        private Iconos.Nombre icono;
        private boolean cargando;
        private int angulo;
        private final Timer giro = new Timer(60, e -> {
            angulo = (angulo + 30) % 360;
            repaint();
        });

        public Boton(String texto, EstiloBoton estilo, Iconos.Nombre icono) {
            super(texto);
            this.estilo = estilo;
            this.textoNormal = texto;
            setFont(BOTON);
            setForeground(colorTexto());
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(0, M, 0, M));
            setIconTextGap(XS);
            setCursor(MANO);
            setIcono(icono);
        }

        public final void setIcono(Iconos.Nombre icono) {
            this.icono = icono;
            actualizarIcono();
        }

        /** Mientras se espera la respuesta el botón se deshabilita y muestra el indicador. */
        public void setCargando(boolean cargando) {
            setCargando(cargando, null);
        }

        /**
         * @param textoEspera texto mientras carga (p. ej. "Importando…"); {@code null} conserva el texto
         */
        public void setCargando(boolean cargando, String textoEspera) {
            this.cargando = cargando;
            setEnabled(!cargando);
            setText(cargando && textoEspera != null ? textoEspera : textoNormal);
            actualizarIcono();
            if (cargando) {
                giro.start();
            } else {
                giro.stop();
            }
        }

        private void actualizarIcono() {
            if (cargando) {
                setIcon(new IndicadorCarga());
                setDisabledIcon(getIcon());
            } else {
                setIcon(icono == null ? null : Iconos.icono(icono, colorTexto(), 18));
                setDisabledIcon(icono == null ? null : Iconos.icono(icono, DESHABILITADO_TEXTO, 18));
            }
        }

        private Color colorTexto() {
            return estilo == EstiloBoton.PRIMARIO || estilo == EstiloBoton.PELIGRO ? BLANCO : AZUL_HOVER;
        }

        private Color fondo() {
            boolean presionado = getModel().isArmed() && getModel().isPressed();
            boolean encima = getModel().isRollover();
            if (!isEnabled()) {
                return estilo == EstiloBoton.TEXTO ? null : FONDO_SECCION;
            }
            return switch (estilo) {
                case PRIMARIO -> presionado ? AZUL_PRESIONADO : encima ? AZUL_HOVER : AZUL_ACCION;
                case PELIGRO -> presionado ? new Color(0x8E2A20) : encima ? new Color(0xA33024) : ERROR_RELLENO;
                case SECUNDARIO -> presionado || encima ? new Color(0xDCE8FF) : AZUL_SUAVE;
                case TEXTO -> presionado || encima ? AZUL_SUAVE : null;
            };
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamano = super.getPreferredSize();
            tamano.height = 48;
            return tamano;
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = suave(graphics);
            Color fondo = fondo();
            if (fondo != null) {
                g.setColor(fondo);
                g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 2f * RADIO_CAMPO, 2f * RADIO_CAMPO));
            }
            if (estilo == EstiloBoton.SECUNDARIO && isEnabled()) {
                g.setColor(BORDE_SUAVE);
                g.draw(new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f,
                        2f * RADIO_CAMPO, 2f * RADIO_CAMPO));
            }
            if (isFocusOwner()) {
                g.setColor(estilo == EstiloBoton.PRIMARIO || estilo == EstiloBoton.PELIGRO ? AZUL_OSCURO : AZUL_FOCO);
                g.setStroke(new BasicStroke(2f));
                g.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2f, getHeight() - 2f,
                        2f * RADIO_CAMPO - 2, 2f * RADIO_CAMPO - 2));
            }
            g.dispose();
            super.paintComponent(graphics);
        }

        /** Arco giratorio de 18 px. */
        private final class IndicadorCarga implements Icon {

            @Override
            public void paintIcon(Component componente, Graphics graphics, int x, int y) {
                Graphics2D g = suave(graphics);
                g.setColor(DESHABILITADO_TEXTO);
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Arc2D.Float(x + 1, y + 1, 16, 16, -angulo, 270, Arc2D.OPEN));
                g.dispose();
            }

            @Override
            public int getIconWidth() {
                return 18;
            }

            @Override
            public int getIconHeight() {
                return 18;
            }
        }
    }

    /** Botón de solo icono con un círculo de fondo al pasar el ratón; siempre con descripción accesible. */
    public static class BotonIcono extends JButton {

        private final Color colorEncima;

        public BotonIcono(Iconos.Nombre icono, Color color, int tamano, Color colorEncima, String descripcion) {
            this.colorEncima = colorEncima;
            setIcon(Iconos.icono(icono, color, tamano));
            setToolTipText(descripcion);
            getAccessibleContext().setAccessibleName(descripcion);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(XS, XS, XS, XS));
            setCursor(MANO);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            if (isEnabled() && (getModel().isRollover() || isFocusOwner())) {
                Graphics2D g = suave(graphics);
                g.setColor(colorEncima);
                int diametro = Math.min(getWidth(), getHeight());
                g.fill(new Ellipse2D.Float((getWidth() - diametro) / 2f, (getHeight() - diametro) / 2f,
                        diametro, diametro));
                g.dispose();
            }
            super.paintComponent(graphics);
        }
    }

    // ================================================================ Formularios

    /** Borde de campo: 48 de alto, radio 10, 1.5 px #B1BFD4; 2 px #1D56CC con foco. */
    private static class MarcoCampo extends JPanel {

        private final JComponent control;

        MarcoCampo(JComponent control) {
            super(new BorderLayout());
            this.control = control;
            setOpaque(false);
            add(control, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamano = super.getPreferredSize();
            tamano.height = 48;
            return tamano;
        }

        boolean tieneFoco() {
            return control.hasFocus();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = suave(graphics);
            boolean habilitado = control.isEnabled();
            boolean foco = tieneFoco();
            float grosor = foco ? 2f : 1.5f;
            float mitad = grosor / 2f;
            g.setColor(habilitado ? BLANCO : FONDO_SECCION);
            g.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 2f * RADIO_CAMPO, 2f * RADIO_CAMPO));
            g.setColor(!habilitado ? DIVISOR : foco ? AZUL_ACCION : BORDE_CAMPO);
            g.setStroke(new BasicStroke(grosor));
            g.draw(new RoundRectangle2D.Float(mitad, mitad, getWidth() - grosor, getHeight() - grosor,
                    2f * RADIO_CAMPO, 2f * RADIO_CAMPO));
            g.dispose();
        }
    }

    private static void repintarAlEnfocar(JComponent control, Component marco) {
        control.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                marco.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                marco.repaint();
            }
        });
    }

    /** Campo de texto con etiqueta visible encima, marcador de ejemplo y ayuda debajo. */
    public static class CampoTexto extends Pila {

        private final TextoConMarcador campo;
        private final MarcoCampo marco;

        public CampoTexto(String etiqueta, String marcador, String ayuda) {
            campo = new TextoConMarcador(marcador);
            campo.setFont(CUERPO_GRANDE);
            campo.setForeground(TEXTO);
            campo.setCaretColor(AZUL_ACCION);
            campo.setDisabledTextColor(DESHABILITADO_TEXTO);
            campo.setSelectionColor(BORDE_SUAVE);
            campo.setSelectedTextColor(TEXTO);
            campo.setOpaque(false);
            campo.setBorder(new EmptyBorder(0, 14, 0, 14));
            campo.getAccessibleContext().setAccessibleName(etiqueta);
            if (ayuda != null) {
                campo.getAccessibleContext().setAccessibleDescription(ayuda);
            }
            marco = new MarcoCampo(campo);
            repintarAlEnfocar(campo, marco);

            if (etiqueta != null) {
                JLabel titulo = Componentes.etiqueta(etiqueta, BOTON, TEXTO);
                titulo.setLabelFor(campo);
                agregar(titulo);
            }
            agregar(marco, etiqueta == null ? 0 : 6);
            if (ayuda != null) {
                agregar(new Texto(ayuda, LEYENDA, TEXTO_SECUNDARIO), XXS);
            }
        }

        public String getTexto() {
            return campo.getText();
        }

        public void setTexto(String texto) {
            campo.setText(texto == null ? "" : texto);
        }

        public void setHabilitado(boolean habilitado) {
            campo.setEnabled(habilitado);
            marco.repaint();
        }

        /** Acción al pulsar Enter en el campo. */
        public void alConfirmar(Runnable accion) {
            campo.addActionListener(e -> accion.run());
        }

        /** Acción en cada cambio del texto (al escribir, pegar o borrar). */
        public void alCambiar(Runnable accion) {
            campo.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    accion.run();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    accion.run();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    accion.run();
                }
            });
        }

        public void enfocar() {
            campo.requestFocusInWindow();
            campo.selectAll();
        }

        JTextField getCampo() {
            return campo;
        }
    }

    private static final class TextoConMarcador extends JTextField {

        private final String marcador;

        TextoConMarcador(String marcador) {
            this.marcador = marcador;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (marcador == null || getDocument().getLength() > 0) {
                return;
            }
            Graphics2D g = suave(graphics);
            g.setFont(getFont());
            g.setColor(PLACEHOLDER);
            FontMetrics medidas = g.getFontMetrics();
            int y = (getHeight() - medidas.getHeight()) / 2 + medidas.getAscent();
            g.drawString(marcador, getInsets().left, y);
            g.dispose();
        }
    }

    /**
     * Lista desplegable con el mismo aspecto que los campos de texto: etiqueta
     * encima, 48 de alto, flecha a la derecha. Muestra un marcador mientras no
     * hay opción elegida.
     */
    public static class CampoSeleccion<T> extends Pila {

        private final JComboBox<T> combo;
        private final MarcoCampo marco;

        public CampoSeleccion(String etiqueta, List<T> opciones, Function<T, String> texto, String marcador) {
            combo = new JComboBox<>(new java.util.Vector<>(opciones));
            combo.setSelectedIndex(-1);
            combo.setFont(CUERPO_GRANDE);
            combo.setForeground(TEXTO);
            combo.setOpaque(false);
            combo.setBorder(null);
            combo.setCursor(MANO);
            combo.setMaximumRowCount(8);
            combo.setUI(new AspectoSeleccion());
            combo.setRenderer(new RenderizadorOpcion<>(texto, marcador));
            combo.getAccessibleContext().setAccessibleName(etiqueta);
            marco = new MarcoCampo(combo) {
                @Override
                boolean tieneFoco() {
                    return combo.hasFocus() || combo.isPopupVisible();
                }
            };
            repintarAlEnfocar(combo, marco);

            if (etiqueta != null) {
                JLabel titulo = Componentes.etiqueta(etiqueta, BOTON, TEXTO);
                titulo.setLabelFor(combo);
                agregar(titulo);
            }
            agregar(marco, etiqueta == null ? 0 : 6);
        }

        @SuppressWarnings("unchecked")
        public T getSeleccion() {
            return (T) combo.getSelectedItem();
        }

        public void setSeleccion(T opcion) {
            combo.setSelectedItem(opcion);
            if (opcion == null) {
                combo.setSelectedIndex(-1);
            }
        }

        /** Acción cuando el usuario elige otra opción. */
        public void alCambiar(Consumer<T> accion) {
            combo.addActionListener(e -> accion.accept(getSeleccion()));
        }

        public void setHabilitado(boolean habilitado) {
            combo.setEnabled(habilitado);
            marco.repaint();
        }

        public void enfocar() {
            combo.requestFocusInWindow();
        }
    }

    private static final class RenderizadorOpcion<T> extends DefaultListCellRenderer {

        private final Function<T, String> texto;
        private final String marcador;

        RenderizadorOpcion(Function<T, String> texto, String marcador) {
            this.texto = texto;
            this.marcador = marcador;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice, boolean elegido,
                boolean foco) {
            super.getListCellRendererComponent(lista, valor, indice, elegido, false);
            boolean vacio = valor == null;
            setText(vacio ? (marcador == null ? " " : marcador) : texto.apply((T) valor));
            setFont(CUERPO_GRANDE);
            // indice -1 es el valor mostrado dentro del campo: sin fondo propio
            boolean enCampo = indice < 0;
            setOpaque(!enCampo);
            setBorder(new EmptyBorder(enCampo ? 0 : 10, 14, enCampo ? 0 : 10, 14));
            setBackground(elegido ? AZUL_SUAVE : BLANCO);
            setForeground(vacio ? PLACEHOLDER : TEXTO);
            return this;
        }
    }

    private static final class AspectoSeleccion extends BasicComboBoxUI {

        @Override
        protected JButton createArrowButton() {
            JButton flecha = new JButton(Iconos.icono(Iconos.Nombre.EXPANDIR, PLACEHOLDER, 22));
            flecha.setContentAreaFilled(false);
            flecha.setBorderPainted(false);
            flecha.setFocusPainted(false);
            flecha.setFocusable(false);
            flecha.setOpaque(false);
            flecha.setBorder(new EmptyBorder(0, XS, 0, S));
            flecha.setCursor(MANO);
            return flecha;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle limites, boolean foco) {
            // El fondo lo pinta MarcoCampo
        }

        @Override
        @SuppressWarnings("unchecked")
        public void paintCurrentValue(Graphics g, Rectangle limites, boolean foco) {
            ListCellRenderer<Object> renderizador = (ListCellRenderer<Object>) comboBox.getRenderer();
            Component valor = renderizador.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1,
                    false, false);
            if (!comboBox.isEnabled()) {
                valor.setForeground(DESHABILITADO_TEXTO);
            }
            currentValuePane.paintComponent(g, valor, comboBox, limites.x, limites.y, limites.width,
                    limites.height, false);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox);
            popup.setBorder(BorderFactory.createLineBorder(DIVISOR));
            return popup;
        }
    }

    // ================================================================ Mensajes y estados

    /** Tipo de alerta: cada uno con su color semántico y su icono (nunca solo color). */
    public enum TipoAlerta {
        INFO(INFO_FONDO, INFO_RELLENO, INFO_TEXTO, Iconos.Nombre.INFO, "Información"),
        EXITO(EXITO_FONDO, EXITO_RELLENO, EXITO_TEXTO, Iconos.Nombre.EXITO, "Correcto"),
        ADVERTENCIA(ADVERTENCIA_FONDO, ADVERTENCIA_RELLENO, ADVERTENCIA_TEXTO, Iconos.Nombre.ADVERTENCIA, "Advertencia"),
        ERROR(ERROR_FONDO, ERROR_RELLENO, ERROR_TEXTO, Iconos.Nombre.ERROR, "Error");

        final Color fondo;
        final Color relleno;
        final Color texto;
        final Iconos.Nombre icono;
        final String nombreAccesible;

        TipoAlerta(Color fondo, Color relleno, Color texto, Iconos.Nombre icono, String nombreAccesible) {
            this.fondo = fondo;
            this.relleno = relleno;
            this.texto = texto;
            this.icono = icono;
            this.nombreAccesible = nombreAccesible;
        }
    }

    /** Alerta con icono y texto. Oculta mientras no hay mensaje; opcionalmente se puede cerrar. */
    public static class Alerta extends Caja {

        private final JLabel icono = new JLabel();
        private final Texto texto = new Texto("", CUERPO, INFO_TEXTO);

        public Alerta(boolean cerrable) {
            super(INFO_FONDO, INFO_RELLENO, RADIO_CAMPO, 0, 0, S);
            JPanel contenido = new JPanel(new BorderLayout(XS, 0));
            contenido.setOpaque(false);
            JPanel columnaIcono = new JPanel(new BorderLayout());
            columnaIcono.setOpaque(false);
            columnaIcono.add(icono, BorderLayout.NORTH);
            contenido.add(columnaIcono, BorderLayout.WEST);
            contenido.add(texto, BorderLayout.CENTER);
            if (cerrable) {
                BotonIcono cerrar = new BotonIcono(Iconos.Nombre.CERRAR, TEXTO_SECUNDARIO, 16,
                        new Color(255, 255, 255, 160), "Cerrar mensaje");
                cerrar.setBorder(new EmptyBorder(XXS, XXS, XXS, XXS));
                cerrar.addActionListener(e -> ocultar());
                JPanel columnaCerrar = new JPanel(new BorderLayout());
                columnaCerrar.setOpaque(false);
                columnaCerrar.add(cerrar, BorderLayout.NORTH);
                contenido.add(columnaCerrar, BorderLayout.EAST);
            }
            agregar(contenido);
            setVisible(false);
        }

        public void mostrar(String mensaje, TipoAlerta tipo) {
            if (mensaje == null || mensaje.isBlank()) {
                ocultar();
                return;
            }
            setColores(tipo.fondo, tipo.relleno);
            icono.setIcon(Iconos.icono(tipo.icono, tipo.relleno, 20));
            icono.getAccessibleContext().setAccessibleName(tipo.nombreAccesible);
            texto.setForeground(tipo.texto);
            texto.setTexto(mensaje);
            getAccessibleContext().setAccessibleName(tipo.nombreAccesible + ": " + mensaje);
            setVisible(true);
            revalidar();
        }

        public void ocultar() {
            if (isVisible()) {
                setVisible(false);
                revalidar();
            }
        }

        private void revalidar() {
            Container padre = getParent();
            (padre == null ? this : padre).revalidate();
            repaint();
        }
    }

    /** Etiqueta pequeña (chip) con radio 6 e icono opcional. */
    public static Caja chip(String texto, Color fondo, Color colorTexto, Iconos.Nombre icono, Color colorIcono) {
        Caja chip = new Caja(fondo, null, RADIO_CHIP, 0, 0, 0);
        chip.setRelleno(XXS, XS, XXS, XS);
        JLabel etiqueta = etiqueta(texto, LEYENDA, colorTexto);
        if (icono != null) {
            etiqueta.setIcon(Iconos.icono(icono, colorIcono, 14));
            etiqueta.setIconTextGap(XXS);
        }
        chip.agregar(etiqueta);
        return chip;
    }

    public static Caja chip(String texto, Color fondo, Color colorTexto) {
        return chip(texto, fondo, colorTexto, null, colorTexto);
    }

    /** Chip de estado con el color semántico del tipo y su icono. */
    public static Caja chipEstado(String texto, TipoAlerta tipo) {
        return chip(texto, tipo.fondo, tipo.texto, tipo.icono, tipo.relleno);
    }

    /** Icono dentro de un cuadro de color suave (radio 10). */
    public static class IconoEnCaja extends JComponent {

        private Iconos.Nombre icono;
        private Color fondo;
        private Color color;
        private final int tamano;
        private final int tamanoIcono;

        public IconoEnCaja(Iconos.Nombre icono, Color fondo, Color color, int tamano) {
            this(icono, fondo, color, tamano, tamano / 2);
        }

        public IconoEnCaja(Iconos.Nombre icono, Color fondo, Color color, int tamano, int tamanoIcono) {
            this.icono = icono;
            this.fondo = fondo;
            this.color = color;
            this.tamano = tamano;
            this.tamanoIcono = tamanoIcono;
            Dimension dimension = new Dimension(tamano, tamano);
            setPreferredSize(dimension);
            setMinimumSize(dimension);
            setMaximumSize(dimension);
        }

        public void cambiar(Iconos.Nombre icono, Color fondo, Color color) {
            this.icono = icono;
            this.fondo = fondo;
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = suave(graphics);
            g.setColor(fondo);
            g.fill(new RoundRectangle2D.Float(0, 0, tamano, tamano, 2f * RADIO_CAMPO, 2f * RADIO_CAMPO));
            int inicio = (tamano - tamanoIcono) / 2;
            Iconos.pintar(g, icono, color, inicio, inicio, tamanoIcono);
            g.dispose();
        }
    }

    /** Mensaje centrado para cuando todavía no hay datos, con una acción opcional para salir de ese estado. */
    public static Caja estadoVacio(Iconos.Nombre icono, String titulo, String texto, JComponent accion) {
        Caja caja = new Caja(null, null, RADIO_TARJETA, 0, 0, XL);
        caja.agregarCentrado(new IconoEnCaja(icono, AZUL_SUAVE, AZUL_ACCION, 56), 0);
        caja.agregar(new Texto(titulo, TITULO, TEXTO, SwingConstants.CENTER), M);
        caja.agregar(new Texto(texto, CUERPO, TEXTO_SECUNDARIO, SwingConstants.CENTER), XXS);
        if (accion != null) {
            caja.agregarCentrado(accion, L);
        }
        return caja;
    }

    /** Fila etiqueta / valor. */
    public static JPanel filaDato(String etiqueta, String valor) {
        JPanel fila = new JPanel(new BorderLayout(M, 0));
        fila.setOpaque(false);
        fila.setBorder(new EmptyBorder(6, 0, 6, 0));
        fila.add(etiqueta(etiqueta, CUERPO, TEXTO_SECUNDARIO), BorderLayout.WEST);
        JLabel dato = etiqueta(valor, CUERPO_SEMINEGRITA, TEXTO);
        dato.setHorizontalAlignment(SwingConstants.RIGHT);
        fila.add(dato, BorderLayout.EAST);
        return fila;
    }

    /** Separador de 1 px #D3DCEA. */
    public static JComponent lineaDivisoria() {
        JComponent linea = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(DIVISOR);
                g.fillRect(0, 0, getWidth(), 1);
            }
        };
        linea.setPreferredSize(new Dimension(1, 1));
        return linea;
    }

    /**
     * Diálogo de confirmación modal con resumen de datos y dos acciones. En las
     * acciones destructivas el botón es rojo y el foco inicial queda en
     * "Cancelar", para que Enter no borre por accidente.
     *
     * @return {@code true} si el usuario confirma
     */
    public static boolean confirmar(Component padre, String titulo, String descripcion, List<String[]> filas,
            String textoConfirmar, boolean destructiva) {
        Window ventana = SwingUtilities.getWindowAncestor(padre);
        JDialog dialogo = new JDialog(ventana, titulo, Dialog.ModalityType.APPLICATION_MODAL);
        dialogo.setUndecorated(true);
        GraphicsDevice pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        boolean translucido = pantalla.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
        int margen = translucido ? MARGEN_SOMBRA : 0;
        if (translucido) {
            dialogo.setBackground(new Color(0, 0, 0, 0));
            dialogo.getRootPane().setOpaque(false);
        }

        boolean[] confirmado = {false};
        Caja panel = new Caja(BLANCO, DIVISOR, RADIO_PANEL, translucido ? 4 : 0, margen, L);
        JPanel encabezado = new JPanel(new BorderLayout(S, 0));
        encabezado.setOpaque(false);
        encabezado.add(enCentro(new IconoEnCaja(destructiva ? Iconos.Nombre.ADVERTENCIA : Iconos.Nombre.INFO,
                destructiva ? ERROR_FONDO : AZUL_SUAVE, destructiva ? ERROR_RELLENO : AZUL_ACCION, 40)),
                BorderLayout.WEST);
        encabezado.add(etiqueta(titulo, TITULO, TEXTO), BorderLayout.CENTER);
        panel.agregar(encabezado);
        panel.agregar(new Texto(descripcion, CUERPO, TEXTO_SECUNDARIO), M);
        for (int i = 0; i < filas.size(); i++) {
            panel.agregar(filaDato(filas.get(i)[0], filas.get(i)[1]), i == 0 ? S : 0);
        }
        Boton cancelar = new Boton("Cancelar", EstiloBoton.TEXTO, null);
        Boton aceptar = new Boton(textoConfirmar, destructiva ? EstiloBoton.PELIGRO : EstiloBoton.PRIMARIO, null);
        cancelar.addActionListener(e -> dialogo.dispose());
        aceptar.addActionListener(e -> {
            confirmado[0] = true;
            dialogo.dispose();
        });
        JPanel acciones = new JPanel();
        acciones.setOpaque(false);
        acciones.setLayout(new BoxLayout(acciones, BoxLayout.X_AXIS));
        acciones.add(Box.createHorizontalGlue());
        acciones.add(cancelar);
        acciones.add(Box.createHorizontalStrut(XS));
        acciones.add(aceptar);
        panel.agregar(acciones, L);

        dialogo.setContentPane(panel);
        if (!destructiva) {
            dialogo.getRootPane().setDefaultButton(aceptar);
        }
        dialogo.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                (destructiva ? cancelar : aceptar).requestFocusInWindow();
            }
        });
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.pack();
        dialogo.setSize(Math.max(dialogo.getWidth(), 460 + 2 * margen), dialogo.getHeight());
        dialogo.pack();
        dialogo.setLocationRelativeTo(ventana);

        // Velo azul oscuro sobre la ventana mientras el diálogo está abierto
        JRootPane raiz = SwingUtilities.getRootPane(padre);
        Component vidrioAnterior = raiz == null ? null : raiz.getGlassPane();
        if (raiz != null) {
            JComponent velo = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    g.setColor(new Color(AZUL_OSCURO.getRed(), AZUL_OSCURO.getGreen(), AZUL_OSCURO.getBlue(), 90));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
            raiz.setGlassPane(velo);
            velo.setVisible(true);
        }
        dialogo.setVisible(true);
        if (raiz != null) {
            raiz.getGlassPane().setVisible(false);
            raiz.setGlassPane(vidrioAnterior);
        }
        return confirmado[0];
    }

    // ================================================================ Soporte

    /** Copia del contexto gráfico con suavizado de bordes y de texto. */
    static Graphics2D suave(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        Object pistasTexto = Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
        if (pistasTexto instanceof Map<?, ?> pistas) {
            g.addRenderingHints(pistas);
        } else {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        return g;
    }
}
