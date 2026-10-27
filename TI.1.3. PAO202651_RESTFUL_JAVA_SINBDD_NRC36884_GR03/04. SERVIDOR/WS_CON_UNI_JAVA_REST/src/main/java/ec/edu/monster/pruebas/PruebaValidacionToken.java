package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de que los recursos protegidos exigen un token válido en la cabecera
 * Authorization.
 *
 * @author carlo
 */
public class PruebaValidacionToken {

    private static final String URL = "http://localhost:8080/WS_CON_UNI_JAVA_REST/api/";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaValidacionToken ===");
        String login = enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        String token = login.substring(inicio, login.indexOf("\"", inicio));

        comprobar("sin token da AUTH-001",
                enviar("GET", "catalogo/categorias", null, null).contains("AUTH-001"));
        comprobar("token inventado da AUTH-001",
                enviar("GET", "catalogo/categorias", "token-inventado", null).contains("AUTH-001"));
        comprobar("token válido permite consultar",
                enviar("GET", "catalogo/categorias", token, null).contains("\"estado\":\"OK\""));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
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
        InputStream entrada = conexion.getResponseCode() < 400 ? conexion.getInputStream() : conexion.getErrorStream();
        return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
