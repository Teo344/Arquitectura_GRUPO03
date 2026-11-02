package ec.edu.monster.pruebas;

import ec.edu.monster.datos.ConexionBD;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Prueba del singleton de conexión contra el MySQL local con los datos de
 * 03. BDD/comercializadora/02_carga_datos.sql.
 *
 * @author carlo
 */
public class PruebaConexion {

    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaConexion ===");
        comprobar("getInstancia devuelve siempre la misma instancia",
                ConexionBD.getInstancia() == ConexionBD.getInstancia());

        try (Connection a = ConexionBD.getInstancia().obtenerConexion();
                Connection b = ConexionBD.getInstancia().obtenerConexion()) {
            comprobar("cada llamada entrega una conexión distinta", a != b);
            try (Statement st = a.createStatement()) {
                comprobar("USUARIO tiene 2 registros", contar(st, "SELECT COUNT(*) FROM USUARIO") == 2);
                comprobar("TELEFONO tiene al menos los 8 de la carga",
                        contar(st, "SELECT COUNT(*) FROM TELEFONO") >= 8);
                comprobar("MONSTER/MONSTER9 está ACTIVO", contar(st, "SELECT COUNT(*) FROM USUARIO WHERE USUARIO ="
                        + " 'MONSTER' AND CLAVE = SHA2('MONSTER9', 256) AND ESTADO = 'ACTIVO'") == 1);
            }
        }

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static int contar(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
