package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.FacturaControlador;
import ec.edu.monster.modelo.DetalleFacturaModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.BotonIcono;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import ec.edu.monster.vista.Componentes.Pila;
import java.awt.BorderLayout;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Facturas (CU-11), como facturas.jsp y factura.jsp del cliente web: la lista
 * de ventas con filtro por cédula y una factura con su detalle, lista para
 * imprimir. Solo muestra datos y delega las consultas al FacturaControlador.
 */
public class FacturasVista extends SeccionVista {

    private final FacturaControlador controlador;

    private final Alerta mensaje = new Alerta();
    private final CampoTexto cedula = new CampoTexto("Cédula del comprador", "1712345678",
            "Déjala vacía para ver las últimas facturas de todos los clientes.", false);
    private final Boton buscar = new Boton("Buscar", EstiloBoton.PRIMARIO, Iconos.Nombre.BUSCAR);
    private final Boton todas = new Boton("Ver todas", EstiloBoton.TEXTO, null);
    private final Pila cuerpo = new Pila();
    private final List<Boton> acciones = new ArrayList<>();

    /** La lista de las últimas facturas. */
    public FacturasVista(PrincipalVista principal, ClienteRESTMonsterCell clienteRest) {
        this(principal, clienteRest, null, null);
    }

    /** Una factura recién emitida, con el aviso de la venta; Volver lleva a la lista. */
    public FacturasVista(PrincipalVista principal, ClienteRESTMonsterCell clienteRest, FacturaModelo factura,
            String aviso) {
        super(principal);
        this.controlador = new FacturaControlador(this, clienteRest);
        buscar.addActionListener(e -> controlador.buscar(cedula.getTexto()));
        cedula.alConfirmar(() -> controlador.buscar(cedula.getTexto()));
        todas.addActionListener(e -> {
            cedula.setTexto("");
            controlador.buscar("");
        });
        acciones.add(buscar);

        Pila contenido = new Pila().agregar(cuerpo);
        add(Componentes.desplazable(Componentes.centrado(contenido, ANCHO_CONTENIDO, L, false)), BorderLayout.CENTER);

        if (factura == null) {
            mostrarFacturas("", null);
            controlador.buscar("");
        } else {
            mostrarFactura(factura);
            mensaje.mostrar(aviso, false);
        }
    }

    // ---- Métodos que usa el controlador ----

    @Override
    public void setProcesando(boolean procesando) {
        acciones.forEach(boton -> boton.setCargando(procesando));
        cedula.setHabilitado(!procesando);
        if (procesando) {
            mensaje.ocultar();
        }
    }

    @Override
    public void mostrarMensaje(String texto, boolean esError) {
        mensaje.mostrar(texto, esError);
    }

    @Override
    void detener() {
        controlador.detener();
    }

    /** Con lista null todavía se está consultando. */
    public void mostrarFacturas(String filtro, List<FacturaModelo> facturas) {
        acciones.clear();
        acciones.add(buscar);
        cuerpo.vaciar();
        cuerpo.agregar(Componentes.encabezado("Ventas", "Facturas",
                "Facturas emitidas, de la más reciente a la más antigua.", null, null));
        cuerpo.agregar(mensaje, M);

        Caja filtroTarjeta = Componentes.tarjeta();
        JPanel fila = new JPanel(new BorderLayout(S, 0));
        fila.setOpaque(false);
        fila.add(cedula, BorderLayout.CENTER);
        JPanel botones = Componentes.fila(XS, buscar, todas);
        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        abajo.add(botones, BorderLayout.NORTH);
        abajo.setBorder(new EmptyBorder(26, 0, 0, 0));
        fila.add(abajo, BorderLayout.EAST);
        filtroTarjeta.agregar(fila);
        cuerpo.agregar(filtroTarjeta, M);

        if (facturas == null) {
            cuerpo.agregar(Componentes.estadoVacio(Iconos.Nombre.FACTURA, "Cargando facturas",
                    "Consultando las ventas registradas..."), M);
        } else if (facturas.isEmpty()) {
            cuerpo.agregar(Componentes.estadoVacio(Iconos.Nombre.FACTURA, "Sin facturas", filtro.isEmpty()
                    ? "Todavía no se ha registrado ninguna venta."
                    : "El comprador con cédula " + filtro + " no tiene facturas."), M);
        } else {
            cuerpo.agregar(listaFacturas(filtro, facturas), M);
        }
        refrescar();
    }

    public void mostrarFactura(FacturaModelo factura) {
        acciones.clear();
        mensaje.ocultar();
        cuerpo.vaciar();

        Caja comprobante = comprobante(factura);
        Boton imprimir = new Boton("Imprimir", EstiloBoton.SECUNDARIO, Iconos.Nombre.IMPRIMIR);
        imprimir.addActionListener(e -> imprimir(comprobante, factura));
        cuerpo.agregar(Componentes.encabezado("Facturas", "Factura N.º " + factura.getNumeroVisible(),
                Formato.fecha(factura.getFecha()) + " · " + Formato.valorO(factura.getNombreCliente()),
                this::volverALista, imprimir));
        cuerpo.agregar(mensaje, M);
        cuerpo.agregar(comprobante, M);
        if (factura.isCredito() && factura.getCodCreditoBanco() != null) {
            Boton tabla = new Boton("Ver tabla de amortización", EstiloBoton.SECUNDARIO, Iconos.Nombre.CALCULADORA);
            tabla.addActionListener(e -> principal.abrirCredito(factura.getCodCreditoBanco(), factura.getCedula()));
            cuerpo.agregar(Componentes.fila(0, tabla), S);
        }
        refrescar();
    }

    // ---- Interfaz ----

    private void volverALista() {
        mostrarFacturas(cedula.getTexto().trim(), null);
        controlador.buscar(cedula.getTexto());
    }

    private Caja listaFacturas(String filtro, List<FacturaModelo> facturas) {
        Caja tarjeta = Componentes.tarjeta();
        JPanel titulo = new JPanel(new BorderLayout(S, 0));
        titulo.setOpaque(false);
        titulo.add(Componentes.tituloSeccion(filtro.isEmpty() ? "Todas" : "Cédula " + filtro, "Facturas emitidas"),
                BorderLayout.CENTER);
        titulo.add(Componentes.enCentro(Componentes.chip(facturas.size() + (facturas.size() == 1 ? " factura" : " facturas"),
                AZUL_SUAVE, AZUL_HOVER)), BorderLayout.EAST);
        tarjeta.agregar(titulo);

        List<Object[]> filas = new ArrayList<>();
        for (FacturaModelo factura : facturas) {
            BotonIcono ver = new BotonIcono(Iconos.Nombre.VER, AZUL_ACCION, 20, AZUL_SUAVE,
                    "Ver factura " + factura.getNumeroVisible());
            ver.addActionListener(e -> controlador.abrir(factura.getNumFactura()));
            filas.add(new Object[]{factura.getNumeroVisible(), Formato.fecha(factura.getFecha()),
                new Pila()
                        .agregar(Componentes.etiqueta(Formato.valorO(factura.getNombreCliente()), CUERPO, TEXTO))
                        .agregar(Componentes.etiqueta(Formato.valorO(factura.getCedula()), LEYENDA, TEXTO_SECUNDARIO)),
                ComponentesTienda.chipFormaPago(factura.getFormaPago()), Formato.importe(factura.getTotal()), ver});
        }
        int izquierda = SwingConstants.LEFT;
        tarjeta.agregar(ComponentesTienda.tabla(new String[]{"N.º", "Fecha", "Comprador", "Forma de pago", "Total", ""},
                new int[]{izquierda, izquierda, izquierda, izquierda, SwingConstants.RIGHT, SwingConstants.CENTER},
                new double[]{0.5, 0.8, 1.6, 0.9, 0.7, 0.2}, filas), M);
        return tarjeta;
    }

    /** La factura como se imprime: encabezado de la tienda, datos de la venta, detalle y totales. */
    private static Caja comprobante(FacturaModelo factura) {
        Caja tarjeta = Componentes.tarjeta();
        JPanel encabezado = new JPanel(new BorderLayout(S, 0));
        encabezado.setOpaque(false);
        encabezado.add(Componentes.enCentro(new IconoEnCaja(Iconos.Nombre.CELULAR, AZUL_ACCION, BLANCO, 48, 26)),
                BorderLayout.WEST);
        encabezado.add(new Pila()
                .agregar(Componentes.etiqueta("MonsterCell", TITULO, TEXTO))
                .agregar(Componentes.etiqueta("Comercializadora de celulares · Grupo 03", LEYENDA, TEXTO_SECUNDARIO), 2),
                BorderLayout.CENTER);
        encabezado.add(new Pila()
                .agregar(Componentes.sobretitulo("Factura"))
                .agregar(Componentes.etiqueta("N.º " + factura.getNumeroVisible(), TITULO, AZUL_OSCURO), 2),
                BorderLayout.EAST);
        tarjeta.agregar(encabezado);
        tarjeta.agregar(Componentes.lineaDivisoria(), M);

        tarjeta.agregar(Componentes.filaDato("Fecha", Formato.fecha(factura.getFecha()), false), XS);
        tarjeta.agregar(Componentes.filaDato("Comprador", Formato.valorO(factura.getNombreCliente()), false));
        tarjeta.agregar(Componentes.filaDato("Cédula", Formato.valorO(factura.getCedula()), false));
        tarjeta.agregar(Componentes.filaDato("Cliente BanQuito N.º", String.valueOf(factura.getCodClienteBanco()), false));
        tarjeta.agregar(Componentes.filaDato("Vendedor", Formato.valorO(factura.getUsuario()), false));
        JPanel pago = new JPanel(new BorderLayout(M, 0));
        pago.setOpaque(false);
        pago.setBorder(new EmptyBorder(XS, 0, XS, 0));
        pago.add(Componentes.etiqueta("Forma de pago", CUERPO, TEXTO_SECUNDARIO), BorderLayout.WEST);
        pago.add(Componentes.enCentro(ComponentesTienda.chipFormaPago(factura.getFormaPago())), BorderLayout.EAST);
        tarjeta.agregar(pago);
        if (factura.isCredito()) {
            tarjeta.agregar(Componentes.filaDato("Crédito BanQuito N.º", String.valueOf(factura.getCodCreditoBanco()),
                    false));
            tarjeta.agregar(Componentes.filaDato("Cuotas mensuales", String.valueOf(factura.getNumCuotas()), false));
        }

        List<Object[]> filas = new ArrayList<>();
        for (DetalleFacturaModelo detalle : factura.getDetalles()) {
            filas.add(new Object[]{detalle.getMarca() + " " + detalle.getModelo(), String.valueOf(detalle.getCantidad()),
                Formato.importe(detalle.getPrecioUnitario()), Formato.importe(detalle.getSubtotal())});
        }
        int derecha = SwingConstants.RIGHT;
        tarjeta.agregar(Componentes.lineaDivisoria(), M);
        tarjeta.agregar(ComponentesTienda.tabla(new String[]{"Teléfono", "Cantidad", "Precio unitario", "Subtotal"},
                new int[]{SwingConstants.LEFT, derecha, derecha, derecha}, new double[]{2, 0.6, 1, 1}, filas), XS);
        tarjeta.agregar(Componentes.lineaDivisoria(), XS);

        tarjeta.agregar(Componentes.filaDato("Subtotal", Formato.importe(factura.getSubtotal()), false), S);
        tarjeta.agregar(Componentes.filaDato(factura.isCredito() ? "Descuento" : "Descuento efectivo (42 %)",
                "− " + Formato.importe(factura.getDescuento()), false));
        tarjeta.agregar(Componentes.lineaDivisoria(), XS);
        tarjeta.agregar(Componentes.filaDato("Total", Formato.importe(factura.getTotal()), true), XS);
        return tarjeta;
    }

    private void imprimir(Caja comprobante, FacturaModelo factura) {
        try {
            ComponentesTienda.imprimir(comprobante, "Factura " + factura.getNumeroVisible());
        } catch (PrinterException ex) {
            mensaje.mostrar("No se pudo imprimir la factura: " + ex.getMessage(), true);
        }
    }

    private void refrescar() {
        cuerpo.revalidate();
        cuerpo.repaint();
    }
}
