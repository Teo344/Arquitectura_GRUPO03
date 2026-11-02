package ec.edu.monster.datos;

import ec.edu.monster.modelo.Cuota;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a la tabla CUOTA (tabla de amortización de cada crédito).
 *
 * @author carlo
 */
@ApplicationScoped
public class CuotaDao {

    private static final String SQL_INSERTAR = """
            INSERT INTO CUOTA (COD_CREDITO, NUM_CUOTA, VALOR_CUOTA, INTERES_PAGADO, CAPITAL_PAGADO, SALDO)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_LISTAR = """
            SELECT NUM_CUOTA, VALOR_CUOTA, INTERES_PAGADO, CAPITAL_PAGADO, SALDO
            FROM CUOTA
            WHERE COD_CREDITO = ?
            ORDER BY NUM_CUOTA
            """;

    /**
     * Inserta toda la tabla en un solo lote; debe ir en la misma transacción
     * que el crédito.
     */
    public void insertar(Connection cn, int codCredito, List<Cuota> cuotas) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR)) {
            for (Cuota cuota : cuotas) {
                ps.setInt(1, codCredito);
                ps.setInt(2, cuota.numero());
                ps.setBigDecimal(3, cuota.valorCuota());
                ps.setBigDecimal(4, cuota.interes());
                ps.setBigDecimal(5, cuota.capital());
                ps.setBigDecimal(6, cuota.saldo());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Cuota> listar(Connection cn, int codCredito) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_LISTAR)) {
            ps.setInt(1, codCredito);
            try (ResultSet rs = ps.executeQuery()) {
                List<Cuota> cuotas = new ArrayList<>();
                while (rs.next()) {
                    cuotas.add(new Cuota(rs.getInt(1), rs.getBigDecimal(2), rs.getBigDecimal(3), rs.getBigDecimal(4),
                            rs.getBigDecimal(5)));
                }
                return cuotas;
            }
        }
    }
}
