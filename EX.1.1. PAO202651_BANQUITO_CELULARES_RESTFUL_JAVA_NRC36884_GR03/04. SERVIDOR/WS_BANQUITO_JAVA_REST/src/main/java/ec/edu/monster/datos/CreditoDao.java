package ec.edu.monster.datos;

import ec.edu.monster.modelo.Credito;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla CREDITO. Los créditos se leen sin sus cuotas (CuotaDao).
 *
 * @author carlo
 */
@ApplicationScoped
public class CreditoDao {

    private static final String COLUMNAS = """
            SELECT COD_CREDITO, COD_CLIENTE, FECHA, MONTO, NUM_CUOTAS, TASA_ANUAL, VALOR_CUOTA, ESTADO
            FROM CREDITO
            """;

    private static final String SQL_BUSCAR = COLUMNAS + "WHERE COD_CREDITO = ?";
    private static final String SQL_POR_CLIENTE = COLUMNAS + "WHERE COD_CLIENTE = ? ORDER BY COD_CREDITO DESC";

    private static final String SQL_INSERTAR = """
            INSERT INTO CREDITO (COD_CLIENTE, FECHA, MONTO, NUM_CUOTAS, TASA_ANUAL, VALOR_CUOTA, ESTADO)
            VALUES (?, CURDATE(), ?, ?, ?, ?, 'ACTIVO')
            """;

    /**
     * Devuelve el código generado por AUTO_INCREMENT.
     */
    public int insertar(Connection cn, int codCliente, BigDecimal monto, int numCuotas, BigDecimal tasaAnual,
            BigDecimal valorCuota) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, codCliente);
            ps.setBigDecimal(2, monto);
            ps.setInt(3, numCuotas);
            ps.setBigDecimal(4, tasaAnual);
            ps.setBigDecimal(5, valorCuota);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    public Optional<Credito> buscar(Connection cn, int codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(rs)) : Optional.empty();
            }
        }
    }

    public List<Credito> listarPorCliente(Connection cn, int codCliente) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_POR_CLIENTE)) {
            ps.setInt(1, codCliente);
            try (ResultSet rs = ps.executeQuery()) {
                List<Credito> creditos = new ArrayList<>();
                while (rs.next()) {
                    creditos.add(leer(rs));
                }
                return creditos;
            }
        }
    }

    private static Credito leer(ResultSet rs) throws SQLException {
        return new Credito(rs.getInt(1), rs.getInt(2), rs.getDate(3).toLocalDate(), rs.getBigDecimal(4),
                rs.getInt(5), rs.getBigDecimal(6), rs.getBigDecimal(7), rs.getString(8), List.of());
    }
}
