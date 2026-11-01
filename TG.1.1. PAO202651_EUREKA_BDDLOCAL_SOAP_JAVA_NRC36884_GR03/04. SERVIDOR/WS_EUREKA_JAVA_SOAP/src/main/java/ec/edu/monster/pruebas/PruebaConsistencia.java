package ec.edu.monster.pruebas;

import ec.edu.monster.datos.ConexionBD;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Prueba de integridad (RNF-008) con la consulta 5 de 03_consultas.sql: en
 * cada cuenta el saldo es la suma de sus movimientos y el contador es el
 * número del último movimiento. Se ejecuta al final de las demás pruebas.
 *
 * @author carlo
 */
public class PruebaConsistencia {

    private static final String SQL = """
            SELECT c.chr_cuencodigo,
                   c.dec_cuensaldo = COALESCE(SUM(CASE t.vch_tipoaccion
                                WHEN 'INGRESO' THEN mv.dec_moviimporte
                                ELSE -mv.dec_moviimporte END), 0) AS ok_saldo,
                   c.int_cuencontmov = COALESCE(MAX(mv.int_movinumero), 0) AS ok_contador
            FROM cuenta c
            LEFT JOIN movimiento mv    ON mv.chr_cuencodigo = c.chr_cuencodigo
            LEFT JOIN tipomovimiento t ON t.chr_tipocodigo  = mv.chr_tipocodigo
            GROUP BY c.chr_cuencodigo, c.dec_cuensaldo, c.int_cuencontmov
            ORDER BY c.chr_cuencodigo
            """;

    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaConsistencia ===");
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion();
                Statement st = cn.createStatement();
                ResultSet rs = st.executeQuery(SQL)) {
            while (rs.next()) {
                comprobar(rs.getString(1) + ": saldo igual a la suma de sus movimientos", rs.getInt(2) == 1);
                comprobar(rs.getString(1) + ": contador igual al último movimiento", rs.getInt(3) == 1);
            }
        }

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
