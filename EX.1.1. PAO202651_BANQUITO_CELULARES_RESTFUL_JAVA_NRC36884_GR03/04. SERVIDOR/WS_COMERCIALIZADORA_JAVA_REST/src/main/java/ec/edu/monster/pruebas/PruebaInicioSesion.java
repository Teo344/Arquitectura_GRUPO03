package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba del inicio de sesión (POST /sesiones) contra el servidor desplegado.
 *
 * @author carlo
 */
public class PruebaInicioSesion {

    private static final String URL = "http://localhost:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1/";
    private static int fallidas = 0;
    private static int estadoHttp;
    private static String ubicacion;
    private static String tipo;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaInicioSesion ===");
        String correcto = login("MONSTER", "MONSTER9");
        comprobar("MONSTER/MONSTER9 da 201 con el token", estadoHttp == 201 && correcto.contains("\"token\":\""));
        comprobar("la cabecera Location apunta a /sesiones/actual",
                ubicacion != null && ubicacion.endsWith("/api/v1/sesiones/actual"));
        comprobar("la respuesta trae el usuario y su vencimiento", correcto.contains("\"usuario\":\"MONSTER\"")
                && correcto.contains("\"expiraEn\":\""));
        String incorrecto = login("MONSTER", "otra");
        comprobar("clave incorrecta da 401 y AUTH-001", incorrecto.contains("AUTH-001") && estadoHttp == 401);
        comprobar("el error es application/problem+json", tipo != null && tipo.startsWith("application/problem+json")
                && incorrecto.contains("\"status\":401"));
        comprobar("usuario inexistente da AUTH-001", login("nadie", "MONSTER9").contains("AUTH-001"));
        comprobar("usuario ANULADO (JPEREZ) da 401 y AUTH-002",
                login("JPEREZ", "JPEREZ1").contains("AUTH-002") && estadoHttp == 401);
        comprobar("usuario vacío da 400 y DATA-001", login("", "MONSTER9").contains("DATA-001") && estadoHttp == 400);
        comprobar("JSON mal formado da 400 y DATA-001",
                enviar("POST", "sesiones", "{malo").contains("DATA-001") && estadoHttp == 400);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String login(String usuario, String clave) throws Exception {
        return enviar("POST", "sesiones", "{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave + "\"}");
    }

    private static String enviar(String metodo, String ruta, String json) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL + ruta).toURL().openConnection();
        conexion.setRequestMethod(metodo);
        if (json != null) {
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        }
        estadoHttp = conexion.getResponseCode();
        ubicacion = conexion.getHeaderField("Location");
        tipo = conexion.getContentType();
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
