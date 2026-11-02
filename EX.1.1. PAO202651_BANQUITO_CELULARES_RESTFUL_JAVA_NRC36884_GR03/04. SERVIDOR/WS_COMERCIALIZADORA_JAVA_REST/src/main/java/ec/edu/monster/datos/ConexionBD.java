package ec.edu.monster.datos;

import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.servicios.ExcepcionServicio;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton de acceso a la base de datos comercializadora. Lee db.properties y carga
 * el driver una sola vez; cada llamada a {@link #obtenerConexion()} entrega una
 * conexión nueva que el llamador cierra. No se comparte una única conexión
 * porque cada transacción (commit/rollback, FOR UPDATE) necesita la suya.
 *
 * @author carlo
 */
public final class ConexionBD {

    private static final Logger LOG = Logger.getLogger(ConexionBD.class.getName());
    private static final String ARCHIVO = "/db.properties";

    private final String url;
    private final String usuario;
    private final String clave;

    /**
     * Si la configuración falla no se lanza la excepción aquí (rompería la
     * carga de la clase): se guarda y cada obtenerConexion responde SYS-001.
     */
    private ConexionBD() {
        Properties propiedades = new Properties();
        try (InputStream entrada = ConexionBD.class.getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                LOG.log(Level.SEVERE, "No se encontró {0} en el classpath", ARCHIVO);
            } else {
                propiedades.load(entrada);
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "No se pudo cargar la configuración de la base de datos", e);
        }
        url = propiedades.getProperty("db.url");
        usuario = propiedades.getProperty("db.usuario");
        clave = propiedades.getProperty("db.clave");
    }

    /**
     * Holder estático: la JVM crea la instancia una sola vez, al primer uso, y
     * de forma segura entre hilos.
     */
    private static final class Holder {

        private static final ConexionBD INSTANCIA = new ConexionBD();
    }

    public static ConexionBD getInstancia() {
        return Holder.INSTANCIA;
    }

    public Connection obtenerConexion() throws ExcepcionServicio {
        if (url == null) {
            throw new ExcepcionServicio(CodigoError.SYS_001);
        }
        try {
            return DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "No se pudo conectar a la base de datos: {0}", e.getMessage());
            throw new ExcepcionServicio(CodigoError.SYS_001, e);
        }
    }
}
