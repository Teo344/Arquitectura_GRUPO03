package ec.edu.monster.vista;

import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.servicio.Categoria;
import ec.edu.monster.servicio.Unidad;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/** Catálogo de categorías y unidades consultadas desde el backend SOAP. */
public class CatalogoView extends JFrame {
    private static final String FUENTE = "Plus Jakarta Sans";
    private static final Color AZUL_ACCION = new Color(0x1D56CC);
    private static final Color AZUL_HOVER = new Color(0x1744A3);
    private static final Color AZUL_SUAVE = new Color(0xEDF4FF);
    private static final Color BORDE_SUAVE = new Color(0xB8D2FF);
    private static final Color TEXTO = new Color(0x141C31);
    private static final Color SECUNDARIO = new Color(0x4A5A78);
    private static final Color DIVISOR = new Color(0xD3DCEA);
    private static final Color SUPERFICIE = new Color(0xF5F8FC);
    private static final Color AZUL_OSCURO = new Color(0x0B1D44);
    private static final Color ACENTO_TEXTO = new Color(0x0B6B83);

    private final SesionUsuario sesion;
    private final JPanel tarjetas = new JPanel(new GridLayout(0, 2, 14, 14));
    private final JPanel listaUnidades = new JPanel(new GridLayout(0, 1, 0, 9));
    private final JLabel tituloDetalle = new JLabel("Selecciona una categoría");
    private final JLabel estado = new JLabel("El catálogo se consultará al servidor.");
    private final JButton botonVolver = new JButton("Volver al inicio");
    private final Map<Categoria, JButton> botonesCategoria = new LinkedHashMap<>();

    public CatalogoView(SesionUsuario sesion) {
        super("Conversión de Unidades Monster | Catálogo");
        this.sesion = sesion;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(940, 620));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel root = new JPanel(new BorderLayout(0, 22));
        root.setBackground(SUPERFICIE);
        root.add(crearBarraSuperior(), BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new GridLayout(1, 2, 24, 0));
        cuerpo.setOpaque(false);
        cuerpo.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));
        cuerpo.add(crearPanelCategorias());
        cuerpo.add(crearPanelDetalle());
        root.add(cuerpo, BorderLayout.CENTER);

        estado.setForeground(SECUNDARIO);
        estado.setFont(new Font(FUENTE, Font.PLAIN, 13));
        estado.setBorder(BorderFactory.createEmptyBorder(0, 36, 12, 36));
        root.add(estado, BorderLayout.SOUTH);
        setContentPane(root);
    }

    /** Barra compartida visualmente con la pantalla del convertidor. */
    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AZUL_OSCURO);
        barra.setBorder(BorderFactory.createEmptyBorder(13, 28, 13, 28));
        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new javax.swing.BoxLayout(titulos, javax.swing.BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("MONSTER · Catálogo de unidades");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(FUENTE, Font.BOLD, 17));
        JLabel subtitulo = new JLabel("Consulta las categorías y unidades disponibles");
        subtitulo.setForeground(new Color(0xB8D2FF));
        subtitulo.setFont(new Font(FUENTE, Font.PLAIN, 12));
        titulos.add(titulo);
        titulos.add(subtitulo);
        barra.add(titulos, BorderLayout.WEST);

        botonVolver.setFont(new Font(FUENTE, Font.BOLD, 13));
        botonVolver.setForeground(AZUL_HOVER);
        botonVolver.setBackground(AZUL_SUAVE);
        botonVolver.setFocusPainted(false);
        botonVolver.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE_SUAVE, 1, true),
                BorderFactory.createEmptyBorder(9, 15, 9, 15)));
        botonVolver.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        barra.add(botonVolver, BorderLayout.EAST);
        return barra;
    }

    private JPanel crearPanelCategorias() {
        JPanel panel = panelBlanco();
        panel.setLayout(new BorderLayout(0, 14));
        panel.add(tituloPanel("Categorías"), BorderLayout.NORTH);
        tarjetas.setOpaque(false);
        JScrollPane scroll = new JScrollPane(tarjetas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelDetalle() {
        JPanel panel = panelBlanco();
        panel.setLayout(new BorderLayout(0, 14));
        JPanel cabecera = new JPanel(new BorderLayout(0, 6));
        cabecera.setOpaque(false);
        tituloDetalle.setForeground(new Color(0x0B1D44));
        tituloDetalle.setFont(new Font(FUENTE, Font.BOLD, 22));
        JLabel texto = new JLabel("Unidades registradas para esta categoría");
        texto.setForeground(SECUNDARIO);
        texto.setFont(new Font(FUENTE, Font.PLAIN, 13));
        cabecera.add(tituloDetalle, BorderLayout.NORTH);
        cabecera.add(texto, BorderLayout.SOUTH);
        panel.add(cabecera, BorderLayout.NORTH);

        listaUnidades.setOpaque(false);
        JScrollPane scroll = new JScrollPane(listaUnidades);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);
        mostrarMensajeUnidades("Elige una tarjeta para consultar las unidades de esa categoría.");
        return panel;
    }

    private JPanel panelBlanco() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVISOR, 1, true),
                BorderFactory.createEmptyBorder(22, 22, 22, 22)));
        return panel;
    }

    private JLabel tituloPanel(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(SECUNDARIO);
        label.setFont(new Font(FUENTE, Font.BOLD, 14));
        return label;
    }

    public void mostrarCategorias(List<Categoria> categorias, ActionListener accion) {
        tarjetas.removeAll();
        botonesCategoria.clear();
        int indice = 1;
        for (Categoria categoria : categorias) {
            boolean convertible = Boolean.TRUE.equals(categoria.isConvertible());
            String tipo = convertible ? "CONVERSIÓN DISPONIBLE" : "SOLO CONSULTA";
            String descripcion = descripcion(categoria.getCodigo());
            JButton boton = new JButton("<html><div style='width:180px'>"
                    + "<span style='color:#1D56CC;font-size:11px;font-weight:bold'>" + String.format("%02d", indice++)
                    + " &nbsp; " + tipo + "</span><br><br>"
                    + "<span style='font-size:19px;font-weight:bold;color:#0B1D44'>" + escapar(categoria.getNombre())
                    + "</span><br><br><span style='font-size:13px;color:#4A5A78'>" + descripcion
                    + "</span></div></html>");
            boton.setHorizontalAlignment(SwingConstants.LEFT);
            boton.setVerticalAlignment(SwingConstants.TOP);
            boton.setBackground(Color.WHITE);
            boton.setFocusPainted(false);
            boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(DIVISOR, 1, true),
                    BorderFactory.createEmptyBorder(16, 15, 12, 12)));
            boton.setActionCommand(categoria.getCodigo());
            boton.addActionListener(accion);
            tarjetas.add(boton);
            botonesCategoria.put(categoria, boton);
        }
        tarjetas.revalidate();
        tarjetas.repaint();
    }

    private String descripcion(String codigo) {
        if (codigo == null) return "Categoría del catálogo de unidades.";
        return switch (codigo.toUpperCase(java.util.Locale.ROOT)) {
            case "LONGITUD" -> "Distancias y medidas lineales.";
            case "MASA" -> "Medidas de masa y peso.";
            case "VOLUMEN" -> "Capacidad y espacio ocupado.";
            case "TIEMPO" -> "Unidades para medir duración.";
            case "TEMPERATURA" -> "Escalas de temperatura.";
            case "AREA" -> "Superficie y extensión.";
            default -> "Categoría del catálogo de unidades.";
        };
    }

    private String escapar(String texto) {
        return texto == null ? "" : texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void seleccionarCategoria(Categoria categoria) {
        tituloDetalle.setText(categoria.getNombre());
        for (Map.Entry<Categoria, JButton> entrada : botonesCategoria.entrySet()) {
            boolean seleccionada = entrada.getKey() == categoria;
            entrada.getValue().setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(seleccionada ? AZUL_ACCION : DIVISOR, seleccionada ? 2 : 1, true),
                    BorderFactory.createEmptyBorder(seleccionada ? 15 : 16, seleccionada ? 14 : 15,
                            seleccionada ? 11 : 12, seleccionada ? 11 : 12)));
        }
    }

    public void mostrarUnidades(List<Unidad> unidades) {
        listaUnidades.removeAll();
        if (unidades.isEmpty()) {
            mostrarMensajeUnidades("No hay unidades registradas para esta categoría.");
        } else {
            for (Unidad unidad : unidades) {
                JPanel fila = new JPanel(new BorderLayout(12, 0));
                fila.setBackground(SUPERFICIE);
                fila.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(DIVISOR, 1, true),
                        BorderFactory.createEmptyBorder(12, 14, 12, 14)));
                JPanel datos = new JPanel();
                datos.setOpaque(false);
                datos.setLayout(new javax.swing.BoxLayout(datos, javax.swing.BoxLayout.Y_AXIS));
                JLabel nombre = new JLabel(unidad.getNombre());
                nombre.setForeground(TEXTO);
                nombre.setFont(new Font(FUENTE, Font.BOLD, 15));
                JLabel codigo = new JLabel("Código: " + unidad.getCodigo());
                codigo.setForeground(SECUNDARIO);
                codigo.setFont(new Font(FUENTE, Font.PLAIN, 12));
                datos.add(nombre);
                datos.add(codigo);
                JLabel simbolo = new JLabel(unidad.getSimbolo(), SwingConstants.CENTER);
                simbolo.setForeground(ACENTO_TEXTO);
                simbolo.setFont(new Font(FUENTE, Font.BOLD, 18));
                simbolo.setPreferredSize(new Dimension(66, 38));
                fila.add(datos, BorderLayout.CENTER);
                fila.add(simbolo, BorderLayout.EAST);
                listaUnidades.add(fila);
            }
        }
        listaUnidades.revalidate();
        listaUnidades.repaint();
    }

    private void mostrarMensajeUnidades(String texto) {
        listaUnidades.removeAll();
        JLabel mensaje = new JLabel("<html><div style='width:300px;text-align:center'>" + texto + "</div></html>",
                SwingConstants.CENTER);
        mensaje.setForeground(SECUNDARIO);
        mensaje.setFont(new Font(FUENTE, Font.PLAIN, 14));
        listaUnidades.add(mensaje);
    }

    public SesionUsuario getSesion() { return sesion; }
    public JButton getBotonVolver() { return botonVolver; }
    public JLabel getEstado() { return estado; }

    public void mostrarEstado(String texto, boolean error) {
        estado.setText(texto);
        estado.setForeground(error ? new Color(0xC0392B) : SECUNDARIO);
    }
}
