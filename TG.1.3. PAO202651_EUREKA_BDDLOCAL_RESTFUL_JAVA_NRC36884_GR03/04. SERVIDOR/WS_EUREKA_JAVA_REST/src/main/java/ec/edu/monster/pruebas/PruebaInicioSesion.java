package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba del inicio de sesión (RQF-001) contra el servidor desplegado.
 *
 * @author carlo
 */
public class PruebaInicioSesion {

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_REST/api/";
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaInicioSesion ===");
        String correcto = login("MONSTER", "MONSTER9");
        comprobar("MONSTER/MONSTER9 da 200 y OK-000", estadoHttp == 200 && correcto.contains("\"codigo\":\"OK-000\""));
        comprobar("la respuesta trae el token", correcto.contains("\"token\":\""));
        comprobar("la respuesta trae el empleado 0015", correcto.contains("\"codigoEmpleado\":\"0015\""));
        comprobar("la respuesta trae la sucursal Sipan", correcto.contains("\"sucursal\":\"Sipan\""));
        comprobar("clave incorrecta da 401 y AUTH-001", login("MONSTER", "otra").contains("AUTH-001") && estadoHttp == 401);
        comprobar("usuario inexistente da AUTH-001", login("nadie", "MONSTER9").contains("AUTH-001"));
        comprobar("usuario ANULADO (creyes) da 401 y AUTH-002",
                login("creyes", "linda").contains("AUTH-002") && estadoHttp == 401);
        comprobar("usuario sin sucursal (internet) da AUTH-002", login("internet", "internet").contains("AUTH-002"));
        comprobar("usuario vacío da 400 y DATA-001", login("", "MONSTER9").contains("DATA-001") && estadoHttp == 400);
        comprobar("JSON mal formado da 400 y DATA-001",
                enviar("POST", "autenticacion/login", null, "{malo").contains("DATA-001") && estadoHttp == 400);
        comprobar("los errores llevan estado ERROR", login("MONSTER", "otra").contains("\"estado\":\"ERROR\""));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String login(String usuario, String clave) throws Exception {
        return enviar("POST", "autenticacion/login", null, "{\"usuario\":\"" + usuario + "\",\"clave\":\"" + clave
                + "\",\"equipo\":\"PruebaInicioSesion\"}");
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
