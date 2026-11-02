package ec.edu.monster.datos;

import ec.edu.monster.modelo.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Consulta de credenciales. No filtra por estado para que el servicio
 * distinga AUTH-001 (no existe o clave incorrecta) de AUTH-002 (anulado).
 *
 * @author carlo
 */
@ApplicationScoped
public class UsuarioDao {

    private static final String SQL_BUSCAR = """
            SELECT COD_USUARIO, USUARIO, NOMBRE, ESTADO
            FROM USUARIO
            WHERE USUARIO = ? AND CLAVE = ?
            """;

    public Optional<Usuario> buscar(Connection cn, String usuario, String hashClave) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            ps.setString(1, usuario);
            ps.setString(2, hashClave);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Usuario(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
            }
        }
    }
}
