package ec.edu.monster.modelo.dao;

import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.entidad.ResultadoImportacion;
import ec.edu.monster.modelo.excepcion.ExcepcionPersistencia;
import ec.edu.monster.modelo.excepcion.ExcepcionValidacion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementación JDBC de {@link PaisDAO} para MySQL 8.0.19+.
 * <ul>
 * <li>Solo PreparedStatement: los datos nunca se concatenan al SQL.</li>
 * <li>try-with-resources: conexiones, sentencias y resultados siempre se
 * cierran, aun cuando hay error (el DAO del enunciado no los cerraba).</li>
 * <li>La importación usa lote + transacción en una sola conexión, en lugar de
 * abrir una conexión por país.</li>
 * </ul>
 *
 * @author carlo
 */
public class PaisDAOMySQL implements PaisDAO {

    private static final Logger LOG = Logger.getLogger(PaisDAOMySQL.class.getName());

    /** Código de error de MySQL para clave única duplicada. */
    private static final int ERROR_DUPLICADO = 1062;

    private static final String COLUMNAS = "id, codigo, nombre, capital, region, poblacion, moneda";

    private static final String SQL_INSERTAR
            = "INSERT INTO pais (codigo, nombre, capital, region, poblacion, moneda) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR
            = "UPDATE pais SET codigo = ?, nombre = ?, capital = ?, region = ?, poblacion = ?, moneda = ? WHERE id = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM pais WHERE id = ?";
    private static final String SQL_POR_ID = "SELECT " + COLUMNAS + " FROM pais WHERE id = ?";
    private static final String SQL_POR_CODIGO = "SELECT " + COLUMNAS + " FROM pais WHERE codigo = ?";
    private static final String SQL_TODOS = "SELECT " + COLUMNAS + " FROM pais ORDER BY nombre";
    private static final String SQL_POR_REGION
            = "SELECT " + COLUMNAS + " FROM pais WHERE region = ? ORDER BY nombre";
    // La intercalación utf8mb4_unicode_ci ya ignora mayúsculas y tildes
    private static final String SQL_POR_NOMBRE
            = "SELECT " + COLUMNAS + " FROM pais WHERE nombre LIKE ? ORDER BY nombre";
    private static final String SQL_CODIGOS = "SELECT codigo FROM pais";
    // Alias de fila "nuevo" (MySQL 8.0.19+): reemplaza a VALUES(col), que está obsoleto
    private static final String SQL_UPSERT
            = "INSERT INTO pais (codigo, nombre, capital, region, poblacion, moneda) VALUES (?, ?, ?, ?, ?, ?) AS nuevo "
            + "ON DUPLICATE KEY UPDATE nombre = nuevo.nombre, capital = nuevo.capital, region = nuevo.region, "
            + "poblacion = nuevo.poblacion, moneda = nuevo.moneda";
    private static final String SQL_CONTAR_POR_REGION
            = "SELECT region, COUNT(*) AS total FROM pais GROUP BY region ORDER BY total DESC, region";
    private static final String SQL_MAS_POBLADOS
            = "SELECT " + COLUMNAS + " FROM pais ORDER BY poblacion DESC, nombre LIMIT ?";
    private static final String SQL_CONTAR = "SELECT COUNT(*) FROM pais";

    private final ConexionBD conexionBD;

    public PaisDAOMySQL(ConexionBD conexionBD) {
        this.conexionBD = Objects.requireNonNull(conexionBD, "conexionBD");
    }

    @Override
    public Pais insertar(Pais pais) {
        try (Connection con = conexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(ps, pais);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("MySQL no devolvió el id generado");
                }
                return pais.conId(claves.getInt(1));
            }
        } catch (SQLException e) {
            throw traducir(e, "registrar el país", pais.codigo());
        }
    }

    @Override
    public boolean actualizar(Pais pais) {
        Objects.requireNonNull(pais.id(), "Para actualizar el país debe tener id");
        try (Connection con = conexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            asignarDatos(ps, pais);
            ps.setInt(7, pais.id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traducir(e, "actualizar el país", pais.codigo());
        }
    }

    @Override
    public boolean eliminar(int id) {
        try (Connection con = conexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traducir(e, "eliminar el país", null);
        }
    }

    @Override
    public Optional<Pais> buscarPorId(int id) {
        return consultar(SQL_POR_ID, ps -> ps.setInt(1, id)).stream().findFirst();
    }

    @Override
    public Optional<Pais> buscarPorCodigo(String codigo) {
        return consultar(SQL_POR_CODIGO, ps -> ps.setString(1, codigo)).stream().findFirst();
    }

    @Override
    public List<Pais> listarTodos() {
        return consultar(SQL_TODOS, ps -> {
        });
    }

    @Override
    public List<Pais> buscarPorRegion(Region region) {
        return consultar(SQL_POR_REGION, ps -> ps.setString(1, region.name()));
    }

    @Override
    public List<Pais> buscarPorNombre(String fragmento) {
        String patron = "%" + escaparLike(fragmento) + "%";
        return consultar(SQL_POR_NOMBRE, ps -> ps.setString(1, patron));
    }

    @Override
    public ResultadoImportacion guardarOActualizar(List<Pais> paises) {
        if (paises.isEmpty()) {
            return ResultadoImportacion.vacio();
        }
        try (Connection con = conexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                Set<String> existentes = leerCodigos(con);
                try (PreparedStatement ps = con.prepareStatement(SQL_UPSERT)) {
                    for (Pais pais : paises) {
                        asignarDatos(ps, pais);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
                long nuevos = paises.stream().filter(p -> !existentes.contains(p.codigo())).count();
                return new ResultadoImportacion(paises.size(), (int) nuevos, paises.size() - (int) nuevos);
            } catch (SQLException e) {
                deshacer(con, e);
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, "guardar los países importados", null);
        }
    }

    @Override
    public Map<Region, Long> contarPorRegion() {
        Map<Region, Long> conteo = new LinkedHashMap<>();
        try (Connection con = conexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_CONTAR_POR_REGION);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                conteo.put(leerRegion(rs.getString("region")), rs.getLong("total"));
            }
            return conteo;
        } catch (SQLException e) {
            throw traducir(e, "contar los países por región", null);
        }
    }

    @Override
    public List<Pais> listarMasPoblados(int limite) {
        return consultar(SQL_MAS_POBLADOS, ps -> ps.setInt(1, Math.max(0, limite)));
    }

    @Override
    public long contar() {
        try (Connection con = conexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_CONTAR);
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw traducir(e, "contar los países", null);
        }
    }

    // ------------------------------------------------------------------ apoyo

    /**
     * Asigna los parámetros que necesita una consulta.
     */
    @FunctionalInterface
    private interface Parametros {

        void asignar(PreparedStatement ps) throws SQLException;
    }

    private List<Pais> consultar(String sql, Parametros parametros) {
        try (Connection con = conexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(sql)) {
            parametros.asignar(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<Pais> paises = new ArrayList<>();
                while (rs.next()) {
                    paises.add(mapear(rs));
                }
                return paises;
            }
        } catch (SQLException e) {
            throw traducir(e, "consultar los países", null);
        }
    }

    /**
     * Parámetros 1 a 6 en el orden: codigo, nombre, capital, region, poblacion, moneda.
     */
    private static void asignarDatos(PreparedStatement ps, Pais pais) throws SQLException {
        ps.setString(1, pais.codigo());
        ps.setString(2, pais.nombre());
        asignarTextoOpcional(ps, 3, pais.capital());
        ps.setString(4, pais.region().name());
        ps.setLong(5, pais.poblacion());
        asignarTextoOpcional(ps, 6, pais.moneda());
    }

    private static void asignarTextoOpcional(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, valor);
        }
    }

    private static Pais mapear(ResultSet rs) throws SQLException {
        return new Pais(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("capital"),
                leerRegion(rs.getString("region")),
                rs.getLong("poblacion"),
                rs.getString("moneda"));
    }

    private static Region leerRegion(String valor) throws SQLException {
        try {
            return Region.valueOf(valor);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new SQLException("Región no válida en la base de datos: " + valor, e);
        }
    }

    private static Set<String> leerCodigos(Connection con) throws SQLException {
        Set<String> codigos = new HashSet<>();
        try (PreparedStatement ps = con.prepareStatement(SQL_CODIGOS);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                codigos.add(rs.getString(1));
            }
        }
        return codigos;
    }

    private static void deshacer(Connection con, SQLException original) {
        try {
            con.rollback();
        } catch (SQLException e) {
            original.addSuppressed(e);
        }
    }

    /**
     * Escapa los comodines de LIKE para que "%" o "_" escritos por el usuario
     * se busquen literalmente (MySQL usa "\" como carácter de escape).
     */
    static String escaparLike(String texto) {
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /**
     * Convierte la SQLException en una excepción de la aplicación con un
     * mensaje para el usuario; el detalle técnico queda en el log.
     */
    private static RuntimeException traducir(SQLException e, String operacion, String codigo) {
        if (e.getErrorCode() == ERROR_DUPLICADO) {
            return new ExcepcionValidacion("Ya existe un país con el código " + codigo + ".");
        }
        LOG.log(Level.SEVERE, "Error de base de datos al " + operacion, e);
        return new ExcepcionPersistencia("No se pudo " + operacion + ". Intente nuevamente.", e);
    }
}
