package ec.edu.monster.datos;

import ec.edu.monster.modelo.Cliente;
import ec.edu.monster.modelo.ReglasCredito;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Acceso a la tabla CLIENTE y a las reglas del sujeto de crédito. Las fechas
 * se comparan con CURDATE() de MySQL, igual que en 03_consultas.sql, así el
 * "último mes" y la edad no dependen del reloj del servidor de aplicaciones.
 *
 * @author carlo
 */
@ApplicationScoped
public class ClienteDao {

    private static final String COLUMNAS = "SELECT COD_CLIENTE, CEDULA, NOMBRE, GENERO, FECHA_NACIMIENTO FROM CLIENTE ";

    private static final String SQL_POR_CEDULA = COLUMNAS + "WHERE CEDULA = ?";
    private static final String SQL_POR_CODIGO = COLUMNAS + "WHERE COD_CLIENTE = ?";
    private static final String SQL_BLOQUEAR = COLUMNAS + "WHERE COD_CLIENTE = ? FOR UPDATE";

    /**
     * Reglas 2, 3 y 4 (consulta 2 de 03_consultas.sql) para un cliente.
     */
    private static final String SQL_REGLAS = """
            SELECT EXISTS (SELECT 1
                           FROM MOVIMIENTO m
                           JOIN CUENTA cu ON cu.NUM_CUENTA = m.NUM_CUENTA
                           WHERE cu.COD_CLIENTE = c.COD_CLIENTE
                             AND m.TIPO = 'DEP'
                             AND m.FECHA >= DATE_SUB(CURDATE(), INTERVAL 1 MONTH)),
                   c.GENERO = 'M' AND TIMESTAMPDIFF(YEAR, c.FECHA_NACIMIENTO, CURDATE()) < 25,
                   EXISTS (SELECT 1
                           FROM CREDITO cr
                           WHERE cr.COD_CLIENTE = c.COD_CLIENTE
                             AND cr.ESTADO = 'ACTIVO')
            FROM CLIENTE c
            WHERE c.COD_CLIENTE = ?
            """;

    public Optional<Cliente> buscarPorCedula(Connection cn, String cedula) throws SQLException {
        return buscar(cn, SQL_POR_CEDULA, ps -> ps.setString(1, cedula));
    }

    public Optional<Cliente> buscar(Connection cn, int codigo) throws SQLException {
        return buscar(cn, SQL_POR_CODIGO, ps -> ps.setInt(1, codigo));
    }

    /**
     * SELECT … FOR UPDATE: dos créditos simultáneos del mismo cliente se
     * otorgan uno detrás del otro. La conexión debe tener autoCommit
     * desactivado.
     */
    public Optional<Cliente> bloquear(Connection cn, int codigo) throws SQLException {
        return buscar(cn, SQL_BLOQUEAR, ps -> ps.setInt(1, codigo));
    }

    public ReglasCredito reglas(Connection cn, int codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_REGLAS)) {
            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return new ReglasCredito(rs.getBoolean(1), rs.getBoolean(2), rs.getBoolean(3));
            }
        }
    }

    @FunctionalInterface
    private interface Parametros {

        void asignar(PreparedStatement ps) throws SQLException;
    }

    private static Optional<Cliente> buscar(Connection cn, String sql, Parametros parametros) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            parametros.asignar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Cliente(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getDate(5).toLocalDate()));
            }
        }
    }
}
