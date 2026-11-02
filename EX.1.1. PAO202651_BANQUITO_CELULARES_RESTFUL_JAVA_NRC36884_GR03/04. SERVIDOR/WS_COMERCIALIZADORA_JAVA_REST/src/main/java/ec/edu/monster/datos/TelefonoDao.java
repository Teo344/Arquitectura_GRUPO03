package ec.edu.monster.datos;

import ec.edu.monster.modelo.Telefono;
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
 * Acceso al catálogo (tabla TELEFONO). Eliminar es una baja lógica: el
 * teléfono pasa a INACTIVO y las facturas que lo vendieron no se tocan.
 *
 * @author carlo
 */
@ApplicationScoped
public class TelefonoDao {

    private static final String COLUMNAS = "SELECT COD_TELEFONO, MARCA, MODELO, PRECIO, ESTADO FROM TELEFONO ";

    private static final String SQL_ACTIVOS = COLUMNAS + "WHERE ESTADO = 'ACTIVO' ORDER BY PRECIO, COD_TELEFONO";
    private static final String SQL_BUSCAR = COLUMNAS + "WHERE COD_TELEFONO = ?";
    private static final String SQL_INSERTAR = "INSERT INTO TELEFONO (MARCA, MODELO, PRECIO, ESTADO) VALUES (?, ?, ?, 'ACTIVO')";
    private static final String SQL_ACTUALIZAR = "UPDATE TELEFONO SET MARCA = ?, MODELO = ?, PRECIO = ? WHERE COD_TELEFONO = ?";
    private static final String SQL_INACTIVAR = "UPDATE TELEFONO SET ESTADO = 'INACTIVO' WHERE COD_TELEFONO = ?";

    public List<Telefono> listarActivos(Connection cn) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_ACTIVOS); ResultSet rs = ps.executeQuery()) {
            List<Telefono> telefonos = new ArrayList<>();
            while (rs.next()) {
                telefonos.add(leer(rs));
            }
            return telefonos;
        }
    }

    public Optional<Telefono> buscar(Connection cn, int codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR)) {
            ps.setInt(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Devuelve el código generado por AUTO_INCREMENT.
     */
    public int insertar(Connection cn, String marca, String modelo, BigDecimal precio) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, marca);
            ps.setString(2, modelo);
            ps.setBigDecimal(3, precio);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    /**
     * Devuelve false si el teléfono no existe. Connector/J cuenta las filas
     * encontradas, no solo las cambiadas, así que guardar los mismos datos
     * también devuelve true.
     */
    public boolean actualizar(Connection cn, int codigo, String marca, String modelo, BigDecimal precio)
            throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, marca);
            ps.setString(2, modelo);
            ps.setBigDecimal(3, precio);
            ps.setInt(4, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean inactivar(Connection cn, int codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SQL_INACTIVAR)) {
            ps.setInt(1, codigo);
            return ps.executeUpdate() > 0;
        }
    }

    private static Telefono leer(ResultSet rs) throws SQLException {
        return new Telefono(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4), rs.getString(5));
    }
}
