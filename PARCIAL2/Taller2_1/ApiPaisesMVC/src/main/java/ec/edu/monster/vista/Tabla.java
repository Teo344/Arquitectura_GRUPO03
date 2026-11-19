package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
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
import java.util.function.ToLongFunction;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Tabla paginada con la identidad "Azul": filas de 44 px, encabezado gris
 * claro, fila resaltada al pasar el ratón y selección con barra lateral.
 * <p>
 * La paginación se hace aquí y no con un {@code TableRowSorter}: el filtro y
 * el orden se aplican sobre <b>todas</b> las filas y recién después se corta
 * la página. Así ordenar por población muestra los más poblados de toda la
 * lista (no solo los de la página actual) y el filtro busca en todas las
 * páginas. El pie muestra el rango visible, las filas por página (10, 25 o 50)
 * y los botones de página.
 *
 * @param <T> tipo de cada fila
 * @author carlo
 */
public final class Tabla<T> extends JPanel {

    /** Cómo se pinta el texto de una columna. */
    public enum Estilo {
        NORMAL,
        /** Seminegrita: la columna que identifica la fila. */
        DESTACADA,
        /** Gris secundario: datos de apoyo. */
        SECUNDARIA,
        /** Chip cian: códigos (p. ej. ISO). */
        CODIGO,
        /** Chip azul suave: categorías (p. ej. región). */
        ETIQUETA
    }

    /**
     * Definición de una columna.
     *
     * @param titulo     encabezado
     * @param texto      texto que se muestra (también se usa para filtrar)
     * @param comparador orden de la columna
     * @param alineacion SwingConstants.LEFT o RIGHT (los números, a la derecha)
     * @param ancho      ancho inicial en píxeles
     * @param minimo     ancho por debajo del cual la columna no se encoge
     * @param estilo     cómo se pinta
     */
    public record Columna<T>(String titulo, Function<T, String> texto, Comparator<T> comparador, int alineacion,
            int ancho, int minimo, Estilo estilo) {

        /** Texto ordenado con las reglas del español (sin distinguir tildes ni mayúsculas). */
        public static <T> Columna<T> texto(String titulo, Function<T, Object> valor, int ancho, int minimo,
                Estilo estilo) {
            Function<T, String> texto = fila -> Formato.texto(valor.apply(fila));
            Comparator<T> comparador = Comparator.comparing(fila -> {
                Object v = valor.apply(fila);
                return v == null ? null : v.toString();
            }, Comparator.nullsLast(COLADOR::compare));
            return new Columna<>(titulo, texto, comparador, SwingConstants.LEFT, ancho, minimo, estilo);
        }

        /** Número entero: alineado a la derecha, con separador de miles y nunca recortado. */
        public static <T> Columna<T> numero(String titulo, ToLongFunction<T> valor, int ancho) {
            return new Columna<>(titulo, fila -> Formato.entero(valor.applyAsLong(fila)),
                    Comparator.comparingLong(valor), SwingConstants.RIGHT, ancho, ancho, Estilo.NORMAL);
        }

        boolean esNumerica() {
            return alineacion == SwingConstants.RIGHT;
        }
    }

    private static final Collator COLADOR = Collator.getInstance(Locale.forLanguageTag("es"));
    private static final int[] TAMANOS_PAGINA = {10, 25, 50};
    private static final int TAMANO_ICONO_ACCION = 34;
    private static final int ANCHO_ACCIONES = 2 * TAMANO_ICONO_ACCION + XXS + M + L + XS;

    static {
        COLADOR.setStrength(Collator.PRIMARY);
    }

    private final List<Columna<T>> columnas;
    private final String sustantivoPlural;
    private final List<T> todas = new ArrayList<>();
    private final List<T> filtradas = new ArrayList<>();
    private final List<T> pagina = new ArrayList<>();
    private final Modelo modelo = new Modelo();
    private final JTable tabla;
    private final Paginador paginador;

    private String filtro = "";
    private int columnaOrden = -1;
    private boolean ascendente = true;
    private int numeroPagina = 0;
    private int tamanoPagina = TAMANOS_PAGINA[0];
    private int filaEncima = -1;
    private int accionEncima = -1;
    private T seleccionada;

    private Consumer<T> alActivar = fila -> {
    };
    private Consumer<T> alEditar;
    private Consumer<T> alEliminar;

    /**
     * @param nombreAccesible  nombre de la tabla para lectores de pantalla
     * @param sustantivoPlural cómo se llaman las filas en el pie ("países")
     * @param columnas         columnas de datos
     */
    public Tabla(String nombreAccesible, String sustantivoPlural, List<Columna<T>> columnas) {
        super(new BorderLayout());
        this.columnas = List.copyOf(columnas);
        this.sustantivoPlural = sustantivoPlural;
        int anchoMinimoTotal = this.columnas.stream().mapToInt(Columna::minimo).sum() + ANCHO_ACCIONES;
        this.tabla = new JTable(modelo) {
            // Las columnas se ajustan al ancho visible mientras quepan con su mínimo; si no, aparece la
            // barra horizontal: nunca se ocultan columnas sin aviso
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getParent() == null || getParent().getWidth() >= anchoMinimoTotal;
            }
        };
        setOpaque(false);

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

        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setReorderingAllowed(false);
        encabezado.setDefaultRenderer(new Encabezado());
        encabezado.setPreferredSize(new Dimension(0, 40));
        encabezado.setBackground(BLANCO);
        encabezado.setCursor(Componentes.MANO);
        encabezado.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int vista = encabezado.columnAtPoint(e.getPoint());
                if (vista >= 0 && vista < Tabla.this.columnas.size()) {
                    ordenarPor(vista, columnaOrden == vista ? !ascendente
                            : !Tabla.this.columnas.get(vista).esNumerica());
                }
            }
        });
        aplicarAnchos();

        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                cambiarEncima(fila, fila < 0 ? -1 : accionEn(e));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cambiarEncima(-1, -1);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila < 0 || e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                T elegida = pagina.get(fila);
                switch (accionEn(e)) {
                    case 0 -> alEditar.accept(elegida);
                    case 1 -> alEliminar.accept(elegida);
                    default -> alActivar.accept(elegida);
                }
            }
        };
        tabla.addMouseListener(raton);
        tabla.addMouseMotionListener(raton);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int fila = tabla.getSelectedRow();
                seleccionada = fila < 0 ? null : pagina.get(fila);
            }
        });
        registrarTeclas();

        JScrollPane desplazamiento = new JScrollPane(tabla);
        // Un borde propio (no null): con null el JTable impone "Table.scrollPaneBorder" del aspecto Metal
        desplazamiento.setBorder(new EmptyBorder(0, 0, 0, 0));
        desplazamiento.getViewport().setBackground(BLANCO);
        desplazamiento.setBackground(BLANCO);
        Componentes.estilizarBarra(desplazamiento);
        add(desplazamiento, BorderLayout.CENTER);

        paginador = new Paginador(TAMANOS_PAGINA, tamanoPagina, this::irAPagina, this::cambiarTamano);
        add(paginador, BorderLayout.SOUTH);
    }

    // ------------------------------------------------------------- configuración

    /**
     * Agrega la columna "Acciones" con los botones editar y eliminar de cada
     * fila. Con el teclado: Enter edita y Supr elimina la fila elegida.
     */
    public void conAcciones(Consumer<T> editar, Consumer<T> eliminar) {
        this.alEditar = Objects.requireNonNull(editar);
        this.alEliminar = Objects.requireNonNull(eliminar);
        modelo.fireTableStructureChanged();
        aplicarAnchos();
    }

    /** Acción al hacer clic en una fila (fuera de los botones) o pulsar Enter sobre ella. */
    public void alActivar(Consumer<T> accion) {
        this.alActivar = Objects.requireNonNull(accion);
    }

    public void ordenarPor(int columna, boolean ascendente) {
        this.columnaOrden = columna;
        this.ascendente = ascendente;
        recalcular(true);
        tabla.getTableHeader().repaint();
    }

    // ------------------------------------------------------------- datos

    /** Reemplaza las filas. Conserva el filtro, el orden y, si se puede, la página actual. */
    public void setFilas(List<T> nuevas) {
        todas.clear();
        todas.addAll(nuevas);
        seleccionada = null;
        recalcular(false);
    }

    /** Muestra solo las filas que contienen el texto en alguna columna (sin distinguir tildes ni mayúsculas). */
    public void setFiltro(String texto) {
        String nuevo = normalizar(texto);
        if (!nuevo.equals(filtro)) {
            filtro = nuevo;
            recalcular(true);
        }
    }

    public int totalFilas() {
        return todas.size();
    }

    /** Filas que pasan el filtro, en todas las páginas. */
    public int filasVisibles() {
        return filtradas.size();
    }

    // ------------------------------------------------------------- selección

    public Optional<T> getSeleccion() {
        return Optional.ofNullable(seleccionada);
    }

    /** Selecciona la primera fila que cumple el criterio, pasando a su página. */
    public void seleccionar(Predicate<T> criterio) {
        for (int i = 0; i < filtradas.size(); i++) {
            if (criterio.test(filtradas.get(i))) {
                numeroPagina = i / tamanoPagina;
                mostrarPagina();
                int fila = i % tamanoPagina;
                tabla.setRowSelectionInterval(fila, fila);
                tabla.scrollRectToVisible(tabla.getCellRect(fila, 0, true));
                return;
            }
        }
    }

    public void limpiarSeleccion() {
        tabla.clearSelection();
        seleccionada = null;
    }

    public void enfocar() {
        tabla.requestFocusInWindow();
    }

    // ------------------------------------------------------------- paginación

    private void recalcular(boolean volverAlInicio) {
        filtradas.clear();
        for (T fila : todas) {
            if (coincide(fila)) {
                filtradas.add(fila);
            }
        }
        if (columnaOrden >= 0) {
            Comparator<T> orden = columnas.get(columnaOrden).comparador();
            filtradas.sort(ascendente ? orden : orden.reversed());
        }
        if (volverAlInicio) {
            numeroPagina = 0;
        }
        mostrarPagina();
    }

    private void mostrarPagina() {
        numeroPagina = Math.max(0, Math.min(numeroPagina, totalPaginas() - 1));
        T anterior = seleccionada;
        int desde = numeroPagina * tamanoPagina;
        pagina.clear();
        pagina.addAll(filtradas.subList(Math.min(desde, filtradas.size()),
                Math.min(desde + tamanoPagina, filtradas.size())));
        modelo.fireTableDataChanged();
        filaEncima = -1;
        // Si la fila elegida sigue en esta página, se conserva resaltada
        int indice = anterior == null ? -1 : pagina.indexOf(anterior);
        if (indice >= 0) {
            tabla.setRowSelectionInterval(indice, indice);
        }
        tabla.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        paginador.actualizar(numeroPagina, totalPaginas(), desde, pagina.size(), filtradas.size(), sustantivoPlural);
    }

    private int totalPaginas() {
        return Math.max(1, (filtradas.size() + tamanoPagina - 1) / tamanoPagina);
    }

    private void irAPagina(int nueva) {
        if (nueva != numeroPagina && nueva >= 0 && nueva < totalPaginas()) {
            numeroPagina = nueva;
            mostrarPagina();
        }
    }

    private void cambiarTamano(int tamano) {
        // Se mantiene a la vista la primera fila que se estaba viendo
        int primera = numeroPagina * tamanoPagina;
        tamanoPagina = tamano;
        numeroPagina = primera / tamano;
        mostrarPagina();
    }

    private boolean coincide(T fila) {
        if (filtro.isEmpty()) {
            return true;
        }
        for (Columna<T> columna : columnas) {
            if (normalizar(columna.texto().apply(fila)).contains(filtro)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------- eventos

    private boolean tieneAcciones() {
        return alEditar != null;
    }

    /** 0 = editar, 1 = eliminar, -1 = fuera de los botones. */
    private int accionEn(MouseEvent e) {
        int columna = tabla.columnAtPoint(e.getPoint());
        int fila = tabla.rowAtPoint(e.getPoint());
        if (!tieneAcciones() || columna != columnas.size() || fila < 0) {
            return -1;
        }
        Rectangle celda = tabla.getCellRect(fila, columna, false);
        int x = e.getX() - celda.x;
        int finEliminar = celda.width - L;
        int inicioEliminar = finEliminar - TAMANO_ICONO_ACCION;
        int inicioEditar = inicioEliminar - XXS - TAMANO_ICONO_ACCION;
        if (x >= inicioEliminar && x < finEliminar) {
            return 1;
        }
        return x >= inicioEditar && x < inicioEliminar ? 0 : -1;
    }

    private void cambiarEncima(int fila, int accion) {
        if (fila != filaEncima || accion != accionEncima) {
            filaEncima = fila;
            accionEncima = accion;
            tabla.setCursor(accion >= 0 ? Componentes.MANO : null);
            tabla.repaint();
        }
    }

    private void registrarTeclas() {
        tecla(KeyEvent.VK_ENTER, "activar", () -> getSeleccion().ifPresent(fila -> {
            if (tieneAcciones()) {
                alEditar.accept(fila);
            } else {
                alActivar.accept(fila);
            }
        }));
        tecla(KeyEvent.VK_DELETE, "eliminar", () -> {
            if (tieneAcciones()) {
                getSeleccion().ifPresent(alEliminar);
            }
        });
        tecla(KeyEvent.VK_PAGE_DOWN, "pagina-siguiente", () -> irAPagina(numeroPagina + 1));
        tecla(KeyEvent.VK_PAGE_UP, "pagina-anterior", () -> irAPagina(numeroPagina - 1));
    }

    private void tecla(int codigo, String nombre, Runnable accion) {
        tabla.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(codigo, 0), nombre);
        tabla.getActionMap().put(nombre, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                accion.run();
            }
        });
    }

    private void aplicarAnchos() {
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            TableColumn columna = tabla.getColumnModel().getColumn(i);
            if (i < columnas.size()) {
                columna.setPreferredWidth(columnas.get(i).ancho());
                columna.setMinWidth(columnas.get(i).minimo());
            } else {
                columna.setPreferredWidth(ANCHO_ACCIONES);
                columna.setMinWidth(ANCHO_ACCIONES);
                columna.setMaxWidth(ANCHO_ACCIONES);
            }
        }
    }

    // ------------------------------------------------------------- modelo y pintura

    private final class Modelo extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return pagina.size();
        }

        @Override
        public int getColumnCount() {
            return columnas.size() + (tieneAcciones() ? 1 : 0);
        }

        @Override
        public String getColumnName(int columna) {
            return columna < columnas.size() ? columnas.get(columna).titulo() : "Acciones";
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            return columna < columnas.size() ? columnas.get(columna).texto().apply(pagina.get(fila)) : null;
        }
    }

    /** Celda: relleno 16, divisor inferior, chips para códigos y categorías, y los botones de acción. */
    private final class Celda extends DefaultTableCellRenderer {

        private boolean elegida;
        private boolean primera;
        private boolean acciones;
        private int fila;
        private String chip;
        private Color fondoChip;
        private Color textoChip;

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean elegida, boolean foco,
                int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, elegida, false, fila, columna);
            this.elegida = elegida;
            this.primera = columna == 0;
            this.fila = fila;
            this.acciones = columna >= columnas.size();
            this.chip = null;
            setBackground(elegida ? AZUL_SUAVE : fila == filaEncima ? SUPERFICIE : BLANCO);
            setBorder(new EmptyBorder(0, primera ? L : M, 0, M));
            setToolTipText(null);
            if (acciones) {
                setText("");
                return this;
            }
            Columna<T> definicion = columnas.get(columna);
            String texto = valor == null ? "" : valor.toString();
            setHorizontalAlignment(definicion.alineacion());
            setFont(definicion.estilo() == Estilo.DESTACADA ? CUERPO_SEMINEGRITA : CUERPO);
            setForeground(definicion.estilo() == Estilo.SECUNDARIA || "—".equals(texto) ? TEXTO_SECUNDARIO : TEXTO);
            if ((definicion.estilo() == Estilo.CODIGO || definicion.estilo() == Estilo.ETIQUETA) && !"—".equals(texto)) {
                chip = texto;
                boolean codigo = definicion.estilo() == Estilo.CODIGO;
                fondoChip = codigo ? CIAN_SUAVE : AZUL_SUAVE;
                textoChip = codigo ? CIAN_TEXTO : AZUL_HOVER;
                setText("");
            } else {
                setText(texto);
                int anchoTexto = getFontMetrics(getFont()).stringWidth(texto) + 2 * M;
                setToolTipText(anchoTexto > tabla.getColumnModel().getColumn(columna).getWidth() ? texto : null);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = Componentes.suave(graphics);
            g.setColor(FONDO_SECCION);
            g.fillRect(0, getHeight() - 1, getWidth(), 1);
            if (elegida && primera) {
                g.setColor(AZUL_ACCION);
                g.fillRect(0, 0, 3, getHeight());
            }
            if (chip != null) {
                pintarChip(g);
            }
            if (acciones) {
                pintarAcciones(g);
            }
            g.dispose();
        }

        private void pintarChip(Graphics2D g) {
            g.setFont(LEYENDA_NEGRITA);
            FontMetrics medidas = g.getFontMetrics();
            int ancho = medidas.stringWidth(chip) + 2 * XS;
            int alto = 22;
            int x = getInsets().left;
            int y = (getHeight() - alto) / 2;
            g.setColor(fondoChip);
            g.fill(new RoundRectangle2D.Float(x, y, ancho, alto, 2f * RADIO_CHIP, 2f * RADIO_CHIP));
            g.setColor(textoChip);
            g.drawString(chip, x + XS, y + (alto - medidas.getHeight()) / 2 + medidas.getAscent());
        }

        private void pintarAcciones(Graphics2D g) {
            int finEliminar = getWidth() - L;
            int inicioEliminar = finEliminar - TAMANO_ICONO_ACCION;
            int inicioEditar = inicioEliminar - XXS - TAMANO_ICONO_ACCION;
            int y = (getHeight() - TAMANO_ICONO_ACCION) / 2;
            pintarBoton(g, inicioEditar, y, 0, Iconos.Nombre.EDITAR, AZUL_ACCION, AZUL_SUAVE);
            pintarBoton(g, inicioEliminar, y, 1, Iconos.Nombre.ELIMINAR, ERROR_RELLENO, ERROR_FONDO);
        }

        private void pintarBoton(Graphics2D g, int x, int y, int accion, Iconos.Nombre icono, Color color,
                Color fondoEncima) {
            if (fila == filaEncima && accion == accionEncima) {
                g.setColor(fondoEncima);
                g.fill(new RoundRectangle2D.Float(x, y, TAMANO_ICONO_ACCION, TAMANO_ICONO_ACCION, 16, 16));
            }
            int margen = (TAMANO_ICONO_ACCION - 18) / 2;
            Iconos.pintar(g, icono, color, x + margen, y + margen, 18);
        }
    }

    /** Encabezado: sobretítulo gris, línea inferior y flecha de orden (no solo color). */
    private final class Encabezado implements TableCellRenderer {

        private final JLabel etiqueta = new JLabel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics g = graphics.create();
                g.setColor(BLANCO);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(DIVISOR);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
                g.dispose();
                super.paintComponent(graphics);
            }
        };

        Encabezado() {
            etiqueta.setFont(SOBRETITULO);
            etiqueta.setHorizontalTextPosition(SwingConstants.LEFT);
            etiqueta.setIconTextGap(XXS);
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean elegida, boolean foco,
                int fila, int columna) {
            boolean deAcciones = columna >= columnas.size();
            String titulo = deAcciones ? "Acciones" : columnas.get(columna).titulo();
            etiqueta.setText(titulo.toUpperCase(Locale.ROOT));
            etiqueta.setBorder(new EmptyBorder(0, columna == 0 ? L : M, 0, deAcciones ? L : M));
            etiqueta.setHorizontalAlignment(deAcciones || columnas.get(columna).esNumerica()
                    ? SwingConstants.RIGHT : SwingConstants.LEFT);
            etiqueta.setIcon(null);
            etiqueta.setForeground(TEXTO_SECUNDARIO);
            String orden = "";
            if (columna == columnaOrden) {
                etiqueta.setIcon(Iconos.icono(ascendente ? Iconos.Nombre.FLECHA_ARRIBA : Iconos.Nombre.FLECHA_ABAJO,
                        AZUL_ACCION, 14));
                etiqueta.setForeground(AZUL_HOVER);
                orden = ascendente ? ", orden ascendente" : ", orden descendente";
            }
            etiqueta.getAccessibleContext().setAccessibleName(titulo + orden);
            etiqueta.setToolTipText(deAcciones ? "Enter edita · Supr elimina"
                    : "Ordenar por " + titulo.toLowerCase(Locale.ROOT));
            return etiqueta;
        }
    }
}
