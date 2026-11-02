package ec.edu.monster.servicios;

import ec.edu.monster.datos.ConexionBD;
import ec.edu.monster.datos.TelefonoDao;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.Telefono;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mantenimiento del catálogo de teléfonos: consultar, crear, editar y
 * eliminar (baja lógica).
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioTelefono {

    private static final Logger LOG = Logger.getLogger(ServicioTelefono.class.getName());
    private static final int LARGO_MARCA = 50;
    private static final int LARGO_MODELO = 100;

    @FunctionalInterface
    private interface OperacionBD<T> {

        T ejecutar(Connection cn) throws SQLException, ExcepcionServicio;
    }

    @Inject
    private ServicioValidacion validacion;

    @Inject
    private TelefonoDao telefonoDao;

    public List<Telefono> listar() throws ExcepcionServicio {
        return conConexion("listar los teléfonos", telefonoDao::listarActivos);
    }

    /**
     * Devuelve el teléfono aunque esté INACTIVO: las facturas antiguas lo
     * siguen referenciando.
     */
    public Telefono buscar(Integer codigo) throws ExcepcionServicio {
        int valor = validacion.validarCodigo(codigo);
        return conConexion("consultar el teléfono", cn -> buscar(cn, valor));
    }

    public Telefono crear(String marca, String modelo, BigDecimal precio) throws ExcepcionServicio {
        String m = validacion.validarTexto(marca, "marca", LARGO_MARCA);
        String mo = validacion.validarTexto(modelo, "modelo", LARGO_MODELO);
        BigDecimal p = validacion.validarPrecio(precio);
        return conConexion("crear el teléfono", cn -> buscar(cn, telefonoDao.insertar(cn, m, mo, p)));
    }

    /**
     * PUT reemplaza marca, modelo y precio: los tres son obligatorios.
     */
    public Telefono actualizar(Integer codigo, String marca, String modelo, BigDecimal precio)
            throws ExcepcionServicio {
        int valor = validacion.validarCodigo(codigo);
        String m = validacion.validarTexto(marca, "marca", LARGO_MARCA);
        String mo = validacion.validarTexto(modelo, "modelo", LARGO_MODELO);
        BigDecimal p = validacion.validarPrecio(precio);
        return conConexion("actualizar el teléfono", cn -> {
            if (!telefonoDao.actualizar(cn, valor, m, mo, p)) {
                throw new ExcepcionServicio(CodigoError.TEL_001);
            }
            return buscar(cn, valor);
        });
    }

    /**
     * Idempotente: eliminar un teléfono ya INACTIVO no es un error.
     */
    public void eliminar(Integer codigo) throws ExcepcionServicio {
        int valor = validacion.validarCodigo(codigo);
        conConexion("eliminar el teléfono", cn -> {
            if (!telefonoDao.inactivar(cn, valor)) {
                throw new ExcepcionServicio(CodigoError.TEL_001);
            }
            return null;
        });
    }

    private Telefono buscar(Connection cn, int codigo) throws SQLException, ExcepcionServicio {
        return telefonoDao.buscar(cn, codigo).orElseThrow(() -> new ExcepcionServicio(CodigoError.TEL_001));
    }

    private <T> T conConexion(String accion, OperacionBD<T> operacion) throws ExcepcionServicio {
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            return operacion.ejecutar(cn);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al " + accion, e);
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }
}
