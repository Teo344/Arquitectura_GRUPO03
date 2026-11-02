package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Prueba de que todo el Core exige la cabecera X-Api-Key y de que los errores
 * salen como application/problem+json. No necesita la base de datos: usa la
 * simulación, que solo calcula.
 *
 * @author carlo
 */
public class PruebaClaveApi {

    private static final String URL = "http://localhost:8080/WS_BANQUITO_JAVA_REST/api/v1/";
    private static final String CONSULTA = "creditos/simulacion?monto=450&cuotas=6";
    private static int fallidas = 0;
    private static int estadoHttp;
    private static String tipo;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaClaveApi ===");
        String clave = clave();

        String sinClave = enviar("GET", CONSULTA, null, null);
        comprobar("sin clave da 401 y AUTH-004", estadoHttp == 401 && sinClave.contains("\"codigo\":\"AUTH-004\""));
        comprobar("el error es application/problem+json", tipo != null && tipo.startsWith("application/problem+json"));
        comprobar("el error trae status 401 y la ruta en instance",
                sinClave.contains("\"status\":401") && sinClave.contains("\"instance\":\"/creditos/simulacion\""));
        comprobar("clave incorrecta da 401", enviar("GET", CONSULTA, "otra-clave", null).contains("AUTH-004")
                && estadoHttp == 401);
        comprobar("sin clave tampoco se puede otorgar un crédito", enviar("POST", "creditos", null,
                "{\"codCliente\":1,\"monto\":100.00,\"numCuotas\":3}").contains("AUTH-004") && estadoHttp == 401);
        String correcta = enviar("GET", CONSULTA, clave, null);
        comprobar("con la clave correcta da 200 y la tabla", estadoHttp == 200 && correcta.startsWith("[")
                && correcta.contains("\"valorCuota\":78.65"));
        comprobar("una ruta inexistente da 404 en problem+json", enviar("GET", "no-existe", clave, null)
                .contains("\"status\":404") && estadoHttp == 404);
        comprobar("un método no permitido da 405", enviar("DELETE", "creditos/simulacion", clave, null)
                .contains("\"status\":405") && estadoHttp == 405);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    /**
     * La misma clave que usa el servidor (db.properties en el classpath).
     */
    private static String clave() throws Exception {
        Properties propiedades = new Properties();
        try (InputStream entrada = PruebaClaveApi.class.getResourceAsStream("/db.properties")) {
            propiedades.load(entrada);
        }
        return propiedades.getProperty("api.clave").trim();
    }

    private static String enviar(String metodo, String ruta, String clave, String json) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL + ruta).toURL().openConnection();
        conexion.setRequestMethod(metodo);
        if (clave != null) {
            conexion.setRequestProperty("X-Api-Key", clave);
        }
        if (json != null) {
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        }
        estadoHttp = conexion.getResponseCode();
        tipo = conexion.getContentType();
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
