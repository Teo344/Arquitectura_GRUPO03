package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de la consulta de cuenta (RQF-003) y de movimientos (RQF-004).
 * Requiere la BD recién cargada con los datos semilla.
 *
 * @author carlo
 */
public class PruebaConsultas {

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_REST/api/";
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaConsultas ===");
        String login = enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        String token = login.substring(inicio, login.indexOf("\"", inicio));

        String cuenta = enviar("GET", "cuentas/00100001", token, null);
        comprobar("00100001 tiene saldo 6900.00", cuenta.contains("\"saldo\":6900.00"));
        comprobar("00100001 es de ALAN ALBERTO ARANDA LUNA", cuenta.contains("\"titular\":\"ALAN ALBERTO ARANDA LUNA\""));
        comprobar("00100001 está ACTIVO", cuenta.contains("\"estado\":\"ACTIVO\""));

        String cancelada = enviar("GET", "cuentas/00300001", token, null);
        comprobar("00300001 se consulta con OK-000", cancelada.contains("\"codigo\":\"OK-000\""));
        comprobar("00300001 muestra el estado CANCELADO", cancelada.contains("\"estado\":\"CANCELADO\""));

        comprobar("cuenta '1234' da 400 y DATA-001",
                enviar("GET", "cuentas/1234", token, null).contains("DATA-001") && estadoHttp == 400);
        comprobar("cuenta 99999999 da CTA-001", enviar("GET", "cuentas/99999999", token, null).contains("CTA-001"));

        String movimientos = enviar("GET", "cuentas/00100001/movimientos", token, null);
        comprobar("00100001 tiene 7 movimientos", contar(movimientos, "\"numero\":") == 7);
        comprobar("el primer movimiento es la apertura", movimientos.indexOf("\"numero\":1,")
                < movimientos.indexOf("\"numero\":2,") && movimientos.contains("Apertura de Cuenta"));
        comprobar("movimientos de 99999999 da CTA-001",
                enviar("GET", "cuentas/99999999/movimientos", token, null).contains("CTA-001"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static int contar(String texto, String buscado) {
        int total = 0;
        for (int i = texto.indexOf(buscado); i >= 0; i = texto.indexOf(buscado, i + 1)) {
            total++;
        }
        return total;
    }

    private static String enviar(String metodo, String ruta, String token, String json) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL + ruta).toURL().openConnection();
        conexion.setRequestMethod(metodo);
        if (token != null) {
            conexion.setRequestProperty("Authorization", "Bearer " + token);
        }
        if (json != null) {
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        }
        estadoHttp = conexion.getResponseCode();
        InputStream entrada = estadoHttp < 400 ? conexion.getInputStream() : conexion.getErrorStream();
        return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
