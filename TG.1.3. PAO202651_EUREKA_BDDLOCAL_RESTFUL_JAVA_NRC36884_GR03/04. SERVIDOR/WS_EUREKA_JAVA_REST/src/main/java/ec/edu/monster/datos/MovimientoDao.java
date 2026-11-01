package ec.edu.monster.datos;

import ec.edu.monster.modelo.Movimiento;
import ec.edu.monster.modelo.TipoMovimiento;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla movimiento: historial (RQF-004) y registro de cada
 * transacción (RQF-005 a RQF-007).
 *
 * @author carlo
 */
@ApplicationScoped
public class MovimientoDao {

    private static final String SQL_LISTAR = """
            SELECT mv.int_movinumero, mv.dtt_movifecha, t.vch_tipodescripcion, t.vch_tipoaccion,
                   mv.dec_moviimporte, mv.chr_cuenreferencia
            FROM movimiento mv
            JOIN tipomovimiento t ON t.chr_tipocodigo = mv.chr_tipocodigo
            WHERE mv.chr_cuencodigo = ?
            ORDER BY mv.int_movinumero
            """;

    private static final String SQL_INSERTAR = """
            INSERT INTO movimiento (chr_cuencodigo, int_movinumero, dtt_movifecha, chr_emplcodigo,
                                    chr_tipocodigo, dec_moviimporte, chr_cuenreferencia)
            VALUES (?, ?, CURRENT_DATE, ?, ?, ?, ?)
            """;

    public List<Movimiento> listar(Connection cn, String cuenta) throws SQLException {
        List<Movimiento> movimientos = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(SQL_LISTAR)) {
            ps.setString(1, cuenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(new Movimiento(rs.getInt(1), rs.getDate(2).toLocalDate(), rs.getString(3),
                            rs.getString(4), rs.getBigDecimal(5), rs.getString(6)));
                }
            }
        }
        return movimientos;
    }

    /**
     * El empleado es siempre el de la sesión (RN-06); referencia es null salvo
     * en las transferencias.
     */
    public void insertar(Connection cn, String cuenta, int numero, String empleado, TipoMovimiento tipo,
            BigDecimal importe, String referencia) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR)) {
            ps.setString(1, cuenta);
            ps.setInt(2, numero);
            ps.setString(3, empleado);
            ps.setString(4, tipo.getCodigo());
            ps.setBigDecimal(5, importe);
            ps.setString(6, referencia);
            ps.executeUpdate();
        }
    }
}
