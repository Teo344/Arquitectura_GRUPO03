package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JTextField;

/** Paleta y controles compartidos por las pantallas del cliente REST. */
public final class Estilo {
    private Estilo() {}
    public static final Color AZUL = new Color(0x1D56CC), OSCURO = new Color(0x0B1D44);
    public static final Color TEXTO = new Color(0x141C31), SECUNDARIO = new Color(0x4A5A78);
    public static final Color BORDE = new Color(0xB1BFD4), DIVISOR = new Color(0xD3DCEA);
    public static final Color SUAVE = new Color(0xEDF4FF), FONDO = new Color(0xF5F8FC);
    public static final Color SECCION = new Color(0xE9EEF6), CIAN = new Color(0x12A9C9);
    public static final Color EXITO = new Color(0x0F7A52), ERROR = new Color(0xC0392B);
    public static Font font(int size, boolean bold) { return new Font("Plus Jakarta Sans", bold ? Font.BOLD : Font.PLAIN, size); }
    public static void button(JButton b, boolean primary) {
        b.setFont(font(15, true)); b.setPreferredSize(new Dimension(190, 48));
        b.setBackground(primary ? AZUL : SUAVE); b.setForeground(primary ? Color.WHITE : new Color(0x1744A3));
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary ? AZUL : new Color(0xB8D2FF), 1, true), BorderFactory.createEmptyBorder(9, 17, 9, 17)));
    }
    public static void field(JTextField f) {
        f.setFont(font(15, false)); f.setForeground(TEXTO); f.setBackground(Color.WHITE);
        f.setPreferredSize(new Dimension(300, 48));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDE, 1), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }
}
