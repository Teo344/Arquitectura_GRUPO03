package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.PrincipalControlador;
import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.ComponentesTienda.ItemLateral;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Ventana principal después del login, como las pantallas con sesión del
 * cliente web: barra lateral con la marca, las secciones, las unidades de la
 * venta en curso, el empleado, la vigencia de la sesión y Cerrar sesión; a la
 * derecha la sección elegida. La venta en curso (carrito) vive aquí, así se
 * conserva al cambiar de sección.
 */
public class PrincipalVista extends JPanel {

    public enum Seccion {
        VENTA, FACTURAS, TELEFONOS, CLIENTES, SIMULADOR
    }

    private final Ventana ventana;
    private final PrincipalControlador controlador;
    private final ClienteRESTMonsterCell clienteRest;
    private final CarritoModelo carrito = new CarritoModelo();
    private final Map<Seccion, ItemLateral> opciones = new EnumMap<>(Seccion.class);
    private final Boton salir = new Boton("Cerrar sesión", EstiloBoton.PELIGRO, Iconos.Nombre.SALIR);
    private final JPanel contenido = new JPanel(new BorderLayout());
    private SeccionVista actual;

    public PrincipalVista(Ventana ventana, ClienteRESTMonsterCell clienteRest, SesionModelo sesion) {
        super(new BorderLayout());
        this.ventana = ventana;
        this.clienteRest = clienteRest;
        this.controlador = new PrincipalControlador(this, clienteRest);
        setBackground(SUPERFICIE);
        contenido.setBackground(SUPERFICIE);
        ventana.setAlCerrar(controlador::salir);
        salir.addActionListener(e -> controlador.cerrarSesion());

        add(lateral(sesion), BorderLayout.WEST);
        add(contenido, BorderLayout.CENTER);
        mostrarSeccion(Seccion.VENTA);
    }

    // ---- Métodos que usa el controlador ----

    public void mostrarSeccion(Seccion seccion) {
        reemplazar(switch (seccion) {
            case VENTA -> new VentaVista(this, clienteRest, carrito);
            case FACTURAS -> new FacturasVista(this, clienteRest);
            case TELEFONOS -> new TelefonosVista(this, clienteRest);
            case CLIENTES -> new ClientesVista(this, clienteRest);
            case SIMULADOR -> new SimuladorVista(this, clienteRest);
        }, seccion);
    }

    public void setProcesando(boolean procesando) {
        salir.setCargando(procesando);
        opciones.values().forEach(opcion -> opcion.setEnabled(!procesando));
    }

    public void volverAlLogin(String mensaje, boolean esError) {
        controlador.detener();
        if (actual != null) {
            actual.detener();
        }
        ventana.abrirLogin(mensaje, esError);
    }

    public void cerrarAplicacion() {
        ventana.cerrar();
    }

    // ---- Navegación entre secciones ----

    /** Después de facturar: la factura que devolvió la API, con el aviso de la venta. */
    void abrirFactura(FacturaModelo factura, String aviso) {
        reemplazar(new FacturasVista(this, clienteRest, factura, aviso), Seccion.FACTURAS);
    }

    /** Desde una factura a crédito: la tabla de amortización de ese crédito. */
    void abrirCredito(int codCredito, String cedula) {
        reemplazar(new ClientesVista(this, clienteRest, cedula, codCredito), Seccion.CLIENTES);
    }

    /** Las unidades de la venta en curso, junto a "Nueva venta". */
    void actualizarContador() {
        opciones.get(Seccion.VENTA).setContador(carrito.getArticulos());
    }

    private void reemplazar(SeccionVista nueva, Seccion seccion) {
        if (actual != null) {
            actual.detener();
        }
        actual = nueva;
        opciones.forEach((s, opcion) -> opcion.setActivo(s == seccion));
        contenido.removeAll();
        contenido.add(nueva, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }

    // ---- Interfaz ----

    private JPanel lateral(SesionModelo sesion) {
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.setBackground(AZUL_OSCURO);
        lateral.setPreferredSize(new Dimension(ANCHO_LATERAL, 0));
        lateral.setBorder(new EmptyBorder(L, M, L, M));

        Pila marca = new Pila()
                .agregar(Componentes.etiqueta("MonsterCell", TITULO, BLANCO))
                .agregar(Componentes.etiqueta("Punto de venta · Grupo 03", LEYENDA, BORDE_SUAVE), 2);
        JPanel encabezado = new JPanel(new BorderLayout(S, 0));
        encabezado.setOpaque(false);
        encabezado.add(Componentes.enCentro(new IconoEnCaja(Iconos.Nombre.CELULAR, AZUL_ACCION, BLANCO, 40, 22)),
                BorderLayout.WEST);
        encabezado.add(marca, BorderLayout.CENTER);

        Pila navegacion = new Pila()
                .agregar(encabezado)
                .agregar(grupo("Ventas"), XL)
                .agregar(opcion(Seccion.VENTA, "Nueva venta", Iconos.Nombre.CARRITO), XS)
                .agregar(opcion(Seccion.FACTURAS, "Facturas", Iconos.Nombre.FACTURA), XXS)
                .agregar(grupo("Catálogo"), L)
                .agregar(opcion(Seccion.TELEFONOS, "Teléfonos", Iconos.Nombre.CATALOGO), XS)
                .agregar(grupo("Crédito BanQuito"), L)
                .agregar(opcion(Seccion.CLIENTES, "Consultar cliente", Iconos.Nombre.CREDITO), XS)
                .agregar(opcion(Seccion.SIMULADOR, "Simulador de cuotas", Iconos.Nombre.CALCULADORA), XXS);

        String nombre = sesion.getNombre() == null || sesion.getNombre().isBlank() ? "Vendedor" : sesion.getNombre();
        JPanel usuario = new JPanel(new BorderLayout(S, 0));
        usuario.setOpaque(false);
        usuario.add(Componentes.enCentro(ComponentesTienda.avatar(Formato.iniciales(sesion.getNombre()))),
                BorderLayout.WEST);
        usuario.add(new Pila()
                .agregar(new Componentes.Texto(nombre, BOTON_NEGRITA, BLANCO))
                .agregar(Componentes.etiqueta("Usuario " + Formato.valorO(sesion.getUsuario()), LEYENDA, BORDE_SUAVE), 2),
                BorderLayout.CENTER);
        JLabel vigencia = Componentes.etiqueta("Sesión activa hasta las " + Formato.hora(sesion.getExpiraEn()),
                LEYENDA, BORDE_SUAVE);
        vigencia.setIcon(Iconos.icono(Iconos.Nombre.INFO, CIAN, 16));
        vigencia.setIconTextGap(6);
        Pila pie = new Pila()
                .agregar(usuario)
                .agregar(vigencia, M)
                .agregar(salir, M);

        lateral.add(navegacion, BorderLayout.NORTH);
        lateral.add(pie, BorderLayout.SOUTH);
        return lateral;
    }

    private static JLabel grupo(String texto) {
        JLabel etiqueta = Componentes.sobretitulo(texto, DESHABILITADO_TEXTO);
        etiqueta.setBorder(new EmptyBorder(0, S, 0, 0));
        return etiqueta;
    }

    private ItemLateral opcion(Seccion seccion, String texto, Iconos.Nombre icono) {
        ItemLateral opcion = new ItemLateral(texto, icono, () -> controlador.abrir(seccion));
        opciones.put(seccion, opcion);
        return opcion;
    }
}
