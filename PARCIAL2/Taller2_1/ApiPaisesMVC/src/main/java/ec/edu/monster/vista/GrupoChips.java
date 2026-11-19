package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

/**
 * Filtro de una sola opción con forma de chips redondeados ("Todas", "África",
 * "América"…). Cada chip muestra cuántos elementos tiene; el elegido se pinta
 * azul oscuro y además se anuncia como seleccionado (no solo por el color).
 *
 * @param <T> tipo de cada opción
 * @author carlo
 */
final class GrupoChips<T> extends JPanel {

    private final Map<T, Chip> chips = new LinkedHashMap<>();
    private final ButtonGroup grupo = new ButtonGroup();
    private Consumer<T> alCambiar = opcion -> {
    };
    private T elegida;

    GrupoChips(String nombreAccesible, List<T> opciones, Function<T, String> nombre) {
        super(new FlowLayout(FlowLayout.LEFT, 6, 0));
        setOpaque(false);
        getAccessibleContext().setAccessibleName(nombreAccesible);
        for (T opcion : opciones) {
            Chip chip = new Chip(nombre.apply(opcion));
            chip.addActionListener(e -> {
                if (opcion != elegida) {
                    elegida = opcion;
                    alCambiar.accept(opcion);
                }
            });
            grupo.add(chip);
            chips.put(opcion, chip);
            add(chip);
        }
    }

    void alCambiar(Consumer<T> accion) {
        this.alCambiar = accion;
    }

    /** Elige la opción sin avisar a {@link #alCambiar}. */
    void setSeleccion(T opcion) {
        elegida = opcion;
        Chip chip = chips.get(opcion);
        if (chip != null) {
            chip.setSelected(true);
        }
    }

    T getSeleccion() {
        return elegida;
    }

    void setConteos(ToLongFunction<T> conteo) {
        chips.forEach((opcion, chip) -> chip.setConteo(conteo.applyAsLong(opcion)));
        revalidate();
        repaint();
    }

    /** Chip de 36 px de alto: nombre + insignia con la cantidad. */
    private static final class Chip extends JToggleButton {

        private final String nombre;
        private String conteo = "";

        Chip(String nombre) {
            super(nombre);
            this.nombre = nombre;
            setFont(PEQUENO_SEMINEGRITA);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Componentes.MANO);
            setBorder(new EmptyBorder(0, 14, 0, 10));
            addItemListener(e -> repaint());
        }

        void setConteo(long cantidad) {
            conteo = Formato.entero(cantidad);
            getAccessibleContext().setAccessibleName(nombre + ", " + conteo);
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics medidas = getFontMetrics(getFont());
            FontMetrics medidasConteo = getFontMetrics(LEYENDA_NEGRITA);
            int ancho = 14 + medidas.stringWidth(nombre) + XS + Math.max(20, medidasConteo.stringWidth(conteo) + 12) + 6;
            return new Dimension(ancho, 36);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = Componentes.suave(graphics);
            boolean activo = isSelected();
            RoundRectangle2D forma = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f,
                    getHeight(), getHeight());
            g.setColor(activo ? AZUL_OSCURO : getModel().isRollover() ? AZUL_SUAVE : BLANCO);
            g.fill(forma);
            g.setColor(activo ? AZUL_OSCURO : DIVISOR);
            g.draw(forma);

            FontMetrics medidas = g.getFontMetrics(getFont());
            int y = (getHeight() - medidas.getHeight()) / 2 + medidas.getAscent();
            g.setFont(getFont());
            g.setColor(activo ? BLANCO : TEXTO);
            g.drawString(nombre, 14, y);

            g.setFont(LEYENDA_NEGRITA);
            FontMetrics medidasConteo = g.getFontMetrics();
            int anchoInsignia = Math.max(20, medidasConteo.stringWidth(conteo) + 12);
            int x = 14 + medidas.stringWidth(nombre) + XS;
            int altoInsignia = 20;
            int yInsignia = (getHeight() - altoInsignia) / 2;
            g.setColor(activo ? new Color(255, 255, 255, 46) : FONDO_SECCION);
            g.fill(new RoundRectangle2D.Float(x, yInsignia, anchoInsignia, altoInsignia, altoInsignia, altoInsignia));
            g.setColor(activo ? BLANCO : TEXTO_SECUNDARIO);
            g.drawString(conteo, x + (anchoInsignia - medidasConteo.stringWidth(conteo)) / 2,
                    yInsignia + (altoInsignia - medidasConteo.getHeight()) / 2 + medidasConteo.getAscent());

            if (isFocusOwner()) {
                g.setColor(AZUL_FOCO);
                g.setStroke(new BasicStroke(2f));
                g.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2f, getHeight() - 2f, getHeight() - 2f,
                        getHeight() - 2f));
            }
            g.dispose();
        }
    }
}
