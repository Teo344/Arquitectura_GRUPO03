package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.VentaControlador;
import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.LineaCarrito;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.SelectorSegmentado;
import ec.edu.monster.vista.Componentes.Texto;
import ec.edu.monster.vista.ComponentesTienda.Contador;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Punto de venta (CU-07, CU-09 y CU-10), como venta.jsp del cliente web:
 * catálogo con búsqueda en vivo a la izquierda y la venta actual a la derecha
 * en cuatro pasos (comprador, teléfonos, forma de pago y totales). Solo
 * muestra datos y delega las acciones al VentaControlador.
 */
public class VentaVista extends SeccionVista {

    private static final int COLUMNAS_CATALOGO = 2;

    private final VentaControlador controlador;
    private final CarritoModelo carrito;
    private List<TelefonoModelo> catalogo;
    private List<CuotaModelo> tablaSimulada;
    private boolean verTabla;
    private boolean facturando;

    private final Alerta mensaje = new Alerta();

    // Catálogo
    private final CampoTexto busqueda = new CampoTexto("Buscar por marca o modelo", "Buscar marca o modelo", null, false);
    private final JLabel conteo = Componentes.etiqueta("", LEYENDA, TEXTO_SECUNDARIO);
    private final Pila zonaCatalogo = new Pila();

    // Venta actual
    private final Pila zonaUnidades = new Pila();
    private final Boton vaciar = new Boton("Vaciar", EstiloBoton.TEXTO, Iconos.Nombre.ELIMINAR);
    private final CampoTexto cedula = new CampoTexto("Cédula del comprador", "1712345678", null, false);
    private final Boton verificar = new Boton("Verificar", EstiloBoton.SECUNDARIO, Iconos.Nombre.BUSCAR);
    private final Pila zonaEvaluacion = new Pila();
    private final Pila zonaLineas = new Pila();
    private final SelectorSegmentado<FormaPago> formaPago;
    private final Contador cuotas;
    private final Pila zonaCredito = new Pila();
    private final JLabel cuotaEstimada = Componentes.etiqueta("", TITULO, AZUL_OSCURO);
    private final Texto avisoCuota = new Texto("", LEYENDA, TEXTO_SECUNDARIO);
    private final Boton botonTabla = new Boton("Ver tabla de cuotas", EstiloBoton.TEXTO, Iconos.Nombre.CALCULADORA);
    private final Pila zonaTabla = new Pila();
    private final Pila zonaTotales = new Pila();
    private final Alerta motivo = new Alerta();
    private final Boton facturar = new Boton("Facturar", EstiloBoton.PRIMARIO, Iconos.Nombre.FACTURA);

    public VentaVista(PrincipalVista principal, ClienteRESTMonsterCell clienteRest, CarritoModelo carrito) {
        super(principal);
        this.carrito = carrito;
        this.controlador = new VentaControlador(this, clienteRest, carrito);

        formaPago = new SelectorSegmentado<>(List.of(FormaPago.values()), carrito.getFormaPago(),
                f -> f == FormaPago.EFECTIVO ? "Efectivo −42 %" : f.getNombreVisible(),
                f -> f == FormaPago.EFECTIVO ? Iconos.Nombre.EXITO : Iconos.Nombre.CREDITO,
                controlador::cambiarFormaPago);
        cuotas = new Contador(carrito.getNumCuotas(), CarritoModelo.CUOTAS_MINIMO, CarritoModelo.CUOTAS_MAXIMO, 96,
                n -> n + " cuotas", controlador::cambiarCuotas);

        busqueda.alCambiar(this::mostrarCatalogoFiltrado);
        cedula.setTexto(carrito.getCedula());
        cedula.alConfirmar(this::verificar);
        verificar.addActionListener(e -> verificar());
        vaciar.addActionListener(e -> controlador.vaciar());
        botonTabla.addActionListener(e -> {
            verTabla = !verTabla;
            mostrarTablaSimulada();
        });
        facturar.addActionListener(e -> controlador.facturar());

        Pila contenido = new Pila()
                .agregar(Componentes.encabezado("Ventas", "Nueva venta",
                        "Agrega teléfonos del catálogo, verifica al comprador en BanQuito y factura en efectivo o a"
                        + " crédito.", null, null))
                .agregar(mensaje, M)
                .agregar(Componentes.dosColumnas(columnaCatalogo(), tarjetaVenta(), 0.52,
                        L - 2 * Componentes.MARGEN_SOMBRA, false), L);
        add(Componentes.desplazable(Componentes.centrado(contenido, ANCHO_CONTENIDO, L, false)), BorderLayout.CENTER);

        actualizarVenta();
        controlador.cargarCatalogo();
    }

    // ---- Métodos que usa el controlador ----

    @Override
    public void setProcesando(boolean procesando) {
        setFacturando(procesando);
    }

    @Override
    public void mostrarMensaje(String texto, boolean esError) {
        mensaje.mostrar(texto, esError);
    }

    @Override
    void detener() {
        controlador.detener();
    }

    public void setCargandoCatalogo(boolean cargando) {
        if (cargando) {
            zonaCatalogo.vaciar();
            zonaCatalogo.agregar(Componentes.estadoVacio(Iconos.Nombre.CATALOGO, "Cargando catálogo",
                    "Consultando los teléfonos a la venta..."));
            zonaCatalogo.revalidate();
        }
    }

    public void mostrarCatalogo(List<TelefonoModelo> telefonos) {
        catalogo = telefonos;
        mostrarCatalogoFiltrado();
    }

    public void setVerificando(boolean verificando) {
        verificar.setCargando(verificando);
        cedula.setHabilitado(!verificando);
    }

    public void setFacturando(boolean facturando) {
        this.facturando = facturando;
        facturar.setCargando(facturando);
        vaciar.setEnabled(!facturando);
        actualizarBotonFacturar();
    }

    public void limpiarComprador() {
        cedula.setTexto("");
    }

    /** Vuelve a dibujar la venta a partir del carrito (el modelo). */
    public void actualizarVenta() {
        mensaje.ocultar();
        zonaUnidades.vaciar();
        int articulos = carrito.getArticulos();
        zonaUnidades.agregar(articulos == 0
                ? Componentes.chip("Sin teléfonos", FONDO_SECCION, TEXTO_SECUNDARIO)
                : Componentes.chip(articulos + (articulos == 1 ? " unidad" : " unidades"), AZUL_SUAVE, AZUL_HOVER,
                        Iconos.Nombre.CARRITO, AZUL_ACCION));
        vaciar.setEnabled(!carrito.isVacio() || carrito.getEvaluacion() != null);

        zonaEvaluacion.vaciar();
        if (carrito.getEvaluacion() != null) {
            zonaEvaluacion.agregar(ComponentesTienda.tarjetaEvaluacion(carrito.getEvaluacion(), true), S);
        }

        zonaLineas.vaciar();
        if (carrito.isVacio()) {
            zonaLineas.agregar(new Texto("Agrega teléfonos desde el catálogo.", CUERPO, TEXTO_SECUNDARIO), XS);
        } else {
            List<LineaCarrito> lineas = carrito.getLineas();
            for (int i = 0; i < lineas.size(); i++) {
                LineaCarrito linea = lineas.get(i);
                int codigo = linea.getTelefono().getCodTelefono();
                if (i > 0) {
                    zonaLineas.agregar(Componentes.lineaDivisoria());
                }
                zonaLineas.agregar(ComponentesTienda.lineaVenta(linea, CarritoModelo.CANTIDAD_MAXIMA,
                        cantidad -> controlador.cambiarCantidad(codigo, cantidad), () -> controlador.quitar(codigo)));
            }
        }

        formaPago.setSeleccionada(carrito.getFormaPago());
        cuotas.setValor(carrito.getNumCuotas());
        zonaCredito.setVisible(carrito.isCredito());

        zonaTotales.vaciar();
        zonaTotales.agregar(Componentes.filaDato("Subtotal", Formato.importe(carrito.getSubtotal()), false));
        if (!carrito.isCredito()) {
            zonaTotales.agregar(Componentes.filaDato("Descuento efectivo (42 %)",
                    "− " + Formato.importe(carrito.getDescuento()), false));
        } else if (carrito.getEvaluacion() != null && carrito.getEvaluacion().getMontoMaximo() != null) {
            zonaTotales.agregar(Componentes.filaDato("Monto máximo del comprador",
                    Formato.importe(carrito.getEvaluacion().getMontoMaximo()), false));
        }
        zonaTotales.agregar(Componentes.lineaDivisoria(), XS);
        zonaTotales.agregar(Componentes.filaDato("Total", Formato.importe(carrito.getTotal()), true), XS);

        actualizarBotonFacturar();
        mostrarCatalogoFiltrado();
        principal.actualizarContador();
        revalidate();
        repaint();
    }

    public void mostrarSimulacionCalculando() {
        tablaSimulada = null;
        cuotaEstimada.setText("Calculando...");
        avisoCuota.setTexto("Cuota mensual estimada (16.5 % anual)");
        botonTabla.setVisible(false);
        mostrarTablaSimulada();
    }

    /** Con null (efectivo o venta vacía) no hay cuota que mostrar. */
    public void mostrarSimulacion(List<CuotaModelo> tabla) {
        tablaSimulada = tabla;
        if (tabla == null || tabla.isEmpty()) {
            cuotaEstimada.setText("-");
            botonTabla.setVisible(false);
        } else {
            cuotaEstimada.setText(Formato.importe(tabla.get(0).getValorCuota()));
            avisoCuota.setTexto("Cuota mensual estimada (16.5 % anual)");
            botonTabla.setVisible(true);
        }
        mostrarTablaSimulada();
    }

    public void mostrarSimulacionNoDisponible(String error) {
        tablaSimulada = null;
        cuotaEstimada.setText("-");
        avisoCuota.setTexto("No se pudo calcular la cuota: " + error);
        botonTabla.setVisible(false);
        mostrarTablaSimulada();
    }

    public boolean pedirConfirmacion(String texto, List<String[]> filas) {
        return Componentes.confirmar(this, "Confirmar venta", texto, filas, "Facturar", true);
    }

    public void mostrarFacturaRegistrada(FacturaModelo factura, String aviso) {
        principal.abrirFactura(factura, aviso);
    }

    // ---- Interfaz ----

    private void verificar() {
        controlador.verificarComprador(cedula.getTexto());
    }

    private Pila columnaCatalogo() {
        JPanel titulo = new JPanel(new BorderLayout(S, 0));
        titulo.setOpaque(false);
        titulo.add(Componentes.tituloSeccion("Catálogo", "Teléfonos a la venta"), BorderLayout.CENTER);
        titulo.add(Componentes.enCentro(conteo), BorderLayout.EAST);
        return new Pila()
                .agregar(titulo, Componentes.MARGEN_SOMBRA)
                .agregar(busqueda, M)
                .agregar(zonaCatalogo, S);
    }

    private void mostrarCatalogoFiltrado() {
        if (catalogo == null) {
            return;
        }
        String filtro = busqueda.getTexto().trim().toLowerCase(Locale.ROOT);
        List<TelefonoModelo> visibles = catalogo.stream()
                .filter(t -> filtro.isEmpty() || t.getNombre().toLowerCase(Locale.ROOT).contains(filtro))
                .toList();
        conteo.setText(visibles.size() + (visibles.size() == 1 ? " modelo" : " modelos")
                + (filtro.isEmpty() ? "" : " de " + catalogo.size()));

        zonaCatalogo.vaciar();
        if (catalogo.isEmpty()) {
            zonaCatalogo.agregar(Componentes.estadoVacio(Iconos.Nombre.CATALOGO, "Catálogo vacío",
                    "No hay teléfonos a la venta. Agrégalos en Catálogo › Teléfonos."));
        } else if (visibles.isEmpty()) {
            zonaCatalogo.agregar(Componentes.estadoVacio(Iconos.Nombre.BUSCAR, "Sin resultados",
                    "Ningún teléfono coincide con «" + busqueda.getTexto().trim() + "»."));
        } else {
            Map<Integer, Integer> enVenta = carrito.getCantidades();
            JPanel grilla = new JPanel(new GridLayout(0, COLUMNAS_CATALOGO, M - 2 * Componentes.MARGEN_SOMBRA,
                    M - 2 * Componentes.MARGEN_SOMBRA));
            grilla.setOpaque(false);
            for (TelefonoModelo telefono : visibles) {
                grilla.add(ComponentesTienda.tarjetaProducto(telefono,
                        enVenta.getOrDefault(telefono.getCodTelefono(), 0), () -> controlador.agregar(telefono)));
            }
            for (int i = visibles.size(); i % COLUMNAS_CATALOGO != 0; i++) {
                JPanel vacio = new JPanel();
                vacio.setOpaque(false);
                grilla.add(vacio);
            }
            zonaCatalogo.agregar(grilla);
        }
        zonaCatalogo.revalidate();
        zonaCatalogo.repaint();
    }

    private Caja tarjetaVenta() {
        Caja tarjeta = Componentes.tarjeta();

        JPanel cabecera = new JPanel(new BorderLayout(S, 0));
        cabecera.setOpaque(false);
        cabecera.add(Componentes.fila(S, Componentes.etiqueta("Venta actual", TITULO, TEXTO), zonaUnidades),
                BorderLayout.CENTER);
        cabecera.add(vaciar, BorderLayout.EAST);
        tarjeta.agregar(cabecera);

        // 1. Comprador
        JPanel filaCedula = new JPanel(new BorderLayout(S, 0));
        filaCedula.setOpaque(false);
        filaCedula.add(cedula, BorderLayout.CENTER);
        // El botón queda a la altura del campo, no de la etiqueta
        JPanel alineado = new JPanel(new BorderLayout());
        alineado.setOpaque(false);
        alineado.add(verificar, BorderLayout.SOUTH);
        filaCedula.add(alineado, BorderLayout.EAST);
        tarjeta.agregar(Componentes.lineaDivisoria(), M);
        tarjeta.agregar(ComponentesTienda.paso(1, "Comprador"), M);
        tarjeta.agregar(filaCedula, S);
        tarjeta.agregar(Componentes.etiqueta("BanQuito confirma que es cliente y si puede comprar a crédito.", LEYENDA,
                TEXTO_SECUNDARIO), XXS);
        tarjeta.agregar(zonaEvaluacion);

        // 2. Teléfonos
        tarjeta.agregar(Componentes.lineaDivisoria(), L);
        tarjeta.agregar(ComponentesTienda.paso(2, "Teléfonos"), M);
        tarjeta.agregar(zonaLineas, XXS);

        // 3. Forma de pago
        tarjeta.agregar(Componentes.lineaDivisoria(), L);
        tarjeta.agregar(ComponentesTienda.paso(3, "Forma de pago"), M);
        tarjeta.agregar(formaPago, S);
        JPanel filaCuotas = new JPanel(new BorderLayout(S, 0));
        filaCuotas.setOpaque(false);
        filaCuotas.add(Componentes.etiqueta("Número de cuotas mensuales", BOTON, TEXTO), BorderLayout.CENTER);
        filaCuotas.add(cuotas, BorderLayout.EAST);
        Caja cuota = new Caja(AZUL_SUAVE, BORDE_SUAVE, RADIO_CAMPO, 0, 0, M);
        JPanel filaCuota = new JPanel(new BorderLayout(S, 0));
        filaCuota.setOpaque(false);
        filaCuota.add(new Pila().agregar(avisoCuota).agregar(cuotaEstimada, 2), BorderLayout.CENTER);
        filaCuota.add(Componentes.enCentro(botonTabla), BorderLayout.EAST);
        cuota.agregar(filaCuota);
        zonaCredito.agregar(filaCuotas, M).agregar(cuota, S).agregar(zonaTabla);
        tarjeta.agregar(zonaCredito);

        // 4. Totales
        tarjeta.agregar(Componentes.lineaDivisoria(), L);
        tarjeta.agregar(ComponentesTienda.paso(4, "Totales"), M);
        tarjeta.agregar(zonaTotales, XS);
        tarjeta.agregar(motivo, M);
        tarjeta.agregar(facturar, M);
        tarjeta.agregar(Componentes.etiqueta("Los precios se confirman con el catálogo al facturar.", LEYENDA,
                TEXTO_SECUNDARIO), XS);
        return tarjeta;
    }

    /** Facturar solo se habilita si la venta está lista; si no, se explica el motivo. */
    private void actualizarBotonFacturar() {
        String razon = controlador.motivoParaNoFacturar();
        facturar.setText(carrito.isVacio() ? "Facturar" : "Facturar " + Formato.importe(carrito.getTotal()));
        if (!facturando) {
            facturar.setEnabled(razon == null);
        }
        if (razon == null) {
            motivo.ocultar();
        } else {
            motivo.mostrar(razon, false);
        }
    }

    private void mostrarTablaSimulada() {
        zonaTabla.vaciar();
        boolean mostrar = verTabla && tablaSimulada != null && !tablaSimulada.isEmpty();
        botonTabla.setText(verTabla ? "Ocultar tabla" : "Ver tabla de cuotas");
        if (mostrar) {
            zonaTabla.agregar(new Texto(Formato.importe(carrito.getTotal()) + " a " + carrito.getNumCuotas()
                    + " cuotas, 16.5 % anual. Es una simulación: el crédito se otorga al facturar.", LEYENDA,
                    TEXTO_SECUNDARIO), M);
            zonaTabla.agregar(ComponentesTienda.tablaAmortizacion(tablaSimulada), XS);
        }
        zonaTabla.revalidate();
        zonaTabla.repaint();
    }
}
