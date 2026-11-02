package ec.edu.monster.servicios;

import ec.edu.monster.datos.ClienteDao;
import ec.edu.monster.datos.ConexionBD;
import ec.edu.monster.datos.CreditoDao;
import ec.edu.monster.datos.CuotaDao;
import ec.edu.monster.datos.MovimientoDao;
import ec.edu.monster.datos.MovimientoDao.Promedios;
import ec.edu.monster.modelo.Cliente;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.Cuota;
import ec.edu.monster.modelo.EvaluacionCredito;
import ec.edu.monster.modelo.MontoMaximo;
import ec.edu.monster.modelo.MotivoRechazo;
import ec.edu.monster.modelo.ReglasCredito;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Los 4 servicios del módulo de crédito: sujeto de crédito, monto máximo,
 * otorgar crédito y tabla de amortización; además la consulta del cliente y
 * la simulación de la tabla sin guardarla.
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioCredito {

    private static final Logger LOG = Logger.getLogger(ServicioCredito.class.getName());
    private static final BigDecimal CIEN = new BigDecimal("100");

    /**
     * Meses hacia atrás de los movimientos que entran en los promedios.
     */
    private static final int MESES_PROMEDIO = 3;

    @FunctionalInterface
    private interface OperacionBD<T> {

        T ejecutar(Connection cn) throws SQLException, ExcepcionServicio;
    }

    @Inject
    @ConfigProperty(name = "credito.tasaAnual")
    private BigDecimal tasaAnual;

    @Inject
    @ConfigProperty(name = "credito.porcentajeMonto")
    private BigDecimal porcentajeMonto;

    @Inject
    @ConfigProperty(name = "credito.plazoMeses")
    private int plazoMeses;

    @Inject
    private ServicioValidacion validacion;

    @Inject
    private ClienteDao clienteDao;

    @Inject
    private MovimientoDao movimientoDao;

    @Inject
    private CreditoDao creditoDao;

    @Inject
    private CuotaDao cuotaDao;

    public Cliente buscarCliente(String cedula) throws ExcepcionServicio {
        String valor = validacion.validarCedula(cedula);
        return conConexion("consultar el cliente", cn -> clienteDao.buscarPorCedula(cn, valor)
                .orElseThrow(() -> new ExcepcionServicio(CodigoError.CLI_001)));
    }

    /**
     * Servicio 1. Una cédula que no es de un cliente no es un error: es una
     * evaluación con el motivo NO_ES_CLIENTE.
     */
    public EvaluacionCredito evaluar(String cedula) throws ExcepcionServicio {
        String valor = validacion.validarCedula(cedula);
        return conConexion("evaluar al cliente", cn -> {
            Cliente cliente = clienteDao.buscarPorCedula(cn, valor).orElse(null);
            if (cliente == null) {
                return new EvaluacionCredito(valor, null, List.of(MotivoRechazo.NO_ES_CLIENTE));
            }
            return new EvaluacionCredito(valor, cliente, motivos(clienteDao.reglas(cn, cliente.codigo())));
        });
    }

    /**
     * Servicio 2: ((promedio de depósitos − promedio de retiros) · porcentaje)
     * · plazo, con los movimientos de los últimos 3 meses y nunca menor que 0.
     */
    public MontoMaximo montoMaximo(Integer codCliente) throws ExcepcionServicio {
        int codigo = validacion.validarCodigo(codCliente);
        return conConexion("calcular el monto máximo", cn -> {
            buscar(cn, codigo);
            return montoMaximo(cn, codigo);
        });
    }

    /**
     * Servicio 3. Vuelve a comprobar las reglas y el monto máximo dentro de la
     * transacción, con el cliente bloqueado: no confía en lo que el llamador
     * haya consultado antes. Así un reintento o dos ventas simultáneas no
     * crean dos créditos activos.
     */
    public Credito otorgar(Integer codCliente, BigDecimal monto, Integer numCuotas) throws ExcepcionServicio {
        int codigo = validacion.validarCodigo(codCliente);
        BigDecimal valor = validacion.validarMonto(monto);
        int cuotas = validacion.validarCuotas(numCuotas);
        List<Cuota> tabla = calcular(valor, cuotas);
        return enTransaccion(cn -> {
            Cliente cliente = clienteDao.bloquear(cn, codigo)
                    .orElseThrow(() -> new ExcepcionServicio(CodigoError.CLI_001));
            List<MotivoRechazo> rechazos = motivos(clienteDao.reglas(cn, cliente.codigo()));
            if (!rechazos.isEmpty()) {
                throw new ExcepcionServicio(CodigoError.CRE_001, CodigoError.CRE_001.getMensaje() + ": "
                        + rechazos.stream().map(MotivoRechazo::getDescripcion).collect(Collectors.joining(", ")));
            }
            BigDecimal maximo = montoMaximo(cn, codigo).montoMaximo();
            if (valor.compareTo(maximo) > 0) {
                throw new ExcepcionServicio(CodigoError.CRE_002, CodigoError.CRE_002.getMensaje() + " (" + maximo + ")");
            }
            BigDecimal valorCuota = tabla.get(0).valorCuota();
            int codCredito = creditoDao.insertar(cn, codigo, valor, cuotas, tasaAnual, valorCuota);
            cuotaDao.insertar(cn, codCredito, tabla);
            return new Credito(codCredito, codigo, LocalDate.now(), valor, cuotas, tasaAnual, valorCuota,
                    "ACTIVO", tabla);
        });
    }

    /**
     * Servicio 4.
     */
    public List<Cuota> tabla(Integer codCredito) throws ExcepcionServicio {
        return consultarCredito(codCredito).cuotas();
    }

    public Credito consultarCredito(Integer codCredito) throws ExcepcionServicio {
        int codigo = validacion.validarCodigo(codCredito);
        return conConexion("consultar el crédito", cn -> {
            Credito credito = creditoDao.buscar(cn, codigo)
                    .orElseThrow(() -> new ExcepcionServicio(CodigoError.CRE_004));
            return new Credito(credito.codigo(), credito.codCliente(), credito.fecha(), credito.monto(),
                    credito.numCuotas(), credito.tasaAnual(), credito.valorCuota(), credito.estado(),
                    cuotaDao.listar(cn, codigo));
        });
    }

    public List<Credito> creditosDeCliente(Integer codCliente) throws ExcepcionServicio {
        int codigo = validacion.validarCodigo(codCliente);
        return conConexion("consultar los créditos", cn -> {
            buscar(cn, codigo);
            return creditoDao.listarPorCliente(cn, codigo);
        });
    }

    /**
     * Tabla de amortización sin guardar nada, para mostrarla antes de vender.
     */
    public List<Cuota> simular(String monto, String numCuotas) throws ExcepcionServicio {
        return calcular(validacion.validarMonto(monto), validacion.validarCuotas(numCuotas));
    }

    /**
     * Un monto muy pequeño para el plazo deja cuotas sin capital: se rechaza
     * como monto inválido antes de llegar a la base de datos.
     */
    private List<Cuota> calcular(BigDecimal monto, int numCuotas) throws ExcepcionServicio {
        List<Cuota> tabla = CalculadoraAmortizacion.tabla(monto, numCuotas, tasaAnual);
        for (Cuota cuota : tabla) {
            if (cuota.capital().signum() <= 0 || cuota.interes().signum() < 0 || cuota.saldo().signum() < 0) {
                throw new ExcepcionServicio(CodigoError.CRE_005, "El monto es demasiado pequeño para "
                        + numCuotas + " cuotas");
            }
        }
        return tabla;
    }

    private MontoMaximo montoMaximo(Connection cn, int codCliente) throws SQLException {
        Promedios promedios = movimientoDao.promedios(cn, codCliente, MESES_PROMEDIO);
        BigDecimal monto = promedios.depositos().subtract(promedios.retiros())
                .multiply(porcentajeMonto).divide(CIEN)
                .multiply(BigDecimal.valueOf(plazoMeses))
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        return new MontoMaximo(codCliente, promedios.depositos().setScale(2, RoundingMode.HALF_UP),
                promedios.retiros().setScale(2, RoundingMode.HALF_UP), porcentajeMonto, plazoMeses, monto);
    }

    private static List<MotivoRechazo> motivos(ReglasCredito reglas) {
        List<MotivoRechazo> motivos = new ArrayList<>();
        if (!reglas.depositoUltimoMes()) {
            motivos.add(MotivoRechazo.SIN_DEPOSITO_ULTIMO_MES);
        }
        if (reglas.hombreMenor25()) {
            motivos.add(MotivoRechazo.HOMBRE_MENOR_25);
        }
        if (reglas.creditoActivo()) {
            motivos.add(MotivoRechazo.CREDITO_ACTIVO);
        }
        return motivos;
    }

    private Cliente buscar(Connection cn, int codigo) throws SQLException, ExcepcionServicio {
        return clienteDao.buscar(cn, codigo).orElseThrow(() -> new ExcepcionServicio(CodigoError.CLI_001));
    }

    private <T> T conConexion(String accion, OperacionBD<T> operacion) throws ExcepcionServicio {
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            return operacion.ejecutar(cn);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al " + accion, e);
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }

    /**
     * READ COMMITTED: después de esperar el bloqueo del cliente, las reglas se
     * leen con lo último confirmado, incluido un crédito que otra transacción
     * acaba de otorgar.
     */
    private <T> T enTransaccion(OperacionBD<T> operacion) throws ExcepcionServicio {
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            cn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            cn.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(cn);
                cn.commit();
                return resultado;
            } catch (ExcepcionServicio | SQLException | RuntimeException e) {
                revertir(cn);
                throw e;
            }
        } catch (SQLException | RuntimeException e) {
            LOG.log(Level.SEVERE, "Transacción revertida por un error del sistema", e);
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
