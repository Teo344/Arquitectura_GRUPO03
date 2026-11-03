package ec.edu.monster.vista;

import static ec.edu.monster.vista.Tema.*;

import ec.edu.monster.controlador.TransaccionControlador;
import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import ec.edu.monster.modelo.TipoTransaccion;
import ec.edu.monster.modelo.TransaccionModelo;
import ec.edu.monster.servicio.ClienteWSEureka;
import ec.edu.monster.vista.Componentes.Alerta;
import ec.edu.monster.vista.Componentes.Boton;
import ec.edu.monster.vista.Componentes.CampoTexto;
import ec.edu.monster.vista.Componentes.Caja;
import ec.edu.monster.vista.Componentes.EstiloBoton;
import ec.edu.monster.vista.Componentes.Pila;
import ec.edu.monster.vista.Componentes.SelectorSegmentado;
import java.awt.BorderLayout;
import java.util.List;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Vista de transacciones (RQF-005 a RQF-007): captura los datos, muestra la
 * confirmación y el comprobante; las reglas las valida el TransaccionControlador.
 */
public class TransaccionVista extends JPanel {

    private final MenuVista menu;
    private final TransaccionControlador controlador;

    private TipoTransaccion tipo;
    private TransaccionModelo registrada;
    private ResultadoTransaccionModelo resultado;

    private final JLabel titulo = Componentes.etiqueta("", TITULO, TEXTO);
    private final SelectorSegmentado<TipoTransaccion> selector;
    private final CampoTexto cuenta = new CampoTexto("Número de cuenta", "00100001",
            "8 dígitos, por ejemplo 00100001", false);
    private final CampoTexto cuentaDestino = new CampoTexto("Cuenta de destino", "00200002",
            "Debe ser distinta de la de origen y de la misma moneda", false);
    private final CampoTexto importe = new CampoTexto("Importe", "150.00",
            "Mayor que cero y con máximo dos decimales", false);
    private final Boton continuar = new Boton("Continuar", EstiloBoton.PRIMARIO, null);
    private final Alerta mensaje = new Alerta();
    private final Pila comprobante = new Pila();

    public TransaccionVista(MenuVista menu, ClienteWSEureka clienteWs, TipoTransaccion tipo) {
        super(new BorderLayout());
        this.menu = menu;
        this.controlador = new TransaccionControlador(this, clienteWs);
        this.tipo = tipo;
        setBackground(SUPERFICIE);

        selector = new SelectorSegmentado<>(List.of(TipoTransaccion.values()), tipo,
                TipoTransaccion::getNombreVisible, TransaccionVista::iconoDe, this::cambiarTipo);
        continuar.addActionListener(e -> preparar());
        cuenta.alConfirmar(() -> (this.tipo == TipoTransaccion.TRANSFERENCIA ? cuentaDestino : importe).enfocar());
        cuentaDestino.alConfirmar(importe::enfocar);
        importe.alConfirmar(this::preparar);

        add(Componentes.barraSuperior("Transacciones", "Depósitos, retiros y transferencias", this::volver, null),
                BorderLayout.NORTH);
        // Escritorio: formulario a la izquierda y comprobante a la derecha
        add(Componentes.desplazable(Componentes.centrado(
                Componentes.dosColumnas(formulario(), comprobante, 0.45, L - 2 * Componentes.MARGEN_SOMBRA, false),
                ANCHO_CONTENIDO, L, false)), BorderLayout.CENTER);
        actualizarTipo();
    }

    // ---- Métodos que usa el controlador ----

    public void setProcesando(boolean procesando) {
        continuar.setCargando(procesando);
        selector.setHabilitado(!procesando);
        cuenta.setHabilitado(!procesando);
        cuentaDestino.setHabilitado(!procesando);
        importe.setHabilitado(!procesando);
        if (procesando) {
            mensaje.ocultar();
        }
    }

    public void mostrarMensaje(String texto, boolean esError) {
        mensaje.mostrar(texto, esError);
    }

    /** El empleado revisa el resumen; solo si confirma se registra la operación. */
    public void pedirConfirmacion(TransaccionModelo modelo) {
        mensaje.ocultar();
        boolean confirmada = Componentes.confirmar(this,
                "Confirmar " + modelo.getTipo().getNombreVisible().toLowerCase(Locale.ROOT),
                resumen(modelo), "Registrar");
        if (confirmada) {
            controlador.ejecutar(modelo);
        }
    }

    public void mostrarResultado(TransaccionModelo modelo, ResultadoTransaccionModelo registro) {
        registrada = modelo;
        resultado = registro;
        importe.setTexto("");
        actualizarComprobante();
    }

    public void volverAlLogin(String mensaje) {
        menu.volverAlLogin(mensaje + ". Inicia sesión nuevamente.", true);
    }

    private void preparar() {
        controlador.preparar(tipo, cuenta.getTexto(), cuentaDestino.getTexto(), importe.getTexto());
    }

    private void cambiarTipo(TipoTransaccion nuevo) {
        tipo = nuevo;
        mensaje.ocultar();
        resultado = null;
        registrada = null;
        actualizarTipo();
    }

    private void volver() {
        controlador.detener();
        menu.regresar();
    }

    // ---- Interfaz ----

    private static Iconos.Nombre iconoDe(TipoTransaccion tipo) {
        return switch (tipo) {
            case DEPOSITO -> Iconos.Nombre.DEPOSITO;
            case RETIRO -> Iconos.Nombre.RETIRO;
            case TRANSFERENCIA -> Iconos.Nombre.TRANSFERENCIA;
        };
    }

    private Caja formulario() {
        Caja tarjeta = Componentes.tarjeta();
        tarjeta.agregar(Componentes.sobretitulo("Nueva operación"));
        tarjeta.agregar(titulo, XXS);
        tarjeta.agregar(selector, M);
        tarjeta.agregar(cuenta, M);
        tarjeta.agregar(cuentaDestino, M);
        tarjeta.agregar(importe, M);
        tarjeta.agregar(continuar, L);
        tarjeta.agregar(mensaje, M);
        return tarjeta;
    }

    private void actualizarTipo() {
        boolean esTransferencia = tipo == TipoTransaccion.TRANSFERENCIA;
        titulo.setText("Registrar " + tipo.getNombreVisible().toLowerCase(Locale.ROOT));
        cuenta.setEtiqueta(esTransferencia ? "Cuenta de origen" : "Número de cuenta");
        cuentaDestino.setVisible(esTransferencia);
        continuar.setIcono(iconoDe(tipo));
        revalidate();
        repaint();
        actualizarComprobante();
    }

    private void actualizarComprobante() {
        comprobante.vaciar();
        if (registrada == null || resultado == null) {
            comprobante.agregar(Componentes.estadoVacio(Iconos.Nombre.MOVIMIENTOS, "Sin operaciones registradas",
                    "Completa el formulario y confirma para ver aquí el comprobante con el nuevo saldo."));
        } else {
            String mensajeExito = resultado.getMensaje() == null ? "Operación registrada" : resultado.getMensaje();
            comprobante.agregar(Componentes.tarjetaResultado(mensajeExito, filasComprobante()));
        }
        comprobante.revalidate();
        comprobante.repaint();
    }

    private List<String[]> filasComprobante() {
        String origen = Componentes.valorO(registrada.getCuenta()).trim();
        if (registrada.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            return List.of(
                    new String[]{"Nuevo saldo de origen", Componentes.formatoImporte(resultado.getNuevoSaldo())},
                    new String[]{"Cuenta de origen", origen},
                    new String[]{"Cuenta de destino", Componentes.valorO(registrada.getCuentaDestino()).trim()},
                    new String[]{"Importe", Componentes.formatoImporte(registrada.getImporte())},
                    new String[]{"Movimiento en origen", "N.º " + numero(resultado.getNumeroMovimiento())},
                    new String[]{"Movimiento en destino", "N.º " + numero(resultado.getNumeroMovimientoDestino())});
        }
        return List.of(
                new String[]{"Nuevo saldo", Componentes.formatoImporte(resultado.getNuevoSaldo())},
                new String[]{"Cuenta", origen},
                new String[]{"Importe", Componentes.formatoImporte(registrada.getImporte())},
                new String[]{"Movimiento", "N.º " + numero(resultado.getNumeroMovimiento())});
    }

    private static List<String[]> resumen(TransaccionModelo modelo) {
        if (modelo.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            return List.of(
                    new String[]{"Cuenta de origen", Componentes.valorO(modelo.getCuenta()).trim()},
                    new String[]{"Cuenta de destino", Componentes.valorO(modelo.getCuentaDestino()).trim()},
                    new String[]{"Importe", Componentes.formatoImporte(modelo.getImporte())});
        }
        return List.of(
                new String[]{"Operación", modelo.getTipo().getNombreVisible()},
                new String[]{"Cuenta", Componentes.valorO(modelo.getCuenta()).trim()},
                new String[]{"Importe", Componentes.formatoImporte(modelo.getImporte())});
    }

    private static String numero(Integer valor) {
        return valor == null ? "-" : valor.toString();
    }
}
