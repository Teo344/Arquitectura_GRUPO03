package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * Contenido con un panel lateral que se abre sobre él desde la derecha (patrón
 * "drawer"): el contenido queda atenuado por un velo azul oscuro y no recibe
 * clics; el foco del teclado queda dentro del panel (Tab no sale de él) y Esc
 * o un clic en el velo piden cerrarlo.
 *
 * @author carlo
 */
final class PanelLateral extends JLayeredPane {

    private final JComponent contenido;
    private final Velo velo = new Velo();
    private JComponent panel;
    private Runnable alPedirCierre = () -> {
    };

    PanelLateral(JComponent contenido) {
        this.contenido = Objects.requireNonNull(contenido);
        add(contenido, JLayeredPane.DEFAULT_LAYER);
        add(velo, JLayeredPane.PALETTE_LAYER);
        velo.setVisible(false);
    }

    /**
     * Acción al pulsar Esc o hacer clic en el velo. Quien la recibe decide si
     * cierra (por ejemplo, si hay cambios sin guardar puede preguntar antes).
     */
    void alPedirCierre(Runnable accion) {
        this.alPedirCierre = Objects.requireNonNull(accion);
    }

    void abrir(JComponent nuevoPanel, Component foco) {
        if (panel != nuevoPanel) {
            if (panel != null) {
                remove(panel);
            }
            panel = nuevoPanel;
            panel.setFocusCycleRoot(true);
            panel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                    .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrar-panel");
            panel.getActionMap().put("cerrar-panel", new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    alPedirCierre.run();
                }
            });
            add(panel, JLayeredPane.MODAL_LAYER);
        }
        velo.setVisible(true);
        panel.setVisible(true);
        revalidate();
        repaint();
        if (foco != null) {
            SwingUtilities.invokeLater(foco::requestFocusInWindow);
        }
    }

    void cerrar() {
        velo.setVisible(false);
        if (panel != null) {
            panel.setVisible(false);
        }
        repaint();
    }

    boolean estaAbierto() {
        return panel != null && panel.isVisible();
    }

    @Override
    public void doLayout() {
        int ancho = getWidth();
        int alto = getHeight();
        contenido.setBounds(0, 0, ancho, alto);
        velo.setBounds(0, 0, ancho, alto);
        if (panel != null) {
            int anchoPanel = Math.min(ANCHO_PANEL_LATERAL, ancho);
            panel.setBounds(ancho - anchoPanel, 0, anchoPanel, alto);
        }
    }

    @Override
    public java.awt.Dimension getPreferredSize() {
        return contenido.getPreferredSize();
    }

    /** Velo translúcido con una sombra junto al borde del panel; consume los clics para que no lleguen al contenido. */
    private final class Velo extends JComponent {

        Velo() {
            MouseAdapter raton = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    alPedirCierre.run();
                }
            };
            addMouseListener(raton);
            addMouseMotionListener(raton);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(AZUL_OSCURO.getRed(), AZUL_OSCURO.getGreen(), AZUL_OSCURO.getBlue(), 115));
            g.fillRect(0, 0, getWidth(), getHeight());
            if (panel != null && panel.isVisible()) {
                // Sombra en capas sólidas (sin degradados) hacia la izquierda del panel
                int borde = panel.getX();
                for (int capa = 1; capa <= 12; capa++) {
                    g.setColor(new Color(TINTE_SOMBRA.getRed(), TINTE_SOMBRA.getGreen(), TINTE_SOMBRA.getBlue(),
                            13 - capa));
                    g.fillRect(borde - capa, 0, 1, getHeight());
                }
            }
        }
    }
}
