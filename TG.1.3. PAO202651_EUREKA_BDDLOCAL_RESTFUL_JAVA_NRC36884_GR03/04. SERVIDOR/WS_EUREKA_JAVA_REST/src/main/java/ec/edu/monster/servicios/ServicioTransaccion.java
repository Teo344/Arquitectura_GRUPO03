package ec.edu.monster.servicios;

import ec.edu.monster.datos.ConexionBD;
import ec.edu.monster.datos.CuentaDao;
import ec.edu.monster.datos.MovimientoDao;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.Cuenta;
import ec.edu.monster.modelo.Estado;
import ec.edu.monster.modelo.TipoMovimiento;
import ec.edu.monster.seguridad.Sesion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Depósitos (RQF-005), retiros (RQF-006) y transferencias (RQF-007). Cada
 * operación es una transacción: bloquea las cuentas, actualiza saldo y
 * contador, registra los movimientos y confirma; ante cualquier error revierte
 * todo (RN-07, RNF-008).
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioTransaccion {

    private static final Logger LOG = Logger.getLogger(ServicioTransaccion.class.getName());

    /**
     * numeroMovimientoDestino solo aplica a las transferencias.
     */
    public record ResultadoTransaccion(BigDecimal nuevoSaldo, int numeroMovimiento,
            Integer numeroMovimientoDestino) {
    }

    @FunctionalInterface
    private interface OperacionTransaccional<T> {

        T ejecutar(Connection cn) throws SQLException, ExcepcionServicio;
    }

    @Inject
    private ServicioValidacion validacion;

    @Inject
    private CuentaDao cuentaDao;

    @Inject
    private MovimientoDao movimientoDao;

    public ResultadoTransaccion depositar(Sesion sesion, String cuenta, BigDecimal importe)
            throws ExcepcionServicio {
        String codigo = validacion.validarCuenta(cuenta);
        validacion.validarImporte(importe);
        return enTransaccion(cn -> {
            Cuenta actual = bloquearActiva(cn, codigo);
            BigDecimal nuevoSaldo = actual.saldo().add(importe);
            validacion.validarMaximo(nuevoSaldo);
            int numero = registrar(cn, actual, nuevoSaldo, sesion, TipoMovimiento.DEPOSITO, importe, null);
            return new ResultadoTransaccion(nuevoSaldo, numero, null);
        });
    }

    public ResultadoTransaccion retirar(Sesion sesion, String cuenta, BigDecimal importe)
            throws ExcepcionServicio {
        String codigo = validacion.validarCuenta(cuenta);
        validacion.validarImporte(importe);
        return enTransaccion(cn -> {
            Cuenta actual = bloquearActiva(cn, codigo);
            BigDecimal nuevoSaldo = restar(actual.saldo(), importe);
            int numero = registrar(cn, actual, nuevoSaldo, sesion, TipoMovimiento.RETIRO, importe, null);
            return new ResultadoTransaccion(nuevoSaldo, numero, null);
        });
    }

    /**
     * Las dos cuentas se bloquean siempre en orden de código: así dos
     * transferencias cruzadas (A→B y B→A) no se bloquean mutuamente.
     */
    public ResultadoTransaccion transferir(Sesion sesion, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe) throws ExcepcionServicio {
        String origen = validacion.validarCuenta(cuentaOrigen);
        String destino = validacion.validarCuenta(cuentaDestino);
        if (origen.equals(destino)) {
            throw new ExcepcionServicio(CodigoError.MOV_003);
        }
        validacion.validarImporte(importe);
        return enTransaccion(cn -> {
            boolean origenPrimero = origen.compareTo(destino) < 0;
            Cuenta primera = bloquear(cn, origenPrimero ? origen : destino);
            Cuenta segunda = bloquear(cn, origenPrimero ? destino : origen);
            Cuenta actualOrigen = origenPrimero ? primera : segunda;
            Cuenta actualDestino = origenPrimero ? segunda : primera;

            exigirActiva(actualOrigen);
            exigirActiva(actualDestino);
            if (!actualOrigen.codigoMoneda().equals(actualDestino.codigoMoneda())) {
                throw new ExcepcionServicio(CodigoError.MOV_004);
            }
            BigDecimal saldoOrigen = restar(actualOrigen.saldo(), importe);
            BigDecimal saldoDestino = actualDestino.saldo().add(importe);
            validacion.validarMaximo(saldoDestino);

            int numeroOrigen = registrar(cn, actualOrigen, saldoOrigen, sesion,
                    TipoMovimiento.TRANSFERENCIA_SALIDA, importe, destino);
            int numeroDestino = registrar(cn, actualDestino, saldoDestino, sesion,
                    TipoMovimiento.TRANSFERENCIA_INGRESO, importe, origen);
            return new ResultadoTransaccion(saldoOrigen, numeroOrigen, numeroDestino);
        });
    }

    private <T> T enTransaccion(OperacionTransaccional<T> operacion) throws ExcepcionServicio {
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
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

    private Cuenta bloquear(Connection cn, String codigo) throws SQLException, ExcepcionServicio {
        return cuentaDao.bloquear(cn, codigo).orElseThrow(() -> new ExcepcionServicio(CodigoError.CTA_001));
    }

    private Cuenta bloquearActiva(Connection cn, String codigo) throws SQLException, ExcepcionServicio {
        Cuenta cuenta = bloquear(cn, codigo);
        exigirActiva(cuenta);
        return cuenta;
    }

    private static void exigirActiva(Cuenta cuenta) throws ExcepcionServicio {
        if (!Estado.esActivo(cuenta.estado())) {
            throw new ExcepcionServicio(CodigoError.CTA_002);
        }
    }

    private static BigDecimal restar(BigDecimal saldo, BigDecimal importe) throws ExcepcionServicio {
        if (saldo.compareTo(importe) < 0) {
            throw new ExcepcionServicio(CodigoError.MOV_002);
        }
        return saldo.subtract(importe);
    }

    /**
     * Actualiza saldo y contador e inserta el movimiento con el número
     * siguiente. Devuelve ese número.
     */
    private int registrar(Connection cn, Cuenta cuenta, BigDecimal nuevoSaldo, Sesion sesion,
            TipoMovimiento tipo, BigDecimal importe, String referencia) throws SQLException {
        int numero = cuenta.contador() + 1;
        cuentaDao.actualizar(cn, cuenta.codigo(), nuevoSaldo, numero);
        movimientoDao.insertar(cn, cuenta.codigo(), numero, sesion.codigoEmpleado(), tipo, importe, referencia);
        return numero;
    }
}
