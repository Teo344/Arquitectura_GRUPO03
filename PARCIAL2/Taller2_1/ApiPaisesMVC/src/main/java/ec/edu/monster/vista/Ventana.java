package ec.edu.monster.vista;

import ec.edu.monster.controlador.InicioControlador;
import ec.edu.monster.controlador.PaisControlador;
import ec.edu.monster.controlador.ReporteControlador;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/**
 * Ventana única de la aplicación: la pantalla de inicio y la principal se
 * reemplazan una a otra. Recibe los controladores desde App y los entrega a
 * cada pantalla; la vista nunca crea servicios ni DAO.
 *
 * @author carlo
 */
public class Ventana extends JFrame {

    private final InicioControlador inicioControlador;
    private final PaisControlador paisControlador;
    private final ReporteControlador reporteControlador;

    public Ventana(InicioControlador inicioControlador, PaisControlador paisControlador,
            ReporteControlador reporteControlador) {
        super("Países API | Taller 2.1 · Grupo 03");
        this.inicioControlador = Objects.requireNonNull(inicioControlador);
        this.paisControlador = Objects.requireNonNull(paisControlador);
        this.reporteControlador = Objects.requireNonNull(reporteControlador);
        configurarAspecto();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIconImages(List.of(crearIcono(16), crearIcono(32), crearIcono(64)));
        setMinimumSize(new Dimension(1120, 720));
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1366, pantalla.width), Math.min(860, pantalla.height - 40));
        setLocationRelativeTo(null);
    }

    public void abrirInicio() {
        InicioVista inicio = new InicioVista(inicioControlador, this::abrirPrincipal);
        mostrar(inicio);
        inicio.verificar();
    }

    public void abrirPrincipal() {
        mostrar(new PrincipalVista(inicioControlador, paisControlador, reporteControlador, this::abrirInicio));
    }

    private void mostrar(JComponent pantalla) {
        setContentPane(pantalla);
        revalidate();
        repaint();
        if (!isVisible()) {
            setVisible(true);
        }
    }

    // Metal sin negritas ni degradados: el aspecto lo dan los componentes del tema
    private static void configurarAspecto() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException ignorada) {
            // Se mantiene el aspecto por defecto
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        UIManager.put("Button.disabledText", new ColorUIResource(Tema.DESHABILITADO_TEXTO));
        UIManager.put("ToolTip.font", new FontUIResource(Tema.LEYENDA));
        UIManager.put("ToolTip.background", new ColorUIResource(Tema.AZUL_OSCURO));
        UIManager.put("ToolTip.foreground", new ColorUIResource(Tema.BLANCO));
        UIManager.put("ToolTip.border", BorderFactory.createEmptyBorder(6, 8, 6, 8));
        UIManager.put("ComboBox.selectionBackground", new ColorUIResource(Tema.AZUL_SUAVE));
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Tema.TEXTO));
    }

    /** Icono de la ventana: el globo blanco sobre el azul de acción. */
    private static BufferedImage crearIcono(int tamano) {
        BufferedImage imagen = new BufferedImage(tamano, tamano, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Tema.AZUL_ACCION);
        float radio = tamano * 0.28f;
        g.fill(new RoundRectangle2D.Float(0, 0, tamano, tamano, radio * 2, radio * 2));
        int margen = Math.round(tamano * 0.18f);
        Iconos.pintar(g, Iconos.Nombre.PUBLICO, Tema.BLANCO, margen, margen, tamano - 2 * margen);
        g.dispose();
        return imagen;
    }
}
