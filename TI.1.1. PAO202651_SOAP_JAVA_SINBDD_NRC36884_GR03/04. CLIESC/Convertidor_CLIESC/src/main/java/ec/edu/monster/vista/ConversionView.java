package ec.edu.monster.vista;

import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.servicio.Categoria;
import ec.edu.monster.servicio.Unidad;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/** Conversor de unidades con formulario e historial de la sesión actual. */
public class ConversionView extends JFrame {
    private static final String FUENTE = "Plus Jakarta Sans";
    private static final Color AZUL_ACCION = new Color(0x1D56CC);
    private static final Color AZUL_HOVER = new Color(0x1744A3);
    private static final Color AZUL_PRESIONADO = new Color(0x153A82);
    private static final Color AZUL_SUAVE = new Color(0xEDF4FF);
    private static final Color BORDE_SUAVE = new Color(0xB8D2FF);
    private static final Color TEXTO = new Color(0x141C31);
    private static final Color SECUNDARIO = new Color(0x4A5A78);
    private static final Color PLACEHOLDER = new Color(0x627393);
    private static final Color BORDE = new Color(0xB1BFD4);
    private static final Color DIVISOR = new Color(0xD3DCEA);
    private static final Color FONDO_SECCION = new Color(0xE9EEF6);
    private static final Color SUPERFICIE = new Color(0xF5F8FC);
    private static final Color AZUL_OSCURO = new Color(0x0B1D44);
    private static final Color CIAN = new Color(0x12A9C9);
    private static final Color EXITO_TEXTO = new Color(0x0F7A52);
    private static final Color EXITO_FONDO = new Color(0xE7F6EF);
    private static final Color ERROR_TEXTO = new Color(0xC0392B);
    private static final Color ERROR_FONDO = new Color(0xFCEBE8);

    private final SesionUsuario sesion;
    private final JComboBox<Categoria> comboCategoria = new JComboBox<>();
    private final JTextField campoValor = new JTextField("0");
    private final JComboBox<Unidad> comboOrigen = new JComboBox<>();
    private final JComboBox<Unidad> comboDestino = new JComboBox<>();
    private final JButton botonConvertir = new JButton("Convertir");
    private final JButton botonLimpiar = new JButton("Limpiar");
    private final JButton botonVolver = new JButton("Volver al inicio");
    private final JLabel etiquetaEstado = new JLabel("Listo para convertir", SwingConstants.CENTER);
    private final DefaultListModel<String> historial = new DefaultListModel<>();
    private final JList<String> listaHistorial = new JList<>(historial);

    public ConversionView(SesionUsuario sesion) {
        super("Conversión de Unidades Monster | Conversor");
        this.sesion = sesion;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(940, 620));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(SUPERFICIE);
        raiz.add(crearBarraSuperior(), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBackground(SUPERFICIE);
        contenido.setBorder(BorderFactory.createEmptyBorder(38, 42, 34, 42));
        JPanel paneles = new JPanel(new GridLayout(1, 2, 28, 0));
        paneles.setOpaque(false);
        paneles.add(crearFormulario());
        paneles.add(crearHistorial());
        paneles.setPreferredSize(new Dimension(1220, 590));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        contenido.add(paneles, gbc);
        raiz.add(contenido, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AZUL_OSCURO);
        barra.setBorder(BorderFactory.createEmptyBorder(13, 28, 13, 28));
        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new javax.swing.BoxLayout(titulos, javax.swing.BoxLayout.Y_AXIS));
        JLabel marca = new JLabel("MONSTER");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font(FUENTE, Font.BOLD, 16));
        JLabel detalle = new JLabel("Conversor de unidades · longitud, masa y temperatura");
        detalle.setForeground(new Color(0xB8D2FF));
        detalle.setFont(new Font(FUENTE, Font.PLAIN, 12));
        titulos.add(marca);
        titulos.add(detalle);
        barra.add(titulos, BorderLayout.WEST);

        estiloSecundario(botonVolver);
        barra.add(botonVolver, BorderLayout.EAST);
        return barra;
    }

    private JPanel crearFormulario() {
        JPanel tarjeta = crearTarjeta();
        tarjeta.setLayout(new BorderLayout(0, 20));
        tarjeta.add(tituloTarjeta("Conversor de unidades", "Realiza una conversión con las opciones del servidor."),
                BorderLayout.NORTH);

        JPanel campos = new JPanel(new GridLayout(4, 1, 0, 14));
        campos.setOpaque(false);
        campos.add(crearCampo("Tipo de conversión", comboCategoria));
        campos.add(crearCampo("Valor", campoValor));
        JPanel unidades = new JPanel(new GridLayout(1, 2, 14, 0));
        unidades.setOpaque(false);
        unidades.add(crearCampo("Unidad inicial", comboOrigen));
        unidades.add(crearCampo("Unidad final", comboDestino));
        campos.add(unidades);

        JPanel botones = new JPanel(new GridLayout(1, 2, 12, 0));
        botones.setOpaque(false);
        estiloPrimario(botonConvertir);
        estiloSecundario(botonLimpiar);
        botones.add(botonConvertir);
        botones.add(botonLimpiar);
        campos.add(botones);

        tarjeta.add(campos, BorderLayout.CENTER);
        etiquetaEstado.setOpaque(true);
        etiquetaEstado.setFont(new Font(FUENTE, Font.BOLD, 13));
        etiquetaEstado.setForeground(EXITO_TEXTO);
        etiquetaEstado.setBackground(EXITO_FONDO);
        etiquetaEstado.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        tarjeta.add(etiquetaEstado, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearHistorial() {
        JPanel tarjeta = crearTarjeta();
        tarjeta.setLayout(new BorderLayout(0, 16));
        tarjeta.add(tituloTarjeta("Historial de conversiones", "Resultados obtenidos en esta sesión."),
                BorderLayout.NORTH);

        listaHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaHistorial.setFont(new Font(FUENTE, Font.PLAIN, 14));
        listaHistorial.setForeground(TEXTO);
        listaHistorial.setBackground(Color.WHITE);
        listaHistorial.setFixedCellHeight(64);
        listaHistorial.setCellRenderer(new DefaultListCellRenderer() {
            @Override public java.awt.Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                setText("<html><div style='padding:5px 2px'>" + value + "</div></html>");
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DIVISOR));
                setBackground(selected ? AZUL_SUAVE : Color.WHITE);
                setForeground(TEXTO);
                return this;
            }
        });
        JScrollPane scroll = new JScrollPane(listaHistorial);
        scroll.setBorder(BorderFactory.createLineBorder(BORDE, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        tarjeta.add(scroll, BorderLayout.CENTER);

        JLabel nota = new JLabel("El historial se conserva mientras esta ventana esté abierta.");
        nota.setForeground(SECUNDARIO);
        nota.setFont(new Font(FUENTE, Font.PLAIN, 12));
        tarjeta.add(nota, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjeta() {
        JPanel tarjeta = new JPanel();
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVISOR, 1, true),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        return tarjeta;
    }

    private JPanel tituloTarjeta(String titulo, String descripcion) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        JLabel encabezado = new JLabel(titulo);
        encabezado.setForeground(AZUL_OSCURO);
        encabezado.setFont(new Font(FUENTE, Font.BOLD, 22));
        JLabel detalle = new JLabel(descripcion);
        detalle.setForeground(SECUNDARIO);
        detalle.setFont(new Font(FUENTE, Font.PLAIN, 13));
        detalle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        panel.add(encabezado);
        panel.add(detalle);
        return panel;
    }

    private JPanel crearCampo(String titulo, java.awt.Component control) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);
        JLabel label = new JLabel(titulo);
        label.setForeground(TEXTO);
        label.setFont(new Font(FUENTE, Font.BOLD, 14));
        control.setFont(new Font(FUENTE, Font.PLAIN, 14));
        control.setPreferredSize(new Dimension(180, 48));
        if (control instanceof JComboBox<?> combo) {
            combo.setBackground(Color.WHITE);
            combo.setForeground(TEXTO);
            combo.setRenderer(new DefaultListCellRenderer() {
                @Override public java.awt.Component getListCellRendererComponent(JList<?> list, Object value,
                        int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    if (value instanceof Categoria categoria) setText(categoria.getNombre());
                    else if (value instanceof Unidad unidad) setText(unidad.getNombre() + " (" + unidad.getSimbolo() + ")");
                    return this;
                }
            });
        } else if (control instanceof JTextField campo) {
            campo.setForeground(TEXTO);
            campo.setCaretColor(AZUL_ACCION);
            campo.setBackground(Color.WHITE);
            campo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE, 1),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            campo.setToolTipText("Escribe el valor numérico que deseas convertir");
        }
        panel.add(label, BorderLayout.NORTH);
        panel.add(control, BorderLayout.CENTER);
        return panel;
    }

    private void estiloPrimario(JButton boton) {
        boton.setBackground(AZUL_ACCION);
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font(FUENTE, Font.BOLD, 15));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(150, 48));
        boton.getModel().addChangeListener(e -> boton.setBackground(
                boton.getModel().isPressed() ? AZUL_PRESIONADO
                : boton.getModel().isRollover() ? AZUL_HOVER : AZUL_ACCION));
    }

    private void estiloSecundario(JButton boton) {
        boton.setBackground(AZUL_SUAVE);
        boton.setForeground(AZUL_HOVER);
        boton.setFont(new Font(FUENTE, Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_SUAVE, 1, true),
                BorderFactory.createEmptyBorder(9, 15, 9, 15)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public SesionUsuario getSesion() { return sesion; }
    public JComboBox<Categoria> getComboCategoria() { return comboCategoria; }
    public JTextField getCampoValor() { return campoValor; }
    public JComboBox<Unidad> getComboOrigen() { return comboOrigen; }
    public JComboBox<Unidad> getComboDestino() { return comboDestino; }
    public JButton getBotonConvertir() { return botonConvertir; }
    public JButton getBotonLimpiar() { return botonLimpiar; }
    public JButton getBotonVolver() { return botonVolver; }

    public void mostrarUnidades(List<Unidad> unidades) {
        comboOrigen.removeAllItems();
        comboDestino.removeAllItems();
        for (Unidad unidad : unidades) {
            comboOrigen.addItem(unidad);
            comboDestino.addItem(unidad);
        }
    }

    public void mostrarCategorias(List<Categoria> categorias) {
        comboCategoria.removeAllItems();
        for (Categoria categoria : categorias) comboCategoria.addItem(categoria);
    }

    public void agregarAlHistorial(String entrada) { historial.insertElementAt(entrada, 0); }

    public void mostrarEstado(String texto, boolean error) {
        etiquetaEstado.setText(texto);
        etiquetaEstado.setBackground(error ? ERROR_FONDO : EXITO_FONDO);
        etiquetaEstado.setForeground(error ? ERROR_TEXTO : EXITO_TEXTO);
    }

    public void setProcesando(boolean procesando) {
        botonConvertir.setEnabled(!procesando);
        comboCategoria.setEnabled(!procesando);
        comboOrigen.setEnabled(!procesando);
        comboDestino.setEnabled(!procesando);
        campoValor.setEnabled(!procesando);
        if (procesando) mostrarEstado("Consultando el servidor...", false);
    }
}
