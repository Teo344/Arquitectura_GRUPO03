package ec.edu.monster.vista;

import ec.edu.monster.modelo.SesionUsuario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Menú principal del cliente con la identidad visual Azul. */
public class MenuView extends JFrame {
    private static final String FUENTE = "Plus Jakarta Sans";
    private static final Color AZUL_ACCION = new Color(0x1D56CC);
    private static final Color AZUL_HOVER = new Color(0x1744A3);
    private static final Color AZUL_SUAVE = new Color(0xEDF4FF);
    private static final Color BORDE_SUAVE = new Color(0xB8D2FF);
    private static final Color TEXTO = new Color(0x141C31);
    private static final Color SECUNDARIO = new Color(0x4A5A78);
    private static final Color DIVISOR = new Color(0xD3DCEA);
    private static final Color FONDO_SECCION = new Color(0xE9EEF6);
    private static final Color SUPERFICIE = new Color(0xF5F8FC);
    private static final Color AZUL_OSCURO = new Color(0x0B1D44);
    private static final Color CIAN = new Color(0x12A9C9);

    private final JButton botonConvertir = new JButton("Abrir conversor");
    private final JButton botonCatalogo = new JButton("Ver catálogo");
    private final JButton botonCerrarSesion = new JButton("Cerrar sesión");
    private final SesionUsuario sesion;

    public MenuView() { this(null); }

    public MenuView(SesionUsuario sesion) {
        super("Conversión de Unidades Monster | Menú Principal");
        this.sesion = sesion;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(SUPERFICIE);
        raiz.add(crearBarraSuperior(), BorderLayout.NORTH);

        JPanel area = new JPanel(new GridBagLayout());
        area.setBackground(SUPERFICIE);
        area.setBorder(BorderFactory.createEmptyBorder(42, 54, 30, 54));
        JPanel contenido = crearContenido();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        area.add(contenido, gbc);
        raiz.add(area, BorderLayout.CENTER);
        setContentPane(raiz);
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AZUL_OSCURO);
        barra.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        JLabel marca = new JLabel("MONSTER");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font(FUENTE, Font.BOLD, 18));
        barra.add(marca, BorderLayout.WEST);

        JPanel acciones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
        acciones.setOpaque(false);
        JLabel tipoCliente = new JLabel("Cliente de escritorio");
        tipoCliente.setForeground(new Color(0xB8D2FF));
        tipoCliente.setFont(new Font(FUENTE, Font.BOLD, 13));
        acciones.add(tipoCliente);
        botonCerrarSesion.setForeground(new Color(0xB8D2FF));
        botonCerrarSesion.setBackground(AZUL_OSCURO);
        botonCerrarSesion.setFont(new Font(FUENTE, Font.BOLD, 12));
        botonCerrarSesion.setFocusPainted(false);
        botonCerrarSesion.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB8D2FF), 1, true),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        botonCerrarSesion.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        acciones.add(botonCerrarSesion);
        barra.add(acciones, BorderLayout.EAST);
        return barra;
    }

    private JPanel crearContenido() {
        JPanel panel = new JPanel(new BorderLayout(0, 38));
        panel.setOpaque(false);

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new javax.swing.BoxLayout(encabezado, javax.swing.BoxLayout.Y_AXIS));
        JLabel sobre = new JLabel("MONSTER", SwingConstants.CENTER);
        sobre.setAlignmentX(CENTER_ALIGNMENT);
        sobre.setForeground(AZUL_ACCION);
        sobre.setFont(new Font(FUENTE, Font.BOLD, 12));
        JLabel titulo = new JLabel("¿Qué deseas hacer?", SwingConstants.CENTER);
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        titulo.setForeground(new Color(0x0B1D44));
        titulo.setFont(new Font(FUENTE, Font.BOLD, 34));
        titulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 5, 0));
        JLabel detalle = new JLabel("Elige una opción para continuar", SwingConstants.CENTER);
        detalle.setAlignmentX(CENTER_ALIGNMENT);
        detalle.setForeground(SECUNDARIO);
        detalle.setFont(new Font(FUENTE, Font.PLAIN, 16));
        encabezado.add(sobre);
        encabezado.add(titulo);
        encabezado.add(detalle);
        panel.add(encabezado, BorderLayout.NORTH);

        JPanel tarjetas = new JPanel(new GridLayout(1, 2, 32, 0));
        tarjetas.setOpaque(false);
        tarjetas.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        tarjetas.add(crearTarjeta("01", "Convertir unidades",
                "Realiza conversiones de longitud, masa y temperatura.",
                new Icono(Icono.Tipo.CONVERSION), botonConvertir, true));
        tarjetas.add(crearTarjeta("02", "Consultar catálogo",
                "Explora las categorías y unidades disponibles en el sistema.",
                new Icono(Icono.Tipo.CATALOGO), botonCatalogo, false));
        panel.add(tarjetas, BorderLayout.CENTER);

        JLabel pie = new JLabel("Conversión de Unidades Monster", SwingConstants.CENTER);
        pie.setForeground(SECUNDARIO);
        pie.setFont(new Font(FUENTE, Font.PLAIN, 12));
        panel.add(pie, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearTarjeta(String numero, String titulo, String descripcion,
                                Icono icono, JButton boton, boolean primaria) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 14));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVISOR, 1, true),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));

        JPanel cima = new JPanel(new BorderLayout());
        cima.setOpaque(false);
        cima.add(icono, BorderLayout.WEST);
        JLabel insignia = new JLabel(numero, SwingConstants.CENTER);
        insignia.setOpaque(true);
        insignia.setBackground(AZUL_SUAVE);
        insignia.setForeground(AZUL_ACCION);
        insignia.setFont(new Font(FUENTE, Font.BOLD, 13));
        insignia.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        cima.add(insignia, BorderLayout.EAST);
        tarjeta.add(cima, BorderLayout.NORTH);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new javax.swing.BoxLayout(textos, javax.swing.BoxLayout.Y_AXIS));
        JLabel encabezado = new JLabel(titulo);
        encabezado.setForeground(TEXTO);
        encabezado.setFont(new Font(FUENTE, Font.BOLD, 22));
        encabezado.setBorder(BorderFactory.createEmptyBorder(4, 0, 9, 0));
        JLabel descripcionLabel = new JLabel("<html><div style='width:300px'>" + descripcion + "</div></html>");
        descripcionLabel.setForeground(SECUNDARIO);
        descripcionLabel.setFont(new Font(FUENTE, Font.PLAIN, 15));
        textos.add(encabezado);
        textos.add(descripcionLabel);
        tarjeta.add(textos, BorderLayout.CENTER);

        boton.setFont(new Font(FUENTE, Font.BOLD, 15));
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(190, 48));
        boton.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        if (primaria) {
            boton.setBackground(AZUL_ACCION);
            boton.setForeground(Color.WHITE);
            boton.setBorderPainted(false);
            boton.getModel().addChangeListener(e -> boton.setBackground(
                    boton.getModel().isPressed() ? new Color(0x153A82)
                    : boton.getModel().isRollover() ? AZUL_HOVER : AZUL_ACCION));
        } else {
            boton.setBackground(AZUL_SUAVE);
            boton.setForeground(AZUL_HOVER);
            boton.setBorderPainted(true);
            boton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDE_SUAVE, 1, true),
                    BorderFactory.createEmptyBorder(9, 17, 9, 17)));
        }
        JPanel acciones = new JPanel(new BorderLayout());
        acciones.setOpaque(false);
        acciones.add(boton, BorderLayout.WEST);
        tarjeta.add(acciones, BorderLayout.SOUTH);
        return tarjeta;
    }

    private static final class Icono extends JPanel {
        enum Tipo { CONVERSION, CATALOGO }
        private final Tipo tipo;

        private Icono(Tipo tipo) {
            this.tipo = tipo;
            setOpaque(false);
            setPreferredSize(new Dimension(62, 62));
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(AZUL_SUAVE);
            g.fillRoundRect(2, 2, 58, 58, 16, 16);
            g.setColor(CIAN);
            g.setStroke(new java.awt.BasicStroke(3f, java.awt.BasicStroke.CAP_ROUND,
                    java.awt.BasicStroke.JOIN_ROUND));
            if (tipo == Tipo.CONVERSION) {
                g.drawLine(14, 23, 46, 23);
                g.drawLine(38, 16, 46, 23);
                g.drawLine(38, 30, 46, 23);
                g.drawLine(48, 39, 16, 39);
                g.drawLine(24, 32, 16, 39);
                g.drawLine(24, 46, 16, 39);
            } else {
                for (int y : new int[]{18, 30, 42}) {
                    g.fillOval(12, y - 2, 5, 5);
                    g.drawLine(24, y, 49, y);
                }
            }
            g.dispose();
        }
    }

    public JButton getBotonConvertir() { return botonConvertir; }
    public JButton getBotonCatalogo() { return botonCatalogo; }
    public JButton getBotonCerrarSesion() { return botonCerrarSesion; }
    public SesionUsuario getSesion() { return sesion; }
    public void mostrarAviso(String texto) {
        javax.swing.JOptionPane.showMessageDialog(this, texto, "Conversión de Unidades Monster",
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }
}
