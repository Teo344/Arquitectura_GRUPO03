package ec.edu.monster.servicios;

import ec.edu.monster.datos.ConexionBD;
import ec.edu.monster.datos.DetalleFacturaDao;
import ec.edu.monster.datos.FacturaDao;
import ec.edu.monster.datos.PasarelaBanquito;
import ec.edu.monster.datos.TelefonoDao;
import ec.edu.monster.modelo.ClienteBanco;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.CreditoBanco;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.EvaluacionBanco;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.Telefono;
import ec.edu.monster.seguridad.Sesion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Venta de teléfonos en efectivo (42 % de descuento) o con crédito directo
 * del banco, y consulta de facturas. El comprador siempre debe ser cliente
 * del banco.
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioFacturacion {

    private static final Logger LOG = Logger.getLogger(ServicioFacturacion.class.getName());

    /**
     * Una línea de la venta tal como la pide el cliente: el precio nunca
     * viene de afuera, se toma del catálogo.
     */
    public record Linea(Integer codTelefono, Integer cantidad) {
    }

    /**
     * Comprador según el banco y, en las ventas a crédito, el crédito que
     * otorgó.
     */
    private record Comprador(int codCliente, String nombre, CreditoBanco credito) {
    }

    @FunctionalInterface
    private interface OperacionBD<T> {

        T ejecutar(Connection cn) throws SQLException, ExcepcionServicio;
    }

    @Inject
    @ConfigProperty(name = "factura.descuentoEfectivo")
    private BigDecimal porcentajeDescuento;

    @Inject
    private ServicioValidacion validacion;

    @Inject
    private TelefonoDao telefonoDao;

    @Inject
    private FacturaDao facturaDao;

    @Inject
    private DetalleFacturaDao detalleFacturaDao;

    @Inject
    private PasarelaBanquito pasarela;

    /**
     * Pasos:
     * <ol>
     * <li>valida el formato de la solicitud;</li>
     * <li>arma las líneas con los precios del catálogo (solo teléfonos
     * ACTIVOS) y calcula subtotal, descuento y total;</li>
     * <li>consulta al Core: en EFECTIVO, que sea cliente; a CREDITO, los
     * servicios 1 y 2 y, si procede, el 3 para otorgar el crédito por el
     * total;</li>
     * <li>guarda encabezado y detalle en una transacción.</li>
     * </ol>
     * Las dos bases de datos son distintas y no hay transacción distribuida:
     * si el paso 4 falla después de otorgar el crédito, el código del crédito
     * queda en el log (SEVERE) para conciliarlo a mano.
     */
    public Factura facturar(Sesion sesion, String cedula, String formaPago, Integer numCuotas, List<Linea> lineas)
            throws ExcepcionServicio {
        String valorCedula = validacion.validarCedula(cedula);
        FormaPago forma = validacion.validarFormaPago(formaPago);
        Integer cuotas = forma == FormaPago.CREDITO ? validacion.validarCuotas(numCuotas) : null;
        List<Linea> pedidas = validarLineas(lineas);

        List<DetalleFactura> detalles = conConexion("leer los precios del catálogo", cn -> armarDetalles(cn, pedidas));
        BigDecimal subtotal = detalles.stream().map(DetalleFactura::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (subtotal.compareTo(ServicioValidacion.MAXIMO) > 0) {
            throw new ExcepcionServicio(CodigoError.DATA_001, "El total de la venta excede el valor permitido");
        }
        BigDecimal descuento = CalculadoraFactura.descuento(subtotal, forma, porcentajeDescuento);
        BigDecimal total = subtotal.subtract(descuento);

        Comprador comprador = forma == FormaPago.EFECTIVO ? comprador(valorCedula) : venderACredito(valorCedula, total, cuotas);
        Integer codCredito = comprador.credito() == null ? null : comprador.credito().codCredito;
        Factura factura = new Factura(0, null, sesion.usuario(), valorCedula, comprador.codCliente(),
                comprador.nombre(), forma, subtotal, descuento, total, codCredito, cuotas, detalles);
        try {
            return guardar(sesion.codUsuario(), factura);
        } catch (ExcepcionServicio e) {
            if (codCredito != null) {
                LOG.log(Level.SEVERE, "Crédito {0} otorgado por el Core para la cédula {1} sin factura registrada;"
                        + " conciliar a mano", new Object[]{codCredito, valorCedula});
            }
            throw e;
        }
    }

    public Factura buscar(Integer numero) throws ExcepcionServicio {
        int valor = validacion.validarCodigo(numero);
        return conConexion("consultar la factura", cn -> buscar(cn, valor));
    }

    /**
     * Facturas sin detalle, las más recientes primero; cedula vacía lista las
     * de todos los clientes.
     */
    public List<Factura> listar(String cedula) throws ExcepcionServicio {
        String valor = cedula == null || cedula.isBlank() ? null : validacion.validarCedula(cedula);
        return conConexion("listar las facturas", cn -> facturaDao.listar(cn, valor));
    }

    private List<Linea> validarLineas(List<Linea> lineas) throws ExcepcionServicio {
        if (lineas == null || lineas.isEmpty()) {
            throw new ExcepcionServicio(CodigoError.DATA_001, "La venta debe tener al menos un teléfono");
        }
        Set<Integer> vistos = new HashSet<>();
        List<Linea> validas = new ArrayList<>();
        for (Linea linea : lineas) {
            if (linea == null) {
                throw new ExcepcionServicio(CodigoError.DATA_001);
            }
            int codigo = validacion.validarCodigo(linea.codTelefono());
            if (!vistos.add(codigo)) {
                throw new ExcepcionServicio(CodigoError.DATA_001, "El teléfono " + codigo
                        + " está repetido; use la cantidad");
            }
            validas.add(new Linea(codigo, validacion.validarCantidad(linea.cantidad())));
        }
        return validas;
    }

    private List<DetalleFactura> armarDetalles(Connection cn, List<Linea> lineas)
            throws SQLException, ExcepcionServicio {
        List<DetalleFactura> detalles = new ArrayList<>();
        for (Linea linea : lineas) {
            Telefono telefono = telefonoDao.buscar(cn, linea.codTelefono())
                    .orElseThrow(() -> new ExcepcionServicio(CodigoError.TEL_001,
                    CodigoError.TEL_001.getMensaje() + ": " + linea.codTelefono()));
            if (!telefono.activo()) {
                throw new ExcepcionServicio(CodigoError.TEL_002, CodigoError.TEL_002.getMensaje() + ": "
                        + telefono.marca() + " " + telefono.modelo());
            }
            detalles.add(new DetalleFactura(telefono.codigo(), telefono.marca(), telefono.modelo(), linea.cantidad(),
                    telefono.precio(), CalculadoraFactura.subtotalLinea(telefono.precio(), linea.cantidad())));
        }
        return detalles;
    }

    private Comprador comprador(String cedula) throws ExcepcionServicio {
        try {
            ClienteBanco cliente = pasarela.buscarCliente(cedula);
            return new Comprador(cliente.codCliente, cliente.nombre, null);
        } catch (ExcepcionServicio e) {
            if (e.getCodigoError() == CodigoError.CLI_001) {
                throw new ExcepcionServicio(CodigoError.CLI_001);
            }
            throw e;
        }
    }

    /**
     * Servicio 1 (sujeto de crédito), servicio 2 (monto máximo) y, si el total
     * no lo supera, servicio 3 (otorgar el crédito por el total).
     */
    private Comprador venderACredito(String cedula, BigDecimal total, int cuotas) throws ExcepcionServicio {
        EvaluacionBanco evaluacion = pasarela.evaluar(cedula);
        if (!evaluacion.esCliente()) {
            throw new ExcepcionServicio(CodigoError.CLI_001);
        }
        if (!evaluacion.sujetoCredito) {
            throw new ExcepcionServicio(CodigoError.CRE_001, CodigoError.CRE_001.getMensaje() + ": "
                    + evaluacion.motivos.stream().map(m -> m.descripcion).collect(Collectors.joining(", ")));
        }
        BigDecimal maximo = pasarela.montoMaximo(evaluacion.codCliente).montoMaximo;
        if (total.compareTo(maximo) > 0) {
            throw new ExcepcionServicio(CodigoError.CRE_002, CodigoError.CRE_002.getMensaje() + " (total " + total
                    + ", máximo " + maximo + ")");
        }
        CreditoBanco credito = pasarela.otorgarCredito(evaluacion.codCliente, total, cuotas);
        return new Comprador(evaluacion.codCliente, evaluacion.nombre, credito);
    }

    private Factura guardar(int codUsuario, Factura factura) throws ExcepcionServicio {
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                int numero = facturaDao.insertar(cn, codUsuario, factura);
                detalleFacturaDao.insertar(cn, numero, factura.detalles());
                Factura guardada = buscar(cn, numero);
                cn.commit();
                return guardada;
            } catch (ExcepcionServicio | SQLException | RuntimeException e) {
                revertir(cn);
                throw e;
            }
        } catch (SQLException | RuntimeException e) {
            LOG.log(Level.SEVERE, "Factura revertida por un error del sistema", e);
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }

    private Factura buscar(Connection cn, int numero) throws SQLException, ExcepcionServicio {
        Factura f = facturaDao.buscar(cn, numero).orElseThrow(() -> new ExcepcionServicio(CodigoError.FAC_001));
        return new Factura(f.numero(), f.fecha(), f.usuario(), f.cedula(), f.codClienteBanco(), f.nombreCliente(),
                f.formaPago(), f.subtotal(), f.descuento(), f.total(), f.codCreditoBanco(), f.numCuotas(),
                detalleFacturaDao.listar(cn, numero));
    }

    private <T> T conConexion(String accion, OperacionBD<T> operacion) throws ExcepcionServicio {
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            return operacion.ejecutar(cn);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al " + accion, e);
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }

    private static void revertir(Connection cn) {
        try {
            cn.rollback();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo revertir la transacción", e);
        }
    }
}
