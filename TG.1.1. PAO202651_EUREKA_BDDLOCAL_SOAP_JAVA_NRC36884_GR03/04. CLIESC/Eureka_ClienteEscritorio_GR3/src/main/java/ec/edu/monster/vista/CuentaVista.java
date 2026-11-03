package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.CuentaControlador;
import ec.edu.monster.modelo.CuentaModelo;
import ec.edu.monster.modelo.MovimientoModelo;
import ec.edu.monster.servicio.ClienteWSEureka;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.IconoEnCaja;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.SelectorSegmentado;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JPanel;

/** Vista de consultas (RQF-003 y RQF-004): solo muestra datos y delega al CuentaControlador. */
public class CuentaVista extends JPanel {

    private enum Modo {
        CUENTA("Datos de la cuenta", Iconos.Nombre.CUENTA),
        MOVIMIENTOS("Movimientos", Iconos.Nombre.MOVIMIENTOS);

        private final String etiqueta;
        private final Iconos.Nombre icono;

        Modo(String etiqueta, Iconos.Nombre icono) {
            this.etiqueta = etiqueta;
            this.icono = icono;
        }
    }

    private final MenuVista menu;
    private final CuentaControlador controlador;

    private Modo modo;
    private CuentaModelo datosCuenta;
    private List<MovimientoModelo> movimientos;
    private String cuentaMovimientos = "";

    private final SelectorSegmentado<Modo> selector;
    private final CampoTexto cuenta = new CampoTexto("Número de cuenta", "00100001",
            "8 dígitos, por ejemplo 00100001", false);
    private final Boton consultar = new Boton("", EstiloBoton.PRIMARIO, null);
    private final Alerta mensaje = new Alerta();
    private final Pila resultado = new Pila();

    public CuentaVista(MenuVista menu, ClienteWSEureka clienteWs, boolean verMovimientos) {
        super(new BorderLayout());
        this.menu = menu;
        this.controlador = new CuentaControlador(this, clienteWs);
        this.modo = verMovimientos ? Modo.MOVIMIENTOS : Modo.CUENTA;
        setBackground(SUPERFICIE);

        selector = new SelectorSegmentado<>(List.of(Modo.values()), modo, m -> m.etiqueta, m -> m.icono, m -> {
            modo = m;
            mensaje.ocultar();
            actualizarModo();
        });
        consultar.addActionListener(e -> consultar());
        cuenta.alConfirmar(this::consultar);

        add(Componentes.barraSuperior("Consultas", "Cuentas y movimientos", this::volver, null), BorderLayout.NORTH);
        // Escritorio: búsqueda a la izquierda y resultado a la derecha
        add(Componentes.desplazable(Componentes.centrado(
                Componentes.dosColumnas(busqueda(), resultado, 0.42, L - 2 * Componentes.MARGEN_SOMBRA, false),
                ANCHO_CONTENIDO, L, false)), BorderLayout.CENTER);
        actualizarModo();
    }

    // ---- Métodos que usa el controlador ----

    public void setProcesando(boolean procesando) {
        consultar.setCargando(procesando);
        cuenta.setHabilitado(!procesando);
        selector.setHabilitado(!procesando);
        if (procesando) {
            mensaje.ocultar();
        }
    }

    public void mostrarMensaje(String texto, boolean esError) {
        mensaje.mostrar(texto, esError);
    }

    public void mostrarCuenta(CuentaModelo resultado) {
        datosCuenta = resultado;
        actualizarResultado();
    }

    public void mostrarMovimientos(String codigo, List<MovimientoModelo> lista) {
        cuentaMovimientos = codigo;
        movimientos = lista;
        actualizarResultado();
    }

    public void volverAlLogin(String mensaje) {
        menu.volverAlLogin(mensaje + ". Inicia sesión nuevamente.", true);
    }

    private void consultar() {
        if (modo == Modo.CUENTA) {
            controlador.consultarCuenta(cuenta.getTexto());
        } else {
            controlador.consultarMovimientos(cuenta.getTexto());
        }
    }

    private void volver() {
        controlador.detener();
        menu.regresar();
    }

    // ---- Interfaz ----

    private Caja busqueda() {
        Caja tarjeta = Componentes.tarjeta();
        tarjeta.agregar(Componentes.sobretitulo("Consulta"));
        tarjeta.agregar(Componentes.etiqueta("Buscar una cuenta", TITULO, TEXTO), XXS);
        tarjeta.agregar(selector, M);
        tarjeta.agregar(cuenta, M);
        tarjeta.agregar(consultar, L);
        tarjeta.agregar(mensaje, M);
        return tarjeta;
    }

    private void actualizarModo() {
        consultar.setText(modo == Modo.CUENTA ? "Consultar cuenta" : "Ver movimientos");
        consultar.setIcono(modo.icono);
        actualizarResultado();
    }

    private void actualizarResultado() {
        resultado.vaciar();
        if (modo == Modo.CUENTA) {
            resultado.agregar(datosCuenta == null
                    ? Componentes.estadoVacio(Iconos.Nombre.CUENTA, "Sin consulta",
                            "Escribe un número de cuenta para ver su titular, saldo y estado.")
                    : fichaCuenta(datosCuenta));
        } else if (movimientos == null) {
            resultado.agregar(Componentes.estadoVacio(Iconos.Nombre.MOVIMIENTOS, "Sin consulta",
                    "Escribe un número de cuenta para ver su historial de movimientos."));
        } else if (movimientos.isEmpty()) {
            resultado.agregar(Componentes.estadoVacio(Iconos.Nombre.INFO, "Cuenta " + cuentaMovimientos,
                    "La cuenta no registra movimientos."));
        } else {
            resultado.agregar(listaMovimientos(movimientos));
        }
        resultado.revalidate();
        resultado.repaint();
    }

    private Caja fichaCuenta(CuentaModelo datos) {
        Caja tarjeta = Componentes.tarjeta();
        JPanel encabezado = new JPanel(new BorderLayout(S, 0));
        encabezado.setOpaque(false);
        encabezado.add(Componentes.enCentro(new IconoEnCaja(Iconos.Nombre.CUENTA, AZUL_SUAVE, AZUL_ACCION, 44)),
                BorderLayout.WEST);
        encabezado.add(new Pila()
                .agregar(Componentes.sobretitulo("Cuenta"))
                .agregar(Componentes.etiqueta(Componentes.valorO(datos.getCodigo()), TITULO, TEXTO)),
                BorderLayout.CENTER);
        encabezado.add(Componentes.enCentro(Componentes.chipEstado(datos.getEstado())), BorderLayout.EAST);
        tarjeta.agregar(encabezado);

        tarjeta.agregar(Componentes.etiqueta("Saldo disponible", CUERPO, TEXTO_SECUNDARIO), L);
        tarjeta.agregar(Componentes.etiqueta(Componentes.formatoImporte(datos.getSaldo()), TITULAR_MEDIANO, AZUL_OSCURO));
        tarjeta.agregar(Componentes.etiqueta(datos.getMoneda() == null ? "" : datos.getMoneda(), BOTON, CIAN_TEXTO));
        tarjeta.agregar(Componentes.lineaDivisoria(), M);
        tarjeta.agregar(Componentes.filaDato("Titular", Componentes.valorO(datos.getTitular()), false));
        tarjeta.agregar(Componentes.filaDato("Sucursal", Componentes.valorO(datos.getSucursal()), false));
        tarjeta.agregar(Componentes.filaDato("Fecha de creación", Componentes.valorO(datos.getFechaCreacion()), false));
        return tarjeta;
    }

    private Caja listaMovimientos(List<MovimientoModelo> lista) {
        Caja tarjeta = Componentes.tarjeta();
        JPanel encabezado = new JPanel(new BorderLayout(S, 0));
        encabezado.setOpaque(false);
        encabezado.add(new Pila()
                .agregar(Componentes.sobretitulo("Cuenta " + cuentaMovimientos))
                .agregar(Componentes.etiqueta("Historial de movimientos", TITULO, TEXTO)), BorderLayout.CENTER);
        encabezado.add(Componentes.enCentro(Componentes.chip(lista.size() + " movimientos", AZUL_SUAVE, AZUL_HOVER)),
                BorderLayout.EAST);
        tarjeta.agregar(encabezado);
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) {
                tarjeta.agregar(Componentes.lineaDivisoria());
            }
            tarjeta.agregar(Componentes.itemMovimiento(lista.get(i)), i == 0 ? S : 0);
        }
        return tarjeta;
    }
}
