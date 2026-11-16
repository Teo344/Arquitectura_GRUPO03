package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.stream.Collectors;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;

/**
 * Barras horizontales de una sola serie (magnitud por categoría), según la
 * guía de visualización: un solo color sin leyenda (el título nombra la
 * serie), barras de 24 px con el extremo redondeado (4 px) y la base recta
 * sobre una línea base, valor escrito en la punta con tinta de texto (nunca
 * del color de la barra), sin cuadrícula y con tooltip por barra. Como cada
 * barra lleva su valor, el gráfico no necesita eje.
 *
 * @author carlo
 */
public final class GraficoBarras extends JComponent {

    /** Una barra: etiqueta y valor. */
    public record Barra(String etiqueta, long valor) {
    }

    private static final int ALTO_FILA = 40;
    private static final int GROSOR = 24;
    private static final int RADIO_PUNTA = 4;

    private List<Barra> barras = List.of();
    private long total;
    private int encima = -1;

    public GraficoBarras() {
        setOpaque(false);
        ToolTipManager.sharedInstance().registerComponent(this);
        MouseAdapter raton = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                cambiarEncima(filaEn(e.getY()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cambiarEncima(-1);
            }
        };
        addMouseListener(raton);
        addMouseMotionListener(raton);
    }

    /** Las barras se muestran en el orden recibido (el reporte ya llega de mayor a menor). */
    public void setBarras(List<Barra> nuevas) {
        barras = List.copyOf(nuevas);
        total = barras.stream().mapToLong(Barra::valor).sum();
        encima = -1;
        revalidate();
        repaint();
    }

    /** Alternativa textual para lectores de pantalla: el título y todos los valores. */
    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                @Override
                public AccessibleRole getAccessibleRole() {
                    return AccessibleRole.CANVAS;
                }

                @Override
                public String getAccessibleName() {
                    return "Gráfico de países por región";
                }

                @Override
                public String getAccessibleDescription() {
                    return barras.stream()
                            .map(b -> b.etiqueta() + ": " + Formato.paises(b.valor()))
                            .collect(Collectors.joining("; "));
                }
            };
        }
        return accessibleContext;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(320, Math.max(1, barras.size()) * ALTO_FILA);
    }

    @Override
    public String getToolTipText(MouseEvent evento) {
        int fila = filaEn(evento.getY());
        if (fila < 0) {
            return null;
        }
        Barra barra = barras.get(fila);
        return barra.etiqueta() + ": " + Formato.paises(barra.valor()) + " ("
                + Formato.porcentaje(total == 0 ? 0 : (double) barra.valor() / total) + ")";
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        if (barras.isEmpty()) {
            return;
        }
        Graphics2D g = Componentes.suave(graphics);
        FontMetrics etiquetas = g.getFontMetrics(CUERPO);
        FontMetrics valores = g.getFontMetrics(CUERPO_SEMINEGRITA);
        int anchoEtiquetas = barras.stream().mapToInt(b -> etiquetas.stringWidth(b.etiqueta())).max().orElse(0);
        int anchoValores = barras.stream()
                .mapToInt(b -> valores.stringWidth(Formato.entero(b.valor()))).max().orElse(0);
        int base = anchoEtiquetas + M;
        int largoMaximo = Math.max(1, getWidth() - base - anchoValores - S);
        long maximo = barras.stream().mapToLong(Barra::valor).max().orElse(1);

        // Línea base recesiva de 1 px
        g.setColor(DIVISOR);
        g.fillRect(base, 0, 1, barras.size() * ALTO_FILA);

        for (int i = 0; i < barras.size(); i++) {
            Barra barra = barras.get(i);
            int centro = i * ALTO_FILA + ALTO_FILA / 2;

            g.setFont(CUERPO);
            g.setColor(TEXTO_SECUNDARIO);
            g.drawString(barra.etiqueta(), 0, centro + etiquetas.getAscent() / 2 - 2);

            int largo = maximo == 0 ? 0 : (int) Math.round((double) barra.valor() / maximo * largoMaximo);
            largo = barra.valor() > 0 ? Math.max(largo, RADIO_PUNTA * 2) : 0;
            if (largo > 0) {
                g.setColor(i == encima ? AZUL_HOVER : AZUL_MARCA);
                g.fill(barra(base + 1, centro - GROSOR / 2, largo, GROSOR));
            }

            g.setFont(CUERPO_SEMINEGRITA);
            g.setColor(TEXTO);
            g.drawString(Formato.entero(barra.valor()), base + 1 + largo + XS, centro + valores.getAscent() / 2 - 2);
        }
        g.dispose();
    }

    /** Rectángulo con la base recta (sobre la línea base) y la punta redondeada. */
    private static Path2D barra(float x, float y, float largo, float alto) {
        float r = Math.min(RADIO_PUNTA, largo / 2);
        Path2D.Float forma = new Path2D.Float();
        forma.moveTo(x, y);
        forma.lineTo(x + largo - r, y);
        forma.quadTo(x + largo, y, x + largo, y + r);
        forma.lineTo(x + largo, y + alto - r);
        forma.quadTo(x + largo, y + alto, x + largo - r, y + alto);
        forma.lineTo(x, y + alto);
        forma.closePath();
        return forma;
    }

    private int filaEn(int y) {
        int fila = y / ALTO_FILA;
        return y >= 0 && fila < barras.size() ? fila : -1;
    }

    private void cambiarEncima(int fila) {
        if (fila != encima) {
            encima = fila;
            repaint();
        }
    }
}
