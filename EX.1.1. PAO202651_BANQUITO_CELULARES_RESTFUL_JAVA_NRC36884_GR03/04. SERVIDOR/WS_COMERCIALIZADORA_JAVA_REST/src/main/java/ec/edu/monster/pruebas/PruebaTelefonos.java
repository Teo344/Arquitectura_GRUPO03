package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba del mantenimiento del catálogo: crear (201 + Location), consultar,
 * editar (PUT), eliminar (204, baja lógica) y las validaciones. Crea su
 * propio teléfono y lo da de baja, así que puede repetirse.
 *
 * @author carlo
 */
public class PruebaTelefonos {

    private static final String URL = "http://localhost:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1/";
    private static int fallidas = 0;
    private static int estadoHttp;
    private static String ubicacion;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaTelefonos ===");
        String token = login();

        String creado = enviar("POST", "telefonos", token,
                "{\"marca\":\"Prueba\",\"modelo\":\"Modelo X\",\"precio\":100.00}");
        comprobar("crear un teléfono da 201 con su Location", estadoHttp == 201 && ubicacion != null
                && ubicacion.matches(".*/api/v1/telefonos/\\d+"));
        String codigo = ubicacion == null ? "0" : ubicacion.substring(ubicacion.lastIndexOf('/') + 1);
        comprobar("el teléfono nuevo está ACTIVO y su precio en efectivo es 58.00",
                creado.contains("\"estado\":\"ACTIVO\"") && creado.contains("\"precioEfectivo\":58.00"));
        comprobar("GET de la Location devuelve el teléfono", enviar("GET", "telefonos/" + codigo, token, null)
                .equals(creado));
        comprobar("el catálogo lo incluye", enviar("GET", "telefonos", token, null)
                .contains("\"codTelefono\":" + codigo + ","));

        String editado = enviar("PUT", "telefonos/" + codigo, token,
                "{\"marca\":\"Prueba\",\"modelo\":\"Modelo X2\",\"precio\":120.50}");
        comprobar("PUT da 200 con los datos nuevos", estadoHttp == 200 && editado.contains("\"modelo\":\"Modelo X2\"")
                && editado.contains("\"precio\":120.50"));

        enviar("DELETE", "telefonos/" + codigo, token, null);
        comprobar("DELETE da 204", estadoHttp == 204);
        comprobar("el catálogo ya no lo incluye", !enviar("GET", "telefonos", token, null)
                .contains("\"codTelefono\":" + codigo + ","));
        comprobar("se puede consultar y queda INACTIVO (baja lógica)", enviar("GET", "telefonos/" + codigo, token, null)
                .contains("\"estado\":\"INACTIVO\"") && estadoHttp == 200);
        enviar("DELETE", "telefonos/" + codigo, token, null);
        comprobar("DELETE de nuevo también da 204 (idempotente)", estadoHttp == 204);

        comprobar("consultar un teléfono inexistente da 404 y TEL-001",
                enviar("GET", "telefonos/99999", token, null).contains("TEL-001") && estadoHttp == 404);
        comprobar("eliminar un teléfono inexistente da 404",
                enviar("DELETE", "telefonos/99999", token, null).contains("TEL-001") && estadoHttp == 404);
        comprobar("editar un teléfono inexistente da 404", enviar("PUT", "telefonos/99999", token,
                "{\"marca\":\"A\",\"modelo\":\"B\",\"precio\":1.00}").contains("TEL-001") && estadoHttp == 404);
        comprobar("precio 0 da 400 y TEL-003", enviar("POST", "telefonos", token,
                "{\"marca\":\"A\",\"modelo\":\"B\",\"precio\":0}").contains("TEL-003") && estadoHttp == 400);
        comprobar("marca vacía da 400 y DATA-001", enviar("POST", "telefonos", token,
                "{\"marca\":\" \",\"modelo\":\"B\",\"precio\":10.00}").contains("DATA-001") && estadoHttp == 400);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String login() throws Exception {
        String login = enviar("POST", "sesiones", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        return login.substring(inicio, login.indexOf("\"", inicio));
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
        ubicacion = conexion.getHeaderField("Location");
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
