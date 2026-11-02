package ec.edu.monster.datos;

import ec.edu.monster.modelo.DetalleFactura;
import jakarta.enterprise.context.ApplicationScoped;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a las líneas de las facturas (tabla DETALLE_FACTURA).
 *
 * @author carlo
 */
@ApplicationScoped
public class DetalleFacturaDao {

    private static final String SQL_INSERTAR = """
            INSERT INTO DETALLE_FACTURA (NUM_FACTURA, COD_TELEFONO, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SQL_LISTAR = """
            SELECT d.COD_TELEFONO, t.MARCA, t.MODELO, d.CANTIDAD, d.PRECIO_UNITARIO, d.SUBTOTAL
            FROM DETALLE_FACTURA d
            JOIN TELEFONO t ON t.COD_TELEFONO = d.COD_TELEFONO
            WHERE d.NUM_FACTURA = ?
            ORDER BY d.COD_TELEFONO
            """;

    /**
     * Inserta todas las líneas en un solo lote; debe ir en la misma
     * transacción que el encabezado.
     */
    public void insertar(Connection cn, int numFactura, List<DetalleFactura> detalles) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR)) {
            for (DetalleFactura detalle : detalles) {
                ps.setInt(1, numFactura);
                ps.setInt(2, detalle.codTelefono());
                ps.setInt(3, detalle.cantidad());
                ps.setBigDecimal(4, detalle.precioUnitario());
                ps.setBigDecimal(5, detalle.subtotal());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<DetalleFactura> listar(Connection cn, int numFactura) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_LISTAR)) {
            ps.setInt(1, numFactura);
            try (ResultSet rs = ps.executeQuery()) {
                List<DetalleFactura> detalles = new ArrayList<>();
                while (rs.next()) {
                    detalles.add(new DetalleFactura(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4),
                            rs.getBigDecimal(5), rs.getBigDecimal(6)));
                }
                return detalles;
            }
        }
    }
}
