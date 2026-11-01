package ec.edu.monster.datos;

import ec.edu.monster.modelo.Empleado;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Consulta de credenciales (RQF-001). No filtra por estado para que el
 * servicio distinga AUTH-001 (no existe) de AUTH-002 (no activo).
 *
 * @author carlo
 */
@ApplicationScoped
public class UsuarioDao {

    private static final String SQL_BUSCAR = """
            SELECT u.chr_emplcodigo, u.vch_emplusuario,
                   CONCAT(e.vch_emplnombre, ' ', e.vch_emplpaterno, ' ', e.vch_emplmaterno),
                   u.vch_emplestado, s.chr_sucucodigo, s.vch_sucunombre
            FROM usuario u
            JOIN empleado e ON e.chr_emplcodigo = u.chr_emplcodigo
            LEFT JOIN asignado a ON a.chr_emplcodigo = u.chr_emplcodigo AND a.dtt_asigfechabaja IS NULL
            LEFT JOIN sucursal s ON s.chr_sucucodigo = a.chr_sucucodigo
            WHERE u.vch_emplusuario = ? AND u.vch_emplclave = ?
            ORDER BY a.dtt_asigfechaalta DESC
            LIMIT 1
            """;

    public Optional<Empleado> buscar(Connection cn, String usuario, String hashClave) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            ps.setString(1, usuario);
            ps.setString(2, hashClave);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Empleado(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getString(6)));
            }
        }
    }
}
