package ec.edu.monster.servicios;

import ec.edu.monster.datos.ConexionBD;
import ec.edu.monster.datos.CuentaDao;
import ec.edu.monster.datos.MovimientoDao;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.Cuenta;
import ec.edu.monster.modelo.Movimiento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Consultas de solo lectura: datos de la cuenta (RQF-003) y movimientos
 * (RQF-004). Una cuenta no activa se consulta igual y muestra su estado.
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioCuenta {

    private static final Logger LOG = Logger.getLogger(ServicioCuenta.class.getName());

    @Inject
    private ServicioValidacion validacion;

    @Inject
    private CuentaDao cuentaDao;

    @Inject
    private MovimientoDao movimientoDao;

    public Cuenta consultarCuenta(String cuenta) throws ExcepcionServicio {
        String codigo = validacion.validarCuenta(cuenta);
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            return buscar(cn, codigo);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al consultar la cuenta", e);
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }

    /**
     * Una cuenta sin movimientos devuelve una lista vacía, no un error.
     */
    public List<Movimiento> consultarMovimientos(String cuenta) throws ExcepcionServicio {
        String codigo = validacion.validarCuenta(cuenta);
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            buscar(cn, codigo);
            return movimientoDao.listar(cn, codigo);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al consultar movimientos", e);
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }

    private Cuenta buscar(Connection cn, String codigo) throws SQLException, ExcepcionServicio {
        return cuentaDao.buscar(cn, codigo).orElseThrow(() -> new ExcepcionServicio(CodigoError.CTA_001));
    }
}
