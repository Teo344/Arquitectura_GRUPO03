package ec.edu.monster.datos;

import ec.edu.monster.modelo.Cuenta;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Acceso a la tabla cuenta: consulta (RQF-003) y bloqueo y actualización de
 * saldo dentro de una transacción (RN-07).
 *
 * @author carlo
 */
@ApplicationScoped
public class CuentaDao {

    private static final String SQL_BUSCAR = """
            SELECT c.chr_cuencodigo,
                   CONCAT(cl.vch_clienombre, ' ', cl.vch_cliepaterno, ' ', cl.vch_cliematerno),
                   c.chr_monecodigo, m.vch_monedescripcion, c.dec_cuensaldo, c.vch_cuenestado,
                   s.vch_sucunombre, c.dtt_cuenfechacreacion, c.int_cuencontmov
            FROM cuenta c
            JOIN cliente  cl ON cl.chr_cliecodigo = c.chr_cliecodigo
            JOIN moneda   m  ON m.chr_monecodigo  = c.chr_monecodigo
            JOIN sucursal s  ON s.chr_sucucodigo  = c.chr_sucucodigo
            WHERE c.chr_cuencodigo = ?
            """;

    private static final String SQL_BLOQUEAR = """
            SELECT chr_cuencodigo, chr_monecodigo, dec_cuensaldo, vch_cuenestado, int_cuencontmov
            FROM cuenta
            WHERE chr_cuencodigo = ?
            FOR UPDATE
            """;

    private static final String SQL_ACTUALIZAR = """
            UPDATE cuenta SET dec_cuensaldo = ?, int_cuencontmov = ?
            WHERE chr_cuencodigo = ?
            """;

    public Optional<Cuenta> buscar(Connection cn, String codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Cuenta(rs.getString(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getBigDecimal(5), rs.getString(6), rs.getString(7),
                        rs.getDate(8).toLocalDate(), rs.getInt(9)));
            }
        }
    }

    /**
     * SELECT … FOR UPDATE: la fila queda bloqueada hasta el commit o rollback
     * de la conexión, que debe tener autoCommit desactivado.
     */
    public Optional<Cuenta> bloquear(Connection cn, String codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_BLOQUEAR)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Cuenta(rs.getString(1), null, rs.getString(2), null,
                        rs.getBigDecimal(3), rs.getString(4), null, null, rs.getInt(5)));
            }
        }
    }

    public void actualizar(Connection cn, String codigo, BigDecimal saldo, int contador) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setBigDecimal(1, saldo);
            ps.setInt(2, contador);
            ps.setString(3, codigo);
            ps.executeUpdate();
        }
    }
}
