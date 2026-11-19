package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.InicioControlador;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.controlador.ReporteControlador;
import ec.edu.monster.modelo.entidad.EstadoSistema;
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla principal: barra superior azul oscuro con la marca, las pestañas
 * de cada sección, el estado de MySQL y el regreso al inicio; debajo, el
 * contenido de la sección elegida a todo el ancho. Cada sección es una vista
 * independiente que solo conoce su controlador.
 *
 * @author carlo
 */
public class PrincipalVista extends JPanel {

    /** Secciones, en el orden de los atajos Ctrl+1, Ctrl+2 y Ctrl+3. */
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
    private final Map<Seccion, Pestana> pestanas = new EnumMap<>(Seccion.class);
    private final JLabel estadoBase = Componentes.etiqueta("Verificando MySQL…", PEQUENO, TEXTO_SOBRE_OSCURO);
    private final PaisesVista paisesVista;
    private final BusquedaVista busquedaVista;
    private final ReportesVista reportesVista;

    public PrincipalVista(InicioControlador inicioControlador, PaisControlador paisControlador,
            ReporteControlador reporteControlador, Runnable alSalir) {
        super(new BorderLayout());
        setBackground(SUPERFICIE);
        paisesVista = new PaisesVista(paisControlador);
        busquedaVista = new BusquedaVista(paisControlador);
        reportesVista = new ReportesVista(reporteControlador, () -> mostrar(Seccion.PAISES));

        contenido.setBackground(SUPERFICIE);
        contenido.add(paisesVista, Seccion.PAISES.name());
        contenido.add(busquedaVista, Seccion.BUSQUEDA.name());
        contenido.add(reportesVista, Seccion.REPORTES.name());

        add(barraSuperior(alSalir), BorderLayout.NORTH);
        add(contenido, BorderLayout.CENTER);
        registrarAtajos();
        mostrar(Seccion.PAISES);
        verificarBase(inicioControlador);
    }

    private void mostrar(Seccion seccion) {
        pestanas.forEach((clave, pestana) -> pestana.setElegida(clave == seccion));
        tarjetas.show(contenido, seccion.name());
        switch (seccion) {
            case PAISES -> paisesVista.alMostrar();
            case BUSQUEDA -> busquedaVista.alMostrar();
            case REPORTES -> reportesVista.cargar();
        }
    }

    /** Estado de MySQL en la barra (texto + punto de color: el estado no depende solo del color). */
    private void verificarBase(InicioControlador controlador) {
        Tarea.de(controlador::verificarEstado)
                .alTerminar(this::mostrarEstado)
                .alFallar(mensaje -> mostrarEstado(new EstadoSistema(false, 0, mensaje, false)))
                .iniciar();
    }

    private void mostrarEstado(EstadoSistema estado) {
        boolean conectada = estado.baseDisponible();
        estadoBase.setText(conectada ? "MySQL conectada" : "MySQL sin conexión");
        estadoBase.setIcon(new Punto(conectada ? VERDE_ESTADO : ERROR_RELLENO));
        estadoBase.setToolTipText(conectada ? Formato.paises(estado.totalPaises()) + " en api_paises"
                : estado.detalleBase());
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

    private JPanel barraSuperior(Runnable alSalir) {
        JPanel barra = new JPanel(new BorderLayout(XXL, 0));
        barra.setBackground(AZUL_OSCURO);
        barra.setPreferredSize(new Dimension(0, ALTO_BARRA_SUPERIOR));
        barra.setBorder(new EmptyBorder(0, XL, 0, XL));

        IconoEnCaja logo = new IconoEnCaja(Iconos.Nombre.PUBLICO, AZUL_ACCION, BLANCO, 34, 20);
        JPanel marca = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        marca.setOpaque(false);
        marca.add(logo);
        marca.add(Box.createHorizontalStrut(S));
        marca.add(Componentes.etiqueta("PAÍSES API", MARCA, BLANCO));
        barra.add(Componentes.enCentro(marca), BorderLayout.WEST);

        JPanel navegacion = new JPanel(new FlowLayout(FlowLayout.LEFT, XXS, 0));
        navegacion.setOpaque(false);
        navegacion.getAccessibleContext().setAccessibleName("Secciones");
        for (Seccion seccion : Seccion.values()) {
            Pestana pestana = new Pestana(seccion.titulo, seccion.icono, "Ctrl+" + (seccion.ordinal() + 1));
            pestana.addActionListener(e -> mostrar(seccion));
            pestanas.put(seccion, pestana);
            navegacion.add(pestana);
        }
        barra.add(navegacion, BorderLayout.CENTER);

        BotonClaro inicio = new BotonClaro("Inicio", Iconos.Nombre.ANTERIOR);
        inicio.setToolTipText("Volver a la pantalla de inicio");
        inicio.addActionListener(e -> alSalir.run());
        estadoBase.setIcon(new Punto(DESHABILITADO_TEXTO));
        estadoBase.setIconTextGap(XS);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        derecha.setOpaque(false);
        derecha.add(estadoBase);
        derecha.add(Box.createHorizontalStrut(M));
        derecha.add(inicio);
        barra.add(Componentes.enCentro(derecha), BorderLayout.EAST);
        return barra;
    }

    /**
     * Pestaña de la barra. La elegida se distingue por texto blanco en
     * negrita, la línea cian inferior y su descripción accesible: no solo por
     * el color.
     */
    private static final class Pestana extends JButton {

        private final Iconos.Nombre icono;
        private boolean elegida;

        Pestana(String texto, Iconos.Nombre icono, String atajo) {
            super(texto);
            this.icono = icono;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Componentes.MANO);
            setIconTextGap(XS);
            setBorder(new EmptyBorder(3, M, 0, M));
            setToolTipText(texto + " (" + atajo + ")");
            actualizar();
        }

        void setElegida(boolean elegida) {
            this.elegida = elegida;
            actualizar();
        }

        private void actualizar() {
            setFont(elegida ? BOTON_NEGRITA : BOTON);
            setForeground(elegida ? BLANCO : BORDE_SUAVE);
            setIcon(Iconos.icono(icono, elegida ? BLANCO : BORDE_SUAVE, 18));
            getAccessibleContext().setAccessibleDescription(elegida ? "Sección actual" : null);
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamano = super.getPreferredSize();
            tamano.height = ALTO_BARRA_SUPERIOR;
            return tamano;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = Componentes.suave(graphics);
            if (!elegida && getModel().isRollover()) {
                g.setColor(new Color(255, 255, 255, 16));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            if (elegida) {
                g.setColor(CIAN);
                g.fillRect(0, getHeight() - 3, getWidth(), 3);
            }
            if (isFocusOwner()) {
                g.setColor(AZUL_FOCO);
                g.setStroke(new BasicStroke(2f));
                g.draw(new RoundRectangle2D.Float(2, 12, getWidth() - 4f, getHeight() - 24f, 16, 16));
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Botón con borde blanco translúcido para la barra oscura. */
    private static final class BotonClaro extends JButton {

        BotonClaro(String texto, Iconos.Nombre icono) {
            super(texto);
            setFont(PEQUENO_SEMINEGRITA);
            setForeground(BLANCO);
            setIcon(Iconos.icono(icono, BLANCO, 18));
            setIconTextGap(6);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Componentes.MANO);
            setBorder(new EmptyBorder(0, 10, 0, 14));
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension tamano = super.getPreferredSize();
            tamano.height = 36;
            return tamano;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = Componentes.suave(graphics);
            RoundRectangle2D forma = new RoundRectangle2D.Float(0.5f, 0.5f, getWidth() - 1f, getHeight() - 1f,
                    2f * RADIO_CAMPO, 2f * RADIO_CAMPO);
            if (getModel().isRollover()) {
                g.setColor(new Color(255, 255, 255, 24));
                g.fill(forma);
            }
            g.setColor(isFocusOwner() ? AZUL_FOCO : new Color(255, 255, 255, 72));
            g.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
            g.draw(forma);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Punto de 8 px para el estado de la base. */
    private record Punto(Color color) implements Icon {

        @Override
        public void paintIcon(Component componente, Graphics graphics, int x, int y) {
            Graphics2D g = Componentes.suave(graphics);
            g.setColor(color);
            g.fill(new Ellipse2D.Float(x, y, 8, 8));
            g.dispose();
        }

        @Override
        public int getIconWidth() {
            return 8;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }
    }
}
