package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.InicioControlador;
import ec.edu.monster.modelo.entidad.EstadoSistema;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
import ec.edu.monster.vista.Componentes.TipoAlerta;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Pantalla de inicio con la foto de los talleres anteriores. La aplicación no
 * maneja usuarios, así que en lugar de pedir credenciales (que no protegerían
 * nada) muestra si MySQL responde y cómo está configurada la API, para que el
 * usuario sepa qué esperar antes de entrar.
 *
 * @author carlo
 */
public class InicioVista extends JPanel {

    private static final String RUTA_FOTO = "/imagenes/fotoLogin.jpg";

    private final InicioControlador controlador;
    private final FilaEstado filaBase = new FilaEstado(Iconos.Nombre.BASE_DATOS, "Base de datos MySQL");
    private final FilaEstado filaApi = new FilaEstado(Iconos.Nombre.LLAVE, "API REST Countries v5");
    private final Alerta aviso = new Alerta(false);
    private final Boton entrar = new Boton("Entrar", EstiloBoton.PRIMARIO, Iconos.Nombre.ENTRAR);
    private final Boton reintentar = new Boton("Reintentar", EstiloBoton.SECUNDARIO, Iconos.Nombre.ACTUALIZAR);

    public InicioVista(InicioControlador controlador, Runnable alEntrar) {
        super(new BorderLayout());
        this.controlador = controlador;
        setBackground(SUPERFICIE);

        entrar.addActionListener(e -> alEntrar.run());
        reintentar.addActionListener(e -> verificar());
        reintentar.setVisible(false);

        Pila derecha = new Pila()
                .agregar(tarjetaBienvenida())
                .agregar(new Texto("Fuente de datos: restcountries.com · Base local: MySQL api_paises",
                        LEYENDA, TEXTO_SECUNDARIO, SwingConstants.CENTER), M);
        add(Componentes.dosColumnas(new PanelMarca(),
                Componentes.desplazable(Componentes.centrado(derecha, ANCHO_ACCESO, XL, true)),
                1.1 / 2.1, 0, true), BorderLayout.CENTER);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        getRootPane().setDefaultButton(entrar);
        entrar.requestFocusInWindow();
    }

    /** Comprueba MySQL en segundo plano y actualiza las filas de estado. */
    public void verificar() {
        filaBase.verificando();
        filaApi.verificando();
        aviso.ocultar();
        reintentar.setVisible(false);
        reintentar.setCargando(true);
        Tarea.de(controlador::verificarEstado)
                .siempre(() -> reintentar.setCargando(false))
                .alTerminar(this::mostrarEstado)
                .alFallar(mensaje -> {
                    filaBase.mostrar("Sin conexión", mensaje, TipoAlerta.ERROR);
                    reintentar.setVisible(true);
                })
                .iniciar();
    }

    private void mostrarEstado(EstadoSistema estado) {
        if (estado.baseDisponible()) {
            filaBase.mostrar("Conectada", estado.totalPaises() == 0
                    ? "Aún no hay países guardados: impórtalos desde la API al entrar."
                    : Formato.paises(estado.totalPaises()) + " guardados en api_paises.", TipoAlerta.EXITO);
        } else {
            filaBase.mostrar("Sin conexión", estado.detalleBase(), TipoAlerta.ERROR);
            aviso.mostrar("Puedes entrar, pero no podrás consultar ni guardar países hasta que MySQL responda.",
                    TipoAlerta.ADVERTENCIA);
            reintentar.setVisible(true);
        }
        if (estado.apiDemostracion()) {
            filaApi.mostrar("Clave demo", "La API solo devolverá un país de muestra. Configura tu clave en "
                    + "api.clave (config.properties).", TipoAlerta.ADVERTENCIA);
        } else {
            filaApi.mostrar("Clave propia", "Datos completos. El plan gratuito permite 1.000 consultas al mes.",
                    TipoAlerta.EXITO);
        }
        revalidate();
    }

    private Caja tarjetaBienvenida() {
        Caja tarjeta = Componentes.tarjeta();
        tarjeta.agregar(Componentes.sobretitulo("Taller 2.1 · Grupo 03"));
        tarjeta.agregar(Componentes.etiqueta("Países del mundo", TITULAR_PEQUENO, TEXTO), XS);
        tarjeta.agregar(new Texto("Importa países desde la API REST Countries, gestiónalos en MySQL y genera "
                + "reportes estadísticos.", CUERPO, TEXTO_SECUNDARIO), XXS);
        tarjeta.agregar(Componentes.sobretitulo("Estado del sistema", TEXTO_SECUNDARIO), L);
        tarjeta.agregar(filaBase, S);
        tarjeta.agregar(Componentes.lineaDivisoria(), S);
        tarjeta.agregar(filaApi, S);
        tarjeta.agregar(aviso, M);
        tarjeta.agregar(entrar, L);
        tarjeta.agregar(reintentar, XS);
        return tarjeta;
    }

    /** Estado de una dependencia: icono, nombre, detalle y chip con icono (el estado no depende solo del color). */
    private static final class FilaEstado extends JPanel {

        private final IconoEnCaja icono;
        private final Iconos.Nombre nombreIcono;
        private final Texto detalle = new Texto("", CUERPO, TEXTO_SECUNDARIO);
        private final JPanel contenedorChip = new JPanel(new BorderLayout());

        FilaEstado(Iconos.Nombre nombreIcono, String titulo) {
            super(new BorderLayout(S, 0));
            this.nombreIcono = nombreIcono;
            setOpaque(false);
            icono = new IconoEnCaja(nombreIcono, AZUL_SUAVE, AZUL_ACCION, 40, 20);
            JPanel columnaIcono = new JPanel(new BorderLayout());
            columnaIcono.setOpaque(false);
            columnaIcono.add(icono, BorderLayout.NORTH);
            add(columnaIcono, BorderLayout.WEST);

            JPanel cabecera = new JPanel(new BorderLayout(XS, 0));
            cabecera.setOpaque(false);
            cabecera.add(Componentes.etiqueta(titulo, CUERPO_GRANDE_SEMINEGRITA, TEXTO), BorderLayout.CENTER);
            contenedorChip.setOpaque(false);
            cabecera.add(contenedorChip, BorderLayout.EAST);
            add(new Pila().agregar(cabecera).agregar(detalle, 2), BorderLayout.CENTER);
            getAccessibleContext().setAccessibleName(titulo);
        }

        void verificando() {
            mostrar("Verificando", "Comprobando…", TipoAlerta.INFO);
        }

        void mostrar(String estado, String texto, TipoAlerta tipo) {
            contenedorChip.removeAll();
            contenedorChip.add(Componentes.chipEstado(estado, tipo), BorderLayout.NORTH);
            detalle.setTexto(texto);
            boolean problema = tipo == TipoAlerta.ERROR || tipo == TipoAlerta.ADVERTENCIA;
            icono.cambiar(nombreIcono, problema ? (tipo == TipoAlerta.ERROR ? ERROR_FONDO : ADVERTENCIA_FONDO)
                    : AZUL_SUAVE, problema ? (tipo == TipoAlerta.ERROR ? ERROR_RELLENO : ADVERTENCIA_TEXTO) : AZUL_ACCION);
            getAccessibleContext().setAccessibleDescription(estado + ". " + texto);
            revalidate();
            repaint();
        }
    }

    /** Foto del grupo con un velo sólido azul oscuro (sin degradados) y los textos de marca. */
    private static final class PanelMarca extends JPanel {

        private final Image foto;

        PanelMarca() {
            super(new BorderLayout());
            URL recurso = InicioVista.class.getResource(RUTA_FOTO);
            foto = recurso == null ? null : new ImageIcon(recurso).getImage();
            setBackground(AZUL_OSCURO);
            setBorder(new EmptyBorder(L, L, XL, L));

            IconoEnCaja logo = new IconoEnCaja(Iconos.Nombre.PUBLICO, AZUL_ACCION, BLANCO, 36, 20);
            add(Componentes.fila(S, logo, Componentes.etiqueta("PAÍSES API", MARCA, BLANCO)), BorderLayout.NORTH);

            Pila textos = new Pila()
                    .agregar(new Texto("Los países del mundo,\nen tu base de datos", TITULAR_MEDIANO, BLANCO))
                    .agregar(new Texto("Consume la API pública REST Countries, guarda los datos en MySQL y "
                            + "analízalos con el patrón MVC.", CUERPO, BORDE_SUAVE), XS)
                    .agregar(Componentes.fila(XS,
                            Componentes.chip("REST", CIAN_SUAVE, CIAN_TEXTO_OSCURO),
                            Componentes.chip("Java 21", AZUL_SUAVE, AZUL_HOVER),
                            Componentes.chip("MySQL", AZUL_SUAVE, AZUL_HOVER),
                            Componentes.chip("MVC", AZUL_SUAVE, AZUL_HOVER)), M);
            add(textos, BorderLayout.SOUTH);
            getAccessibleContext().setAccessibleName("Foto del Grupo 03");
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (foto == null || foto.getWidth(this) <= 0) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            // Escala tipo "cover" con el encuadre algo por encima del centro
            int ancho = getWidth();
            int alto = getHeight();
            double escala = Math.max((double) ancho / foto.getWidth(this), (double) alto / foto.getHeight(this));
            int anchoFoto = (int) Math.ceil(foto.getWidth(this) * escala);
            int altoFoto = (int) Math.ceil(foto.getHeight(this) * escala);
            int x = (ancho - anchoFoto) / 2;
            int y = (int) ((alto - altoFoto) * 0.325);
            g.drawImage(foto, x, y, anchoFoto, altoFoto, this);
            // Velo para que el texto blanco tenga contraste suficiente sobre la foto
            g.setColor(new Color(AZUL_OSCURO.getRed(), AZUL_OSCURO.getGreen(), AZUL_OSCURO.getBlue(), 150));
            g.fillRect(0, 0, ancho, alto);
            g.dispose();
        }
    }
}
