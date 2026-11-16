package ec.edu.monster.modelo.dao;

import ec.edu.monster.modelo.excepcion.ExcepcionPersistencia;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entrega conexiones JDBC a MySQL. Cada llamada abre una conexión nueva que el
 * llamador cierra con try-with-resources; no se comparte una sola conexión
 * porque las transacciones (commit/rollback) necesitan la suya.
 * <p>
 * Los datos de acceso llegan por el constructor (desde config.properties), en
 * lugar de estar escritos en el código como en el enunciado. App crea una
 * única instancia y la inyecta en los DAO.
 *
 * @author carlo
 */
public final class ConexionBD {

    private static final Logger LOG = Logger.getLogger(ConexionBD.class.getName());

    private final String url;
    private final String usuario;
    private final String clave;

    public ConexionBD(String url, String usuario, String clave) {
        this.url = Objects.requireNonNull(url, "url");
        this.usuario = Objects.requireNonNull(usuario, "usuario");
        this.clave = Objects.requireNonNull(clave, "clave");
    }

    /**
     * @throws ExcepcionPersistencia si MySQL no está disponible o rechaza el acceso
     */
    public Connection obtenerConexion() {
        try {
            // JDBC 4 registra el driver solo (ServiceLoader): no hace falta Class.forName
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo conectar a MySQL: {0}", e.getMessage());
            throw new ExcepcionPersistencia(
                    "No se pudo conectar a la base de datos. Verifique que MySQL esté en ejecución.", e);
        }
    }
}
