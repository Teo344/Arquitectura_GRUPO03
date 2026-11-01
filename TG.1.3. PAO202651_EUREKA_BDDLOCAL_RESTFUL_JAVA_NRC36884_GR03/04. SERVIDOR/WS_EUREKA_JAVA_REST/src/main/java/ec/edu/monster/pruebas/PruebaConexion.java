package ec.edu.monster.pruebas;

import ec.edu.monster.datos.ConexionBD;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

/**
 * Prueba del singleton de conexión contra el MySQL local. Los conteos son los
 * de la consulta 1 de 03_consultas.sql (solo tablas que el sistema no
 * modifica).
 *
 * @author carlo
 */
public class PruebaConexion {

    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaConexion ===");
        comprobar("getInstancia devuelve siempre la misma instancia",
                ConexionBD.getInstancia() == ConexionBD.getInstancia());

        Map<String, Integer> esperados = Map.of("moneda", 2, "tipomovimiento", 10, "sucursal", 7,
                "empleado", 16, "usuario", 13, "cliente", 20, "cuenta", 6);
        try (Connection a = ConexionBD.getInstancia().obtenerConexion();
                Connection b = ConexionBD.getInstancia().obtenerConexion()) {
            comprobar("cada llamada entrega una conexión distinta", a != b);
            try (Statement st = a.createStatement()) {
                for (Map.Entry<String, Integer> tabla : esperados.entrySet()) {
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla.getKey())) {
                        rs.next();
                        comprobar(tabla.getKey() + " tiene " + tabla.getValue() + " registros",
                                rs.getInt(1) == tabla.getValue());
                    }
                }
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
