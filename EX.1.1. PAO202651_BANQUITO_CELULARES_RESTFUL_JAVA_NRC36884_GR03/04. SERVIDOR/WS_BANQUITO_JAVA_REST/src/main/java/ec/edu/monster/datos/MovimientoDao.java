package ec.edu.monster.datos;

import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Promedios de los movimientos de un cliente (todas sus cuentas) para el
 * monto máximo de crédito; misma ventana que la consulta 3 de
 * 03_consultas.sql.
 *
 * @author carlo
 */
@ApplicationScoped
public class MovimientoDao {

    /**
     * Sin depósitos o sin retiros en la ventana, el promedio es 0.
     */
    public record Promedios(BigDecimal depositos, BigDecimal retiros) {
    }

    private static final String SQL_PROMEDIOS = """
            SELECT COALESCE(AVG(CASE WHEN m.TIPO = 'DEP' THEN m.VALOR END), 0),
                   COALESCE(AVG(CASE WHEN m.TIPO = 'RET' THEN m.VALOR END), 0)
            FROM CUENTA cu
            JOIN MOVIMIENTO m ON m.NUM_CUENTA = cu.NUM_CUENTA
            WHERE cu.COD_CLIENTE = ?
              AND m.FECHA >= DATE_SUB(CURDATE(), INTERVAL ? MONTH)
            """;

    public Promedios promedios(Connection cn, int codCliente, int meses) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_PROMEDIOS)) {
            ps.setInt(1, codCliente);
            ps.setInt(2, meses);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new Promedios(rs.getBigDecimal(1), rs.getBigDecimal(2));
            }
        }
    }
}
