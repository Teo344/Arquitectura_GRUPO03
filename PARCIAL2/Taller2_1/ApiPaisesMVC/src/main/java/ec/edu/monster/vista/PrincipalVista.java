package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import ec.edu.monster.vista.Componentes.Pila;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla principal: menú lateral fijo y el contenido de la sección elegida.
 * Cada sección es una vista independiente que solo conoce su controlador.
 *
 * @author carlo
 */
public class PrincipalVista extends JPanel {

    /** Secciones del menú, en el orden de los atajos Ctrl+1, Ctrl+2 y Ctrl+3. */
    enum Seccion {
        PAISES("Países", Iconos.Nombre.TABLA, KeyEvent.VK_1),
        BUSQUEDA("Buscar en la API", Iconos.Nombre.BUSCAR, KeyEvent.VK_2),
        REPORTES("Reportes", Iconos.Nombre.GRAFICO, KeyEvent.VK_3);

        final String titulo;
        final Iconos.Nombre icono;
        final int tecla;

        Seccion(String titulo, Iconos.Nombre icono, int tecla) {
            this.titulo = titulo;
            this.icono = icono;
            this.tecla = tecla;
        }
    }

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<Seccion, ItemMenu> items = new EnumMap<>(Seccion.class);
    private final PaisesVista paisesVista;
    private final BusquedaVista busquedaVista;
    private final ReportesVista reportesVista;

    public PrincipalVista(PaisControlador paisControlador, ReporteControlador reporteControlador, Runnable alSalir) {
        super(new BorderLayout());
        setBackground(SUPERFICIE);
        paisesVista = new PaisesVista(paisControlador);
        busquedaVista = new BusquedaVista(paisControlador);
        reportesVista = new ReportesVista(reporteControlador, () -> mostrar(Seccion.PAISES));

        contenido.setBackground(SUPERFICIE);
        contenido.add(paisesVista, Seccion.PAISES.name());
        contenido.add(busquedaVista, Seccion.BUSQUEDA.name());
        contenido.add(reportesVista, Seccion.REPORTES.name());

        add(menuLateral(alSalir), BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        registrarAtajos();
        mostrar(Seccion.PAISES);
    }

    private void mostrar(Seccion seccion) {
        items.forEach((clave, item) -> item.setElegido(clave == seccion));
        tarjetas.show(contenido, seccion.name());
        // El foco sigue a la sección elegida (cada vista puede llevarlo a su primer control)
        items.get(seccion).requestFocusInWindow();
        switch (seccion) {
            case PAISES -> paisesVista.alMostrar();
            case BUSQUEDA -> busquedaVista.alMostrar();
            case REPORTES -> reportesVista.cargar();
        }
    }

    private void registrarAtajos() {
        for (Seccion seccion : Seccion.values()) {
            String accion = "seccion-" + seccion.name();
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(seccion.tecla, InputEvent.CTRL_DOWN_MASK), accion);
            getActionMap().put(accion, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    mostrar(seccion);
                }
            });
        }
    }

    private JPanel menuLateral(Runnable alSalir) {
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(AZUL_OSCURO);
        menu.setPreferredSize(new Dimension(ANCHO_MENU_LATERAL, 0));
        menu.setBorder(new EmptyBorder(L, M, L, M));

        IconoEnCaja logo = new IconoEnCaja(Iconos.Nombre.PUBLICO, AZUL_ACCION, BLANCO, 40, 22);
        JPanel marca = new JPanel(new BorderLayout(S, 0));
        marca.setOpaque(false);
        marca.setBorder(new EmptyBorder(0, XS, 0, 0));
        marca.add(logo, BorderLayout.WEST);
        marca.add(new Pila()
                .agregar(Componentes.etiqueta("PAÍSES API", MARCA, BLANCO))
                .agregar(Componentes.etiqueta("Taller 2.1 · Grupo 03", LEYENDA, BORDE_SUAVE), 2), BorderLayout.CENTER);

        Pila navegacion = new Pila();
        navegacion.agregar(marca);
        navegacion.agregar(Componentes.sobretitulo("Menú", BORDE_SUAVE), XL);
        for (Seccion seccion : Seccion.values()) {
            ItemMenu item = new ItemMenu(seccion.titulo, seccion.icono, "Ctrl+" + (seccion.ordinal() + 1));
            item.addActionListener(e -> mostrar(seccion));
            items.put(seccion, item);
            navegacion.agregar(item, XS);
        }
        menu.add(navegacion, BorderLayout.NORTH);

        ItemMenu salir = new ItemMenu("Volver al inicio", Iconos.Nombre.SALIR, null);
        salir.addActionListener(e -> alSalir.run());
        menu.add(salir, BorderLayout.SOUTH);
        return menu;
    }

    /**
     * Opción del menú. La elegida se distingue por fondo, barra cian, texto
     * blanco en negrita y su descripción accesible: no solo por el color.
     */
    private static final class ItemMenu extends JButton {

        private boolean elegido;
        private final Iconos.Nombre icono;

        ItemMenu(String texto, Iconos.Nombre icono, String atajo) {
            super(texto);
            this.icono = icono;
            setHorizontalAlignment(SwingConstants.LEFT);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Componentes.MANO);
            setIconTextGap(S);
            setBorder(new EmptyBorder(0, M, 0, S));
            setToolTipText(atajo == null ? null : texto + " (" + atajo + ")");
            actualizar();
        }

        void setElegido(boolean elegido) {
            this.elegido = elegido;
            actualizar();
        }

        private void actualizar() {
            setFont(elegido ? BOTON_NEGRITA : BOTON);
            setForeground(elegido ? BLANCO : BORDE_SUAVE);
            setIcon(Iconos.icono(icono, elegido ? BLANCO : BORDE_SUAVE, 20));
            getAccessibleContext().setAccessibleDescription(elegido ? "Sección actual" : null);
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamano = super.getPreferredSize();
            tamano.height = 48;
            return tamano;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = Componentes.suave(graphics);
            RoundRectangle2D forma = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                    2f * RADIO_CAMPO, 2f * RADIO_CAMPO);
            if (elegido) {
                g.setColor(new Color(255, 255, 255, 30));
                g.fill(forma);
                g.setColor(CIAN);
                g.fillRoundRect(0, S, 4, getHeight() - 2 * S, 4, 4);
            } else if (getModel().isRollover()) {
                g.setColor(new Color(255, 255, 255, 16));
                g.fill(forma);
            }
            if (isFocusOwner()) {
                g.setColor(AZUL_FOCO);
                g.setStroke(new BasicStroke(2f));
                g.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2f, getHeight() - 2f,
                        2f * RADIO_CAMPO - 2, 2f * RADIO_CAMPO - 2));
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
