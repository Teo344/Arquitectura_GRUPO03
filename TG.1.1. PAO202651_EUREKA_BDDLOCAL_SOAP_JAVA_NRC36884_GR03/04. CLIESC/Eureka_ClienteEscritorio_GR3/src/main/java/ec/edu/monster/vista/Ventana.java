package ec.edu.monster.vista;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

/**
 * Ventana única de la aplicación: cada pantalla (login, menú, consultas,
 * transacciones) reemplaza a la anterior, como las Activity del móvil.
 */
public class Ventana extends JFrame {

    private Runnable alCerrar;

    public Ventana() {
        super("EurekaBank | Cliente de escritorio SOAP");
        configurarAspecto();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (alCerrar != null) {
                    alCerrar.run();
                } else {
                    cerrar();
                }
            }
        });
        setIconImages(List.of(crearIcono(16), crearIcono(32), crearIcono(64)));
        setMinimumSize(new Dimension(1024, 700));
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(Math.min(1280, pantalla.width), Math.min(820, pantalla.height - 40));
        setLocationRelativeTo(null);
    }

    public void mostrar(JComponent pantalla) {
        setContentPane(pantalla);
        revalidate();
        repaint();
        if (!isVisible()) {
            setVisible(true);
        }
    }

    /** Vuelve al inicio de sesión, opcionalmente con un mensaje (sesión cerrada o caducada). */
    public void abrirLogin(String mensaje, boolean esError) {
        alCerrar = null;
        LoginVista login = new LoginVista(this);
        if (mensaje != null) {
            login.mostrarMensaje(mensaje, esError);
        }
        mostrar(login);
        login.enfocar();
    }

    /** Acción al cerrar la ventana mientras hay una sesión abierta (cerrar la sesión en el servidor). */
    public void setAlCerrar(Runnable alCerrar) {
        this.alCerrar = alCerrar;
    }

    public void cerrar() {
        dispose();
        System.exit(0);
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
    }

    /** Icono de la ventana: el banco blanco sobre el azul de acción. */
    private static BufferedImage crearIcono(int tamano) {
        BufferedImage imagen = new BufferedImage(tamano, tamano, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Tema.AZUL_ACCION);
        float radio = tamano * 0.28f;
        g.fill(new RoundRectangle2D.Float(0, 0, tamano, tamano, radio * 2, radio * 2));
        int margen = Math.round(tamano * 0.2f);
        Iconos.pintar(g, Iconos.Nombre.BANCO, Tema.BLANCO, margen, margen, tamano - 2 * margen);
        g.dispose();
        return imagen;
    }
}
