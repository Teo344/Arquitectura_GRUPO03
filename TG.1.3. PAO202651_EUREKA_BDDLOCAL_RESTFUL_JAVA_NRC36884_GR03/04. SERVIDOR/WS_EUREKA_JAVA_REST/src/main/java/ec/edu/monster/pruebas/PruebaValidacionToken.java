package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de que los recursos protegidos exigen un token válido en la cabecera
 * Authorization y de que cerrar-sesion lo invalida (RQF-002, RN-06).
 *
 * @author carlo
 */
public class PruebaValidacionToken {

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_REST/api/";
    private static final String CONSULTA = "cuentas/00100001";
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaValidacionToken ===");
        String login = enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        String token = login.substring(inicio, login.indexOf("\"", inicio));

        comprobar("sin token da 401 y AUTH-003", enviar("GET", CONSULTA, null, null).contains("AUTH-003") && estadoHttp == 401);
        comprobar("token inventado da AUTH-003", enviar("GET", CONSULTA, "token-inventado", null).contains("AUTH-003"));
        comprobar("sin token no se puede depositar", enviar("POST", "transacciones/depositos", null,
                "{\"cuenta\":\"00100001\",\"importe\":1.00}").contains("AUTH-003"));
        comprobar("token válido permite consultar",
                enviar("GET", CONSULTA, token, null).contains("\"codigo\":\"OK-000\"") && estadoHttp == 200);
        comprobar("cerrar-sesion da OK-000",
                enviar("POST", "autenticacion/cerrar-sesion", token, null).contains("\"codigo\":\"OK-000\""));
        comprobar("después del cierre el token da AUTH-003", enviar("GET", CONSULTA, token, null).contains("AUTH-003"));
        comprobar("cerrar de nuevo la sesión da 401 y AUTH-003", enviar("POST", "autenticacion/cerrar-sesion", token, null)
                .contains("AUTH-003") && estadoHttp == 401);

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
        return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
