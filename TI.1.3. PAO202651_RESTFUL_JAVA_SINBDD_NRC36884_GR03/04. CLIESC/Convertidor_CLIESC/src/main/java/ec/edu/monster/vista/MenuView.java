package ec.edu.monster.vista;

import ec.edu.monster.modelo.Sesion;
import java.awt.*;
import javax.swing.*;

/** Menú principal del cliente RESTful con la identidad visual Azul. */
public final class MenuView extends JFrame {
    private final Sesion sesion;
    private final JButton convertir = new JButton("Abrir conversor");
    private final JButton catalogo = new JButton("Ver catálogo");
    private final JButton salir = new JButton("Cerrar sesión");

    public MenuView(Sesion sesion) {
        super("Conversión de Unidades Monster | Menú Principal");
        this.sesion = sesion;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Estilo.FONDO);
        root.add(createToolbar(), BorderLayout.NORTH);

        JPanel area = new JPanel(new GridBagLayout());
        area.setBackground(Estilo.FONDO);
        area.setBorder(BorderFactory.createEmptyBorder(42, 54, 30, 54));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        area.add(createContent(), constraints);
        root.add(area, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createToolbar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Estilo.OSCURO);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 28));
        JLabel brand = new JLabel("MONSTER");
        brand.setForeground(Color.WHITE);
        brand.setFont(Estilo.font(18, true));
        bar.add(brand, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        JLabel client = new JLabel("Cliente de escritorio");
        client.setForeground(new Color(0xB8D2FF));
        client.setFont(Estilo.font(13, true));
        actions.add(client);
        salir.setForeground(new Color(0xB8D2FF));
        salir.setBackground(Estilo.OSCURO);
        salir.setFont(Estilo.font(12, true));
        salir.setFocusPainted(false);
        salir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        salir.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xB8D2FF), 1, true),
                BorderFactory.createEmptyBorder(9, 14, 9, 14)));
        actions.add(salir);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel createContent() {
        JPanel panel = new JPanel(new BorderLayout(0, 38));
        panel.setOpaque(false);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(centered("MONSTER", 12, true, Estilo.AZUL));
        JLabel title = centered("¿Qué deseas hacer?", 34, true, Estilo.OSCURO);
        title.setBorder(BorderFactory.createEmptyBorder(8, 0, 5, 0));
        heading.add(title);
        heading.add(centered("Elige una opción para continuar", 16, false, Estilo.SECUNDARIO));
        panel.add(heading, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 2, 40, 0));
        cards.setOpaque(false);
        cards.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        cards.add(createCard("01", "Convertir unidades",
                "Realiza conversiones de longitud, masa y temperatura.", new MenuIcon(true), convertir, true));
        cards.add(createCard("02", "Consultar catálogo",
                "Explora las categorías y unidades disponibles en el sistema.", new MenuIcon(false), catalogo, false));
        panel.add(cards, BorderLayout.CENTER);

        JLabel footer = centered("Conversión de Unidades Monster", 12, false, Estilo.SECUNDARIO);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createCard(String number, String title, String description,
                              JComponent icon, JButton action, boolean primary) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilo.DIVISOR, 1, true),
                BorderFactory.createEmptyBorder(32, 32, 30, 32)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(icon, BorderLayout.WEST);
        JLabel badge = new JLabel(number, SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setBackground(Estilo.SUAVE);
        badge.setForeground(Estilo.AZUL);
        badge.setFont(Estilo.font(13, true));
        badge.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        top.add(badge, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        JLabel heading = new JLabel(title);
        heading.setForeground(Estilo.TEXTO);
        heading.setFont(Estilo.font(22, true));
        heading.setBorder(BorderFactory.createEmptyBorder(12, 0, 9, 0));
        JLabel detail = new JLabel("<html><div style='width:300px'>" + description + "</div></html>");
        detail.setForeground(Estilo.SECUNDARIO);
        detail.setFont(Estilo.font(15, false));
        copy.add(heading);
        copy.add(detail);
        card.add(copy, BorderLayout.CENTER);

        Estilo.button(action, primary);
        action.setPreferredSize(new Dimension(190, 60));
        JPanel buttons = new JPanel(new BorderLayout());
        buttons.setOpaque(false);
        buttons.add(action, BorderLayout.WEST);
        card.add(buttons, BorderLayout.SOUTH);
        return card;
    }

    private JLabel centered(String text, int size, boolean bold, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(Estilo.font(size, bold));
        label.setForeground(color);
        return label;
    }

    private static final class MenuIcon extends JPanel {
        private final boolean conversion;
        MenuIcon(boolean conversion) {
            this.conversion = conversion;
            setOpaque(false);
            setPreferredSize(new Dimension(74, 74));
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Estilo.SUAVE);
            g.fillRoundRect(2, 2, 70, 70, 16, 16);
            g.setColor(Estilo.CIAN);
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (conversion) {
                g.drawLine(19, 29, 55, 29); g.drawLine(47, 21, 55, 29); g.drawLine(47, 37, 55, 29);
                g.drawLine(55, 47, 19, 47); g.drawLine(27, 39, 19, 47); g.drawLine(27, 55, 19, 47);
            } else {
                for (int y : new int[]{24, 37, 50}) {
                    g.fillOval(17, y - 2, 5, 5); g.drawLine(30, y, 57, y);
                }
            }
            g.dispose();
        }
    }

    public Sesion sesion() { return sesion; }
    public JButton convertir() { return convertir; }
    public JButton catalogo() { return catalogo; }
    public JButton salir() { return salir; }
}
