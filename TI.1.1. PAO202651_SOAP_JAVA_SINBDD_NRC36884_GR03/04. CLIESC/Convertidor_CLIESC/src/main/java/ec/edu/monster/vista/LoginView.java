package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Pantalla de autenticación con identidad visual Azul. */
public class LoginView extends JFrame {

    private static final String FUENTE = "Plus Jakarta Sans";
    private static final Color AZUL_MARCA = new Color(0x2F6FEB);
    private static final Color AZUL_ACCION = new Color(0x1D56CC);
    private static final Color AZUL_HOVER = new Color(0x1744A3);
    private static final Color AZUL_PRESIONADO = new Color(0x153A82);
    private static final Color AZUL_SUAVE = new Color(0xEDF4FF);
    private static final Color BORDE_SUAVE = new Color(0xB8D2FF);
    private static final Color TEXTO = new Color(0x141C31);
    private static final Color TEXTO_SECUNDARIO = new Color(0x4A5A78);
    private static final Color PLACEHOLDER = new Color(0x627393);
    private static final Color BORDE = new Color(0xB1BFD4);
    private static final Color DIVISOR = new Color(0xD3DCEA);
    private static final Color FONDO_SECCION = new Color(0xE9EEF6);
    private static final Color SUPERFICIE = new Color(0xF5F8FC);
    private static final Color AZUL_OSCURO = new Color(0x0B1D44);
    private static final Color AZUL_FOCO = new Color(0x5B93F7);
    private static final Color ERROR_TEXTO = new Color(0xC0392B);
    private static final Color ERROR_FONDO = new Color(0xFCEBE8);
    private static final Color INFO_TEXTO = new Color(0x1744A3);
    private static final Color INFO_FONDO = new Color(0xEDF4FF);

    /* Foto del panel izquierdo, empaquetada en src/main/resources/imagenes */
    private static final String RUTA_IMAGEN = "/imagenes/fotoLogin.jpeg";

    private final JTextField campoUsuario = new JTextField();
    private final JPasswordField campoClave = new JPasswordField();
    private final JButton botonIngresar = new JButton("Iniciar sesión");
    private final JLabel mensaje = new JLabel(" ", SwingConstants.CENTER);

    public LoginView() {
        super("Conversión de Unidades Monster | Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 600));
        setSize(1180, 760);
        setResizable(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel pagina = new JPanel(new GridBagLayout());
        pagina.setBackground(FONDO_SECCION);
        pagina.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createLineBorder(DIVISOR, 1, true));
        tarjeta.setPreferredSize(new Dimension(1080, 660));
        tarjeta.setMinimumSize(new Dimension(850, 540));
        tarjeta.add(crearPanelVisual(), BorderLayout.WEST);
        tarjeta.add(crearFormulario(), BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        pagina.add(tarjeta, gbc);
        setContentPane(pagina);
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelVisual() {
        PanelVisual panel = new PanelVisual(RUTA_IMAGEN);
        panel.setPreferredSize(new Dimension(460, 660));
        panel.setMinimumSize(new Dimension(300, 400));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        JPanel sello = new JPanel(new BorderLayout());
        sello.setOpaque(false);
        sello.setBorder(BorderFactory.createEmptyBorder(28, 30, 0, 30));
        JLabel marca = new JLabel("MONSTER");
        marca.setForeground(Color.WHITE);
        marca.setFont(new Font(FUENTE, Font.BOLD, 14));
        sello.add(marca, BorderLayout.WEST);
        panel.add(sello, BorderLayout.NORTH);

        JPanel pie = new JPanel();
        pie.setOpaque(false);
        pie.setLayout(new javax.swing.BoxLayout(pie, javax.swing.BoxLayout.Y_AXIS));
        JLabel titulo = new JLabel("Convierte unidades");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font(FUENTE, Font.BOLD, 27));
        JLabel detalle = new JLabel("de forma sencilla y segura");
        detalle.setForeground(new Color(0xB8D2FF));
        detalle.setFont(new Font(FUENTE, Font.PLAIN, 16));
        titulo.setAlignmentX(LEFT_ALIGNMENT);
        detalle.setAlignmentX(LEFT_ALIGNMENT);
        pie.add(titulo);
        pie.add(javax.swing.Box.createVerticalStrut(8));
        pie.add(detalle);
        pie.setBorder(BorderFactory.createEmptyBorder(0, 38, 24, 30));
        panel.add(pie, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SUPERFICIE);
        panel.setBorder(BorderFactory.createEmptyBorder(36, 56, 32, 56));

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new javax.swing.BoxLayout(contenido, javax.swing.BoxLayout.Y_AXIS));

        JLabel sobretitulo = new JLabel("ACCESO SEGURO");
        sobretitulo.setAlignmentX(CENTER_ALIGNMENT);
        sobretitulo.setForeground(AZUL_ACCION);
        sobretitulo.setFont(new Font(FUENTE, Font.BOLD, 12));

        JLabel titulo = new JLabel("Sistema de conversión", SwingConstants.CENTER);
        titulo.setAlignmentX(CENTER_ALIGNMENT);
        titulo.setForeground(AZUL_OSCURO);
        titulo.setFont(new Font(FUENTE, Font.BOLD, 30));
        titulo.setBorder(BorderFactory.createEmptyBorder(8, 0, 3, 0));

        JLabel tituloDos = new JLabel("de unidades", SwingConstants.CENTER);
        tituloDos.setAlignmentX(CENTER_ALIGNMENT);
        tituloDos.setForeground(AZUL_OSCURO);
        tituloDos.setFont(new Font(FUENTE, Font.BOLD, 30));

        JLabel grupo = new JLabel("GRUPO #3", SwingConstants.CENTER);
        grupo.setAlignmentX(CENTER_ALIGNMENT);
        grupo.setForeground(TEXTO_SECUNDARIO);
        grupo.setFont(new Font(FUENTE, Font.PLAIN, 12));
        grupo.setBorder(BorderFactory.createEmptyBorder(7, 0, 5, 0));

        JLabel protocolo = new JLabel("Conexión mediante SOAP", SwingConstants.CENTER);
        protocolo.setAlignmentX(CENTER_ALIGNMENT);
        protocolo.setForeground(AZUL_ACCION);
        protocolo.setFont(new Font(FUENTE, Font.BOLD, 13));
        protocolo.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JPanel bienvenida = new JPanel(new BorderLayout(0, 4));
        bienvenida.setBackground(AZUL_SUAVE);
        bienvenida.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, AZUL_ACCION),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));
        JLabel saludo = new JLabel("¡Bienvenido!");
        saludo.setForeground(TEXTO);
        saludo.setFont(new Font(FUENTE, Font.BOLD, 14));
        JLabel descripcion = new JLabel("Inicia sesión para convertir tus unidades");
        descripcion.setForeground(TEXTO_SECUNDARIO);
        descripcion.setFont(new Font(FUENTE, Font.PLAIN, 13));
        bienvenida.add(saludo, BorderLayout.NORTH);
        bienvenida.add(descripcion, BorderLayout.CENTER);
        bienvenida.setAlignmentX(CENTER_ALIGNMENT);
        bienvenida.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        bienvenida.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 22, 0), bienvenida.getBorder()));

        contenido.add(sobretitulo);
        contenido.add(titulo);
        contenido.add(tituloDos);
        contenido.add(grupo);
        contenido.add(protocolo);
        contenido.add(bienvenida);
        contenido.add(crearCampo("Usuario", campoUsuario));
        contenido.add(javax.swing.Box.createVerticalStrut(14));
        contenido.add(crearCampo("Contraseña", campoClave));
        contenido.add(javax.swing.Box.createVerticalStrut(22));

        botonIngresar.setAlignmentX(CENTER_ALIGNMENT);
        botonIngresar.setPreferredSize(new Dimension(420, 48));
        botonIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        botonIngresar.setBackground(AZUL_ACCION);
        botonIngresar.setForeground(Color.WHITE);
        botonIngresar.setFont(new Font(FUENTE, Font.BOLD, 15));
        botonIngresar.setFocusPainted(false);
        botonIngresar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        botonIngresar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botonIngresar.getModel().addChangeListener(e -> {
            if (!botonIngresar.isEnabled()) botonIngresar.setBackground(FONDO_SECCION);
            else if (botonIngresar.getModel().isPressed()) botonIngresar.setBackground(AZUL_PRESIONADO);
            else if (botonIngresar.getModel().isRollover()) botonIngresar.setBackground(AZUL_HOVER);
            else botonIngresar.setBackground(AZUL_ACCION);
        });
        contenido.add(botonIngresar);

        JLabel pie = new JLabel("Cliente de escritorio Java Swing listo para conversión.", SwingConstants.CENTER);
        pie.setAlignmentX(CENTER_ALIGNMENT);
        pie.setForeground(TEXTO_SECUNDARIO);
        pie.setFont(new Font(FUENTE, Font.PLAIN, 12));
        pie.setBorder(BorderFactory.createEmptyBorder(17, 0, 6, 0));
        contenido.add(pie);

        mensaje.setAlignmentX(CENTER_ALIGNMENT);
        mensaje.setForeground(INFO_TEXTO);
        mensaje.setFont(new Font(FUENTE, Font.PLAIN, 12));
        mensaje.setOpaque(true);
        mensaje.setBackground(INFO_FONDO);
        mensaje.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        mensaje.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        contenido.add(mensaje);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(contenido, gbc);
        return panel;
    }

    private JPanel crearCampo(String etiqueta, JTextField campo) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);
        panel.setAlignmentX(CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        JLabel label = new JLabel(etiqueta);
        label.setForeground(TEXTO);
        label.setFont(new Font(FUENTE, Font.BOLD, 14));
        campo.setFont(new Font(FUENTE, Font.PLAIN, 15));
        campo.setForeground(TEXTO);
        campo.setCaretColor(AZUL_ACCION);
        campo.setBackground(Color.WHITE);
        campo.setPreferredSize(new Dimension(420, 48));
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(0, 14, 0, 14)));
        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AZUL_FOCO, 2),
                        BorderFactory.createEmptyBorder(0, 13, 0, 13)));
            }
            @Override public void focusLost(FocusEvent e) {
                campo.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDE, 1),
                        BorderFactory.createEmptyBorder(0, 14, 0, 14)));
            }
        });
        panel.add(label, BorderLayout.NORTH);
        panel.add(campo, BorderLayout.CENTER);
        return panel;
    }

    private static final class PanelVisual extends JPanel {
        private final String ruta;
        private final java.awt.Image imagen;

        private PanelVisual(String ruta) {
            this.ruta = ruta;
            java.net.URL recurso = ruta == null ? null : LoginView.class.getResource(ruta);
            imagen = recurso == null ? null : new javax.swing.ImageIcon(recurso).getImage();
            setOpaque(true);
            setBackground(AZUL_OSCURO);
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            if (imagen != null && imagen.getWidth(this) > 0) {
                // Escala tipo "cover": llena el panel sin deformar la foto
                int iw = imagen.getWidth(this);
                int ih = imagen.getHeight(this);
                double escala = Math.max((double) w / iw, (double) h / ih);
                int dw = (int) Math.ceil(iw * escala);
                int dh = (int) Math.ceil(ih * escala);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(imagen, (w - dw) / 2, (h - dh) / 2, dw, dh, this);
                // Degradado oscuro arriba y abajo para que se lean los textos blancos
                g.setPaint(new java.awt.GradientPaint(0, 0, new Color(11, 29, 68, 150), 0, h / 4f, new Color(11, 29, 68, 0)));
                g.fillRect(0, 0, w, h / 4);
                int inicioPie = h * 11 / 20;
                g.setPaint(new java.awt.GradientPaint(0, inicioPie, new Color(11, 29, 68, 0), 0, h, new Color(11, 29, 68, 230)));
                g.fillRect(0, inicioPie, w, h - inicioPie);
            } else {
                g.setColor(AZUL_OSCURO);
                g.fillRect(0, 0, w, h);
                g.setColor(AZUL_ACCION);
                g.fillRoundRect(w / 9, h / 3, w * 7 / 9, h / 3, 24, 24);
                g.setColor(AZUL_MARCA);
                g.fillOval(w / 4, h / 3 + 28, 72, 72);
                g.setColor(new Color(0x12A9C9));
                g.fillOval(w * 2 / 3, h / 2, 42, 42);
                g.setColor(BORDE_SUAVE);
                g.drawRoundRect(w / 9, h / 3, w * 7 / 9, h / 3, 24, 24);
            }
            g.dispose();
        }
    }

    public JTextField getCampoUsuario() { return campoUsuario; }
    public JPasswordField getCampoClave() { return campoClave; }
    public JButton getBotonIngresar() { return botonIngresar; }
    public JLabel getMensaje() { return mensaje; }

    public void mostrarMensaje(String texto, boolean error) {
        mensaje.setText(texto == null || texto.isBlank() ? " " : texto);
        mensaje.setForeground(error ? ERROR_TEXTO : INFO_TEXTO);
        mensaje.setBackground(error ? ERROR_FONDO : INFO_FONDO);
    }

    public void setProcesando(boolean procesando) {
        botonIngresar.setEnabled(!procesando);
        botonIngresar.setText(procesando ? "Conectando..." : "Iniciar sesión");
        campoUsuario.setEnabled(!procesando);
        campoClave.setEnabled(!procesando);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
            catch (Exception ignored) { }
            LoginView vista = new LoginView();
            new ec.edu.monster.controlador.LoginController(vista,
                    new ec.edu.monster.servicio.AutenticacionSoapService());
            vista.setVisible(true);
        });
    }
}
