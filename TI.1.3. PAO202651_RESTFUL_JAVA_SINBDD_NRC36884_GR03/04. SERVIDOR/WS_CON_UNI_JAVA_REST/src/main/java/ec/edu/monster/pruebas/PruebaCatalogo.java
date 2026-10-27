package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de la consulta del catálogo y de las unidades en el servidor REST.
 *
 * @author carlo
 */
public class PruebaCatalogo {

    private static final String URL = "http://localhost:8080/WS_CON_UNI_JAVA_REST/api/";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaCatalogo ===");
        String login = enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        String token = login.substring(inicio, login.indexOf("\"", inicio));

        String catalogo = enviar("GET", "catalogo/categorias", token, null);
        comprobar("el catálogo tiene 6 categorías", catalogo.split("\"convertible\"").length - 1 == 6);

        String masa = enviar("GET", "catalogo/categorias/MASA/unidades", token, null);
        comprobar("MASA tiene 5 unidades", masa.split("\"simbolo\"").length - 1 == 5);
        comprobar("MASA incluye el quintal (QQ)", masa.contains("\"codigo\":\"QQ\""));

        String inexistente = enviar("GET", "catalogo/categorias/xyz/unidades", token, null);
        comprobar("categoría 'xyz' da DATA-001", inexistente.contains("DATA-001"));

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
