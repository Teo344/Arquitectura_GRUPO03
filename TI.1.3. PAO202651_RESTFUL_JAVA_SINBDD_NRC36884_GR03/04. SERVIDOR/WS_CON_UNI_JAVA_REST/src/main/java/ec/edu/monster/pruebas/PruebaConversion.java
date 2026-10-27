package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de las conversiones de longitud, masa y temperatura en el servidor
 * REST.
 *
 * @author carlo
 */
public class PruebaConversion {

    private static final String URL = "http://localhost:8080/WS_CON_UNI_JAVA_REST/api/";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaConversion ===");
        String login = enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        String token = login.substring(inicio, login.indexOf("\"", inicio));

        comprobar("1 KM -> M = 1000", convertir(token, "longitud", "1", "KM", "M").contains("\"resultado\":1000.0"));
        comprobar("1 QQ -> LB = 100", convertir(token, "masa", "1", "QQ", "LB").contains("\"resultado\":100.0"));
        comprobar("0 C -> K = 273.15", convertir(token, "temperatura", "0", "C", "K").contains("\"resultado\":273.15"));
        comprobar("KG en longitud da UNIT-001", convertir(token, "longitud", "1", "KG", "M").contains("UNIT-001"));
        comprobar("valor negativo en masa da DATA-001", convertir(token, "masa", "-5", "KG", "G").contains("DATA-001"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String convertir(String token, String categoria, String valor, String origen, String destino)
            throws Exception {
        return enviar("POST", "conversiones/" + categoria, token, "{\"valor\":" + valor
                + ",\"unidadOrigen\":\"" + origen + "\",\"unidadDestino\":\"" + destino + "\"}");
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
