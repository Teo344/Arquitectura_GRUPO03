package ec.edu.monster.vista;

import java.awt.*;
import javax.swing.*;

/** Acceso REST con la misma composición visual del cliente SOAP. */
public final class LoginView extends JFrame {
    private final JTextField usuario = new JTextField();
    private final JPasswordField clave = new JPasswordField();
    private final JButton ingresar = new JButton("Iniciar sesión");
    private final JLabel estado = new JLabel("Conéctate al servicio de conversiones", SwingConstants.CENTER);

    public LoginView() {
        super("Conversión de Unidades Monster | Iniciar sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 600)); setSize(1180, 760); setLocationRelativeTo(null);
        JPanel page = new JPanel(new GridBagLayout()); page.setBackground(Estilo.SECCION);
        page.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        JPanel card = new JPanel(new BorderLayout()); card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Estilo.DIVISOR)); card.setPreferredSize(new Dimension(1080, 660));
        card.add(visual(), BorderLayout.WEST); card.add(form(), BorderLayout.CENTER);
        GridBagConstraints layout = new GridBagConstraints();
        layout.gridx = 0; layout.gridy = 0; layout.weightx = 1; layout.weighty = 1;
        layout.fill = GridBagConstraints.BOTH;
        page.add(card, layout); setContentPane(page);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    private JPanel visual() {
        JPanel panel = new PanelVisual(new BorderLayout()); panel.setBackground(Estilo.OSCURO); panel.setPreferredSize(new Dimension(460, 660));
        JLabel brand = new JLabel("MONSTER"); brand.setForeground(Color.WHITE); brand.setFont(Estilo.font(14, true));
        brand.setBorder(BorderFactory.createEmptyBorder(28, 30, 0, 0)); panel.add(brand, BorderLayout.NORTH);
        JPanel bottom = new JPanel(); bottom.setOpaque(false); bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Convierte unidades"); title.setForeground(Color.WHITE); title.setFont(Estilo.font(27, true));
        JLabel subtitle = new JLabel("de forma sencilla y segura"); subtitle.setForeground(new Color(0xB8D2FF)); subtitle.setFont(Estilo.font(16, false));
        bottom.add(title); bottom.add(Box.createVerticalStrut(8)); bottom.add(subtitle);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 38, 34, 20)); panel.add(bottom, BorderLayout.SOUTH); return panel;
    }
    private JPanel form() {
        JPanel panel = new JPanel(new GridBagLayout()); panel.setBackground(Estilo.FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(36, 56, 32, 56));
        JPanel content = new JPanel(); content.setOpaque(false); content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel eyebrow = centered("ACCESO SEGURO", 12, true, Estilo.AZUL);
        JLabel title1 = centered("Sistema de conversión", 30, true, Estilo.OSCURO);
        JLabel title2 = centered("de unidades", 30, true, Estilo.OSCURO);
        JLabel group = centered("GRUPO #3", 12, false, Estilo.SECUNDARIO); group.setBorder(BorderFactory.createEmptyBorder(7, 0, 5, 0));
        JLabel protocol = centered("Conexión mediante REST", 13, true, Estilo.AZUL);
        protocol.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        JPanel welcome = new JPanel(new BorderLayout(0,4)); welcome.setBackground(Estilo.SUAVE);
        welcome.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,4,0,0,Estilo.AZUL),BorderFactory.createEmptyBorder(12,15,12,15)));
        JLabel greeting = new JLabel("¡Bienvenido!"); greeting.setFont(Estilo.font(14,true)); greeting.setForeground(Estilo.TEXTO);
        JLabel description = new JLabel("Inicia sesión para convertir tus unidades"); description.setFont(Estilo.font(13,false)); description.setForeground(Estilo.SECUNDARIO);
        welcome.add(greeting,BorderLayout.NORTH); welcome.add(description,BorderLayout.CENTER); welcome.setMaximumSize(new Dimension(Integer.MAX_VALUE,64));
        content.add(eyebrow); content.add(title1); content.add(title2); content.add(group); content.add(protocol); content.add(welcome);
        content.add(Box.createVerticalStrut(20)); content.add(input("Usuario", usuario)); content.add(Box.createVerticalStrut(14));
        content.add(input("Contraseña", clave)); content.add(Box.createVerticalStrut(22));
        Estilo.button(ingresar,true); ingresar.setAlignmentX(Component.CENTER_ALIGNMENT); ingresar.setMaximumSize(new Dimension(Integer.MAX_VALUE,48)); content.add(ingresar);
        JLabel foot = centered("Cliente de escritorio Java Swing listo para conversión.",12,false,Estilo.SECUNDARIO); foot.setBorder(BorderFactory.createEmptyBorder(17,0,6,0)); content.add(foot);
        estado.setAlignmentX(Component.CENTER_ALIGNMENT); estado.setFont(Estilo.font(12,false)); estado.setForeground(Estilo.AZUL);
        estado.setOpaque(true); estado.setBackground(Estilo.SUAVE); estado.setBorder(BorderFactory.createEmptyBorder(7,10,7,10)); estado.setMaximumSize(new Dimension(Integer.MAX_VALUE,36)); content.add(estado);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.fill=GridBagConstraints.HORIZONTAL; gbc.weightx=1; panel.add(content,gbc); return panel;
    }
    private JPanel input(String name, JTextField field) {
        JPanel p=new JPanel(new BorderLayout(0,7)); p.setOpaque(false); p.setMaximumSize(new Dimension(Integer.MAX_VALUE,76));
        JLabel label=new JLabel(name); label.setForeground(Estilo.TEXTO); label.setFont(Estilo.font(14,true)); Estilo.field(field);
        field.setPreferredSize(new Dimension(420,48)); p.add(label,BorderLayout.NORTH); p.add(field,BorderLayout.CENTER); return p;
    }
    private JLabel centered(String text,int size,boolean bold,Color color){ JLabel l=new JLabel(text,SwingConstants.CENTER); l.setAlignmentX(Component.CENTER_ALIGNMENT); l.setFont(Estilo.font(size,bold)); l.setForeground(color); return l; }

    /** Mantiene el mismo panel ilustrado del acceso SOAP y usa un fondo azul si la imagen no está disponible. */
    private static final class PanelVisual extends JPanel {
        private static final String IMAGE_PATH = "C:/Users/MSI/Pictures/MaterialVideos/estrella.gif";
        private final Image image;
        PanelVisual(LayoutManager layout) {
            super(layout);
            java.io.File file = new java.io.File(IMAGE_PATH);
            image = file.isFile() ? new ImageIcon(IMAGE_PATH).getImage() : null;
            setOpaque(true);
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (image != null && image.getWidth(this) > 0) {
                g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            } else {
                g.setPaint(new GradientPaint(0, 0, Estilo.OSCURO, 0, getHeight(), new Color(0x123A70)));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(new Color(0x1D56CC));
                g.fillOval(getWidth() / 3, getHeight() / 3, getWidth() / 2, getWidth() / 2);
                g.setColor(new Color(0x12A9C9));
                g.fillOval(getWidth() / 8, getHeight() / 2, 42, 42);
            }
            g.dispose();
        }
    }

    public JTextField usuario(){return usuario;} public JPasswordField clave(){return clave;} public JButton ingresar(){return ingresar;}
    public void estado(String text,boolean error){estado.setText(text);estado.setForeground(error?Estilo.ERROR:Estilo.EXITO);}
}
