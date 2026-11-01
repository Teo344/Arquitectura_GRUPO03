package ec.edu.monster.datos;

import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Registro de ingreso (RQF-001) y salida (RQF-002) de cada sesión en la tabla
 * logsession.
 *
 * @author carlo
 */
@ApplicationScoped
public class LogSesionDao {

    private static final String SQL_INGRESO = """
            INSERT INTO logsession (chr_emplcodigo, dtt_logingreso, vch_logip, vch_loghostname)
            VALUES (?, NOW(), ?, ?)
            """;

    private static final String SQL_SALIDA = """
            UPDATE logsession SET dtt_logsalida = ?
            WHERE int_logid = ? AND dtt_logsalida IS NULL
            """;

    /**
     * Devuelve el int_logid generado, que la sesión guarda para el cierre.
     */
    public int registrarIngreso(Connection cn, String codigoEmpleado, String ip, String equipo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INGRESO, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, codigoEmpleado);
            ps.setString(2, recortar(ip, 45));
            ps.setString(3, recortar(equipo, 100));
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    public void registrarSalida(Connection cn, int logId, Instant salida) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_SALIDA)) {
            ps.setTimestamp(1, Timestamp.from(salida));
            ps.setInt(2, logId);
            ps.executeUpdate();
        }
    }

    private static String recortar(String texto, int maximo) {
        if (texto == null || texto.isBlank()) {
            return "NONE";
        }
        String limpio = texto.trim();
        return limpio.length() <= maximo ? limpio : limpio.substring(0, maximo);
    }
}
