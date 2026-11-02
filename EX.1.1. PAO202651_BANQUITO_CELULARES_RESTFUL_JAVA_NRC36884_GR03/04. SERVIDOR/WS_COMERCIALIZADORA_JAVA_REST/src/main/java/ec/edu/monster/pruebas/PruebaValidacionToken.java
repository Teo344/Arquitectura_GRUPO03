package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de que los recursos protegidos exigen un token válido en la cabecera
 * Authorization y de que DELETE /sesiones/actual lo invalida.
 *
 * @author carlo
 */
public class PruebaValidacionToken {

    private static final String URL = "http://localhost:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1/";
    private static final String CONSULTA = "telefonos";
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaValidacionToken ===");
        String login = enviar("POST", "sesiones", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        String token = login.substring(inicio, login.indexOf("\"", inicio));

        comprobar("sin token da 401 y AUTH-003", enviar("GET", CONSULTA, null, null).contains("AUTH-003") && estadoHttp == 401);
        comprobar("token inventado da AUTH-003", enviar("GET", CONSULTA, "token-inventado", null).contains("AUTH-003"));
        comprobar("sin token no se puede facturar", enviar("POST", "facturas", null,
                "{\"cedula\":\"1712345678\",\"formaPago\":\"EFECTIVO\",\"detalles\":[{\"codTelefono\":1,\"cantidad\":1}]}")
                .contains("AUTH-003") && estadoHttp == 401);
        comprobar("token válido permite consultar el catálogo",
                enviar("GET", CONSULTA, token, null).startsWith("[") && estadoHttp == 200);
        enviar("DELETE", "sesiones/actual", token, null);
        comprobar("cerrar la sesión da 204", estadoHttp == 204);
        comprobar("después del cierre el token da AUTH-003", enviar("GET", CONSULTA, token, null).contains("AUTH-003"));
        comprobar("cerrar de nuevo la sesión da 401 y AUTH-003",
                enviar("DELETE", "sesiones/actual", token, null).contains("AUTH-003") && estadoHttp == 401);

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
        estadoHttp = conexion.getResponseCode();
        InputStream entrada = estadoHttp < 400 ? conexion.getInputStream() : conexion.getErrorStream();
        return entrada == null ? "" : new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
