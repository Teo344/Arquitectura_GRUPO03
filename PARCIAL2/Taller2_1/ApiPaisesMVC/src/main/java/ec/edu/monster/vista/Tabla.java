package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

/**
 * Tabla de datos con la identidad "Azul": filas de 44 px, divisores de 1 px,
 * fila resaltada al pasar el ratón, selección con fondo y barra lateral, y
 * columnas ordenables con un indicador visible. Trabaja con objetos del
 * modelo (no con arreglos), y ordena los textos con las reglas del español.
 *
 * @param <T> tipo de cada fila
 * @author carlo
 */
public final class Tabla<T> extends JPanel {

    /**
     * Definición de una columna.
     *
     * @param titulo        encabezado
     * @param tipo          clase del valor (decide cómo se ordena)
     * @param valor         valor de la fila para esta columna
     * @param formato       cómo se muestra el valor
     * @param alineacion    SwingConstants.LEFT o RIGHT (los números, a la derecha)
     * @param anchoPreferido ancho inicial en píxeles
     * @param anchoMinimo   ancho por debajo del cual la columna no se encoge
     * @param destacada     texto en seminegrita (la columna que identifica la fila)
     */
    public record Columna<T>(String titulo, Class<?> tipo, Function<T, Object> valor,
            Function<Object, String> formato, int alineacion, int anchoPreferido, int anchoMinimo,
            boolean destacada) {

        /** Texto: si no cabe se recorta con "…" y la celda muestra el valor completo en un tooltip. */
        public static <T> Columna<T> texto(String titulo, Function<T, Object> valor, int ancho, int minimo,
                boolean destacada) {
            return new Columna<>(titulo, String.class, valor, Formato::texto, SwingConstants.LEFT, ancho, minimo,
                    destacada);
        }

        /** Número: alineado a la derecha y nunca recortado (su ancho mínimo es el preferido). */
        public static <T> Columna<T> numero(String titulo, Function<T, Object> valor, Function<Object, String> formato,
                int ancho) {
            return new Columna<>(titulo, Long.class, valor, formato, SwingConstants.RIGHT, ancho, ancho, false);
        }
    }

    private static final Collator COLADOR = Collator.getInstance(Locale.forLanguageTag("es"));

    private final List<Columna<T>> columnas;
    private final List<T> filas = new ArrayList<>();
    // Se crean en el constructor: el JTable consulta las columnas al construirse
    private final Modelo modelo;
    private final JTable tabla;
    private final TableRowSorter<Modelo> ordenador;
    private int filaEncima = -1;
    private T seleccionActual;
    private boolean revirtiendo;
    private Predicate<T> puedeCambiarSeleccion = fila -> true;

    public Tabla(String nombreAccesible, List<Columna<T>> columnas) {
        super(new BorderLayout());
        this.columnas = List.copyOf(columnas);
        this.modelo = new Modelo();
        int anchoMinimoTotal = this.columnas.stream().mapToInt(Columna::anchoMinimo).sum();
        this.tabla = new JTable(modelo) {
            // Las columnas se ajustan al ancho visible mientras quepan con su mínimo; si no, aparece la
            // barra horizontal: nunca se ocultan columnas sin aviso
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getParent() == null || getParent().getWidth() >= anchoMinimoTotal;
            }
        };
        this.ordenador = new TableRowSorter<>(modelo);
        setOpaque(false);

        COLADOR.setStrength(Collator.PRIMARY);
        for (int i = 0; i < this.columnas.size(); i++) {
            if (this.columnas.get(i).tipo() == String.class) {
                ordenador.setComparator(i, Comparator.nullsLast(
                        Comparator.comparing(Object::toString, COLADOR::compare)));
            }
        }
        tabla.setRowSorter(ordenador);
        tabla.setRowHeight(ALTO_FILA);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(BLANCO);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tabla.setFont(CUERPO);
        tabla.setForeground(TEXTO);
        tabla.getAccessibleContext().setAccessibleName(nombreAccesible);
        tabla.setDefaultRenderer(Object.class, new Celda());
        tabla.setDefaultRenderer(Long.class, new Celda());

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setReorderingAllowed(false);
        encabezado.setDefaultRenderer(new Encabezado());
        encabezado.setPreferredSize(new Dimension(0, 40));
        encabezado.setBackground(BLANCO);

        for (int i = 0; i < this.columnas.size(); i++) {
            TableColumn columna = tabla.getColumnModel().getColumn(i);
            columna.setPreferredWidth(this.columnas.get(i).anchoPreferido());
            columna.setMinWidth(this.columnas.get(i).anchoMinimo());
        }

        tabla.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                cambiarFilaEncima(tabla.rowAtPoint(e.getPoint()));
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                cambiarFilaEncima(-1);
            }
        });

        JScrollPane desplazamiento = new JScrollPane(tabla);
        // Un borde propio (no null): con null el JTable impone "Table.scrollPaneBorder" del aspecto Metal
        desplazamiento.setBorder(new EmptyBorder(0, 0, 0, 0));
        desplazamiento.getViewport().setBackground(BLANCO);
        desplazamiento.setBackground(BLANCO);
        Componentes.estilizarBarra(desplazamiento);
        add(desplazamiento, BorderLayout.CENTER);
    }

    // ------------------------------------------------------------- datos

    public void setFilas(List<T> nuevas) {
        filas.clear();
        filas.addAll(nuevas);
        revirtiendo = true;
        modelo.fireTableDataChanged();
        revirtiendo = false;
        seleccionActual = null;
        filaEncima = -1;
    }

    public int totalFilas() {
        return filas.size();
    }

    public int filasVisibles() {
        return tabla.getRowCount();
    }

    // ------------------------------------------------------------- selección

    public Optional<T> getSeleccion() {
        int vista = tabla.getSelectedRow();
        return vista < 0 ? Optional.empty() : Optional.of(filas.get(tabla.convertRowIndexToModel(vista)));
    }

    /** Selecciona la primera fila que cumple el criterio y la hace visible. */
    public void seleccionar(Predicate<T> criterio) {
        for (int i = 0; i < filas.size(); i++) {
            if (criterio.test(filas.get(i))) {
                int vista = tabla.convertRowIndexToView(i);
                if (vista >= 0) {
                    revirtiendo = true;
                    tabla.setRowSelectionInterval(vista, vista);
                    revirtiendo = false;
                    seleccionActual = filas.get(i);
                    tabla.scrollRectToVisible(tabla.getCellRect(vista, 0, true));
                }
                return;
            }
        }
    }

    public void limpiarSeleccion() {
        revirtiendo = true;
        tabla.clearSelection();
        revirtiendo = false;
        seleccionActual = null;
    }

    /**
     * Permite vetar un cambio de selección (por ejemplo, si hay cambios sin
     * guardar y el usuario decide conservarlos).
     */
    public void setPuedeCambiarSeleccion(Predicate<T> criterio) {
        this.puedeCambiarSeleccion = Objects.requireNonNull(criterio);
    }

    /** Acción cuando el usuario elige otra fila; recibe {@code null} si la selección se vacía. */
    public void alSeleccionar(Consumer<T> accion) {
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || revirtiendo) {
                return;
            }
            T nueva = getSeleccion().orElse(null);
            if (Objects.equals(nueva, seleccionActual)) {
                return;
            }
            if (!puedeCambiarSeleccion.test(nueva)) {
                T anterior = seleccionActual;
                if (anterior == null) {
                    limpiarSeleccion();
                } else {
                    seleccionar(fila -> fila.equals(anterior));
                }
                return;
            }
            seleccionActual = nueva;
            accion.accept(nueva);
        });
    }

    // ------------------------------------------------------------- filtro y teclado

    /** Muestra solo las filas que contienen el texto en alguna columna (sin distinguir tildes ni mayúsculas). */
    public void setFiltro(String texto) {
        String buscado = normalizar(texto);
        if (buscado.isEmpty()) {
            ordenador.setRowFilter(null);
            return;
        }
        ordenador.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends Modelo, ? extends Integer> entrada) {
                T fila = filas.get(entrada.getIdentifier());
                for (Columna<T> columna : columnas) {
                    if (normalizar(columna.formato().apply(columna.valor().apply(fila))).contains(buscado)) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /** Acción para la tecla Supr sobre la tabla. */
    public void alSuprimir(Runnable accion) {
        tabla.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "suprimir");
        tabla.getActionMap().put("suprimir", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accion.run();
            }
        });
    }

    public void ordenarPor(int columna) {
        ordenador.setSortKeys(List.of(new RowSorter.SortKey(columna, SortOrder.ASCENDING)));
    }

    public void enfocar() {
        tabla.requestFocusInWindow();
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    private void cambiarFilaEncima(int fila) {
        if (fila != filaEncima) {
            filaEncima = fila;
            tabla.repaint();
        }
    }

    // ------------------------------------------------------------- modelo y pintura

    private final class Modelo extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.size();
        }

        @Override
        public String getColumnName(int columna) {
            return columnas.get(columna).titulo();
        }

        @Override
        public Class<?> getColumnClass(int columna) {
            return columnas.get(columna).tipo();
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            return columnas.get(columna).valor().apply(filas.get(fila));
        }
    }

    /** Celda: relleno 12, divisor inferior y barra azul de 3 px a la izquierda de la fila elegida. */
    private final class Celda extends DefaultTableCellRenderer {

        private boolean seleccionada;
        private boolean primera;

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean elegida, boolean foco,
                int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, elegida, false, fila, columna);
            Columna<T> definicion = columnas.get(tabla.convertColumnIndexToModel(columna));
            String texto = definicion.formato().apply(valor);
            setText(texto);
            setFont(definicion.destacada() ? CUERPO_SEMINEGRITA : CUERPO);
            setForeground(valor == null ? PLACEHOLDER : TEXTO);
            setHorizontalAlignment(definicion.alineacion());
            setBorder(new EmptyBorder(0, S, 0, S));
            setBackground(elegida ? AZUL_SUAVE : fila == filaEncima ? SUPERFICIE : BLANCO);
            int anchoTexto = getFontMetrics(getFont()).stringWidth(texto) + 2 * S;
            setToolTipText(anchoTexto > tabla.getColumnModel().getColumn(columna).getWidth() ? texto : null);
            this.seleccionada = elegida;
            this.primera = columna == 0;
            return this;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics g = graphics.create();
            g.setColor(DIVISOR);
            g.fillRect(0, getHeight() - 1, getWidth(), 1);
            if (seleccionada && primera) {
                g.setColor(AZUL_ACCION);
                g.fillRect(0, 0, 3, getHeight());
            }
            g.dispose();
        }
    }

    /** Encabezado: sobretítulo gris, línea inferior de 1.5 px y flecha de orden (no solo color). */
    private final class Encabezado implements TableCellRenderer {

        private final JLabel etiqueta = new JLabel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = Componentes.suave(graphics);
                g.setColor(BLANCO);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(BORDE_CAMPO);
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g.dispose();
                super.paintComponent(graphics);
            }
        };

        Encabezado() {
            etiqueta.setFont(SOBRETITULO);
            etiqueta.setForeground(TEXTO_SECUNDARIO);
            etiqueta.setBorder(new EmptyBorder(0, S, 0, S));
            etiqueta.setHorizontalTextPosition(SwingConstants.LEFT);
            etiqueta.setIconTextGap(XXS);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean elegida, boolean foco,
                int fila, int columna) {
            int indice = tabla.convertColumnIndexToModel(columna);
            Columna<T> definicion = columnas.get(indice);
            etiqueta.setText(definicion.titulo().toUpperCase(Locale.ROOT));
            etiqueta.setHorizontalAlignment(definicion.alineacion());
            etiqueta.setIcon(null);
            etiqueta.setForeground(TEXTO_SECUNDARIO);
            String orden = "";
            for (RowSorter.SortKey clave : ordenador.getSortKeys()) {
                if (clave.getColumn() == indice && clave.getSortOrder() != SortOrder.UNSORTED) {
                    boolean ascendente = clave.getSortOrder() == SortOrder.ASCENDING;
                    etiqueta.setIcon(Iconos.icono(ascendente ? Iconos.Nombre.FLECHA_ARRIBA : Iconos.Nombre.FLECHA_ABAJO,
                            AZUL_ACCION, 14));
                    etiqueta.setForeground(AZUL_HOVER);
                    orden = ascendente ? ", orden ascendente" : ", orden descendente";
                }
            }
            etiqueta.getAccessibleContext().setAccessibleName(definicion.titulo() + orden);
            etiqueta.setToolTipText("Ordenar por " + definicion.titulo().toLowerCase(Locale.ROOT));
            return etiqueta;
        }
    }
}
