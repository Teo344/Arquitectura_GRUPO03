package ec.edu.monster.datos;

import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.FormaPago;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso al encabezado de las facturas (tabla FACTURA). Las facturas se leen
 * sin detalle (DetalleFacturaDao).
 *
 * @author carlo
 */
@ApplicationScoped
public class FacturaDao {

    /**
     * Límite de la lista de facturas: las más recientes primero.
     */
    private static final int LIMITE = 100;

    private static final String COLUMNAS = """
            SELECT f.NUM_FACTURA, f.FECHA, u.USUARIO, f.CEDULA_CLIENTE, f.COD_CLIENTE_BANCO, f.NOMBRE_CLIENTE,
                   f.FORMA_PAGO, f.SUBTOTAL, f.DESCUENTO, f.TOTAL, f.COD_CREDITO_BANCO, f.NUM_CUOTAS
            FROM FACTURA f
            JOIN USUARIO u ON u.COD_USUARIO = f.COD_USUARIO
            """;

    private static final String SQL_BUSCAR = COLUMNAS + "WHERE f.NUM_FACTURA = ?";
    private static final String SQL_TODAS = COLUMNAS + "ORDER BY f.NUM_FACTURA DESC LIMIT " + LIMITE;
    private static final String SQL_POR_CEDULA = COLUMNAS
            + "WHERE f.CEDULA_CLIENTE = ? ORDER BY f.NUM_FACTURA DESC LIMIT " + LIMITE;

    private static final String SQL_INSERTAR = """
            INSERT INTO FACTURA (FECHA, COD_USUARIO, CEDULA_CLIENTE, COD_CLIENTE_BANCO, NOMBRE_CLIENTE, FORMA_PAGO,
                                 SUBTOTAL, DESCUENTO, TOTAL, COD_CREDITO_BANCO, NUM_CUOTAS)
            VALUES (NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    /**
     * Inserta el encabezado (los campos de detalles y fecha de la factura se
     * ignoran) y devuelve el número generado.
     */
    public int insertar(Connection cn, int codUsuario, Factura factura) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, codUsuario);
            ps.setString(2, factura.cedula());
            ps.setInt(3, factura.codClienteBanco());
            ps.setString(4, factura.nombreCliente());
            ps.setString(5, factura.formaPago().name());
            ps.setBigDecimal(6, factura.subtotal());
            ps.setBigDecimal(7, factura.descuento());
            ps.setBigDecimal(8, factura.total());
            if (factura.codCreditoBanco() == null) {
                ps.setNull(9, Types.INTEGER);
                ps.setNull(10, Types.INTEGER);
            } else {
                ps.setInt(9, factura.codCreditoBanco());
                ps.setInt(10, factura.numCuotas());
            }
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    public Optional<Factura> buscar(Connection cn, int numero) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Con cedula null lista las últimas facturas de todos los clientes.
     */
    public List<Factura> listar(Connection cn, String cedula) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(cedula == null ? SQL_TODAS : SQL_POR_CEDULA)) {
            if (cedula != null) {
                ps.setString(1, cedula);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Factura> facturas = new ArrayList<>();
                while (rs.next()) {
                    facturas.add(leer(rs));
                }
                return facturas;
            }
        }
    }

    private static Factura leer(ResultSet rs) throws SQLException {
        return new Factura(rs.getInt(1), rs.getTimestamp(2).toLocalDateTime(), rs.getString(3), rs.getString(4),
                rs.getInt(5), rs.getString(6), FormaPago.valueOf(rs.getString(7)), rs.getBigDecimal(8),
                rs.getBigDecimal(9), rs.getBigDecimal(10), rs.getObject(11, Integer.class),
                rs.getObject(12, Integer.class), List.of());
    }
}
