package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.LoginControlador;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ClienteWSEureka;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.Texto;
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

/** Vista de acceso (RQF-001): solo muestra datos y delega las acciones al LoginControlador. */
public class LoginVista extends JPanel {

    private static final String RUTA_FOTO = "/imagenes/fotoLogin.jpg";

    private final Ventana ventana;
    private final LoginControlador controlador;

    private final CampoTexto usuario = new CampoTexto("Usuario", "Ej. MONSTER", null, false);
    private final CampoTexto clave = new CampoTexto("Contraseña", null, null, true);
    private final Boton ingresar = new Boton("Ingresar", EstiloBoton.PRIMARIO, null);
    private final Alerta mensaje = new Alerta();

    public LoginVista(Ventana ventana) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.controlador = new LoginControlador(this);
        setBackground(SUPERFICIE);

        ingresar.addActionListener(e -> iniciarSesion());
        usuario.alConfirmar(clave::enfocar);
        clave.alConfirmar(this::iniciarSesion);

        // Dos paneles, como el móvil en pantallas amplias: la foto a la izquierda y el formulario a la derecha
        Pila derecha = new Pila()
                .agregar(formularioAcceso())
                .agregar(new Texto("Cliente de escritorio · EurekaBank SOAP · Grupo 03", LEYENDA, TEXTO_SECUNDARIO,
                        SwingConstants.CENTER), M);
        add(Componentes.dosColumnas(new PanelMarca(),
                Componentes.desplazable(Componentes.centrado(derecha, ANCHO_ACCESO, XL, true)),
                1.1 / 2.1, 0, true), BorderLayout.CENTER);
    }

    // ---- Métodos que usa el controlador ----

    public void mostrarMensaje(String texto, boolean esError) {
        mensaje.mostrar(texto, esError);
    }

    public void setProcesando(boolean procesando) {
        ingresar.setCargando(procesando);
        usuario.setHabilitado(!procesando);
        clave.setHabilitado(!procesando);
    }

    public void limpiarClave() {
        clave.setTexto("");
        clave.enfocar();
    }

    public void abrirMenu(ClienteWSEureka clienteWs, SesionModelo sesion) {
        controlador.detener();
        ventana.mostrar(new MenuVista(ventana, clienteWs, sesion));
    }

    public void enfocar() {
        usuario.enfocar();
    }

    private void iniciarSesion() {
        controlador.iniciarSesion(usuario.getTexto(), clave.getTexto());
    }

    // ---- Interfaz ----

    private Caja formularioAcceso() {
        Caja tarjeta = Componentes.tarjeta();
        tarjeta.agregar(Componentes.sobretitulo("Acceso de empleados"));
        tarjeta.agregar(Componentes.etiqueta("Iniciar sesión", TITULAR_PEQUENO, TEXTO), XS);
        tarjeta.agregar(new Texto("Ingresa con tu usuario del sistema EurekaBank.", CUERPO, TEXTO_SECUNDARIO));
        tarjeta.agregar(usuario, L);
        tarjeta.agregar(clave, M);
        tarjeta.agregar(ingresar, L);
        tarjeta.agregar(mensaje, M);
        return tarjeta;
    }

    /** Foto del grupo con un velo sólido azul oscuro (sin degradados) y los textos de marca. */
    private static final class PanelMarca extends JPanel {

        private final Image foto;

        PanelMarca() {
            super(new BorderLayout());
            URL recurso = LoginVista.class.getResource(RUTA_FOTO);
            foto = recurso == null ? null : new ImageIcon(recurso).getImage();
            setBackground(AZUL_OSCURO);
            setBorder(new EmptyBorder(L, L, XL, L));

            IconoEnCaja logo = new IconoEnCaja(Iconos.Nombre.BANCO, BLANCO, AZUL_ACCION, 36, 20);
            add(Componentes.fila(S, logo, Componentes.etiqueta("EUREKABANK", SOBRETITULO, BLANCO)), BorderLayout.NORTH);

            Pila textos = new Pila()
                    .agregar(new Texto("Banca de ventanilla,\nsimple y segura", TITULAR_MEDIANO, BLANCO))
                    .agregar(new Texto("Consulta cuentas y registra depósitos, retiros y transferencias en tiempo real.",
                            CUERPO, BORDE_SUAVE), XS)
                    .agregar(Componentes.fila(XS,
                            Componentes.chip("SOAP", CIAN_SUAVE, CIAN_TEXTO_OSCURO),
                            Componentes.chip("Java", AZUL_SUAVE, AZUL_HOVER),
                            Componentes.chip("Grupo 03", AZUL_SUAVE, AZUL_HOVER)), M);
            add(textos, BorderLayout.SOUTH);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (foto == null || foto.getWidth(this) <= 0) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            // Escala tipo "cover" (ContentScale.Crop) con el encuadre algo por encima del centro
            int ancho = getWidth();
            int alto = getHeight();
            double escala = Math.max((double) ancho / foto.getWidth(this), (double) alto / foto.getHeight(this));
            int anchoFoto = (int) Math.ceil(foto.getWidth(this) * escala);
            int altoFoto = (int) Math.ceil(foto.getHeight(this) * escala);
            int x = (ancho - anchoFoto) / 2;
            int y = (int) ((alto - altoFoto) * 0.325);
            g.drawImage(foto, x, y, anchoFoto, altoFoto, this);
            g.setColor(new Color(AZUL_OSCURO.getRed(), AZUL_OSCURO.getGreen(), AZUL_OSCURO.getBlue(), 140));
            g.fillRect(0, 0, ancho, alto);
            g.dispose();
        }
    }
}
