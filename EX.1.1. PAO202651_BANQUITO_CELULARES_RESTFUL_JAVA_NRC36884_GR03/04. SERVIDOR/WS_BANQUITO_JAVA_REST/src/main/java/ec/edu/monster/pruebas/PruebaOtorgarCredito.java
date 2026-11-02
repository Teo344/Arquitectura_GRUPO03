package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Prueba del servicio 3 (otorgar crédito). Otorga un crédito al cliente 6
 * (monto máximo 945.00): requiere la BD recién cargada (01 + 02) y no se
 * puede repetir sin recargarla, porque un cliente con crédito activo ya no es
 * sujeto de crédito.
 *
 * @author carlo
 */
public class PruebaOtorgarCredito {

    private static final String URL = "http://localhost:8080/WS_BANQUITO_JAVA_REST/api/v1/";
    private static int fallidas = 0;
    private static int estadoHttp;
    private static String ubicacion;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaOtorgarCredito ===");
        String clave = clave();
        comprobar("el cliente 6 aún es sujeto de crédito (BD recién cargada)",
                enviar("GET", "clientes/sujeto-credito?cedula=1767890123", clave, null).contains("\"sujetoCredito\":true"));

        String credito = otorgar(clave, 6, "500.00", 12);
        comprobar("500 a 12 cuotas para el cliente 6 da 201", estadoHttp == 201);
        comprobar("la cabecera Location apunta al crédito nuevo", ubicacion != null
                && ubicacion.matches(".*/api/v1/creditos/\\d+") && credito.contains("\"codCredito\":"
                + ubicacion.substring(ubicacion.lastIndexOf('/') + 1) + ","));
        comprobar("el crédito trae su tabla de 12 cuotas y queda ACTIVO", credito.contains("\"numero\":12,")
                && credito.contains("\"estado\":\"ACTIVO\"") && credito.contains("\"saldo\":0.00}"));
        comprobar("GET de la Location devuelve el mismo crédito", ubicacion != null
                && enviar("GET", ubicacion.substring(ubicacion.indexOf("creditos/")), clave, null).equals(credito));
        String repetido = otorgar(clave, 6, "100.00", 3);
        comprobar("un segundo crédito para el cliente 6 da 409 y CRE-001 (crédito activo)", estadoHttp == 409
                && repetido.contains("CRE-001") && repetido.contains("crédito activo"));

        comprobar("cliente 1 con 3000.00 (máximo 2250.00) da 409 y CRE-002",
                otorgar(clave, 1, "3000.00", 6).contains("CRE-002") && estadoHttp == 409);
        comprobar("cliente 3 (hombre de 22) da 409 y CRE-001", otorgar(clave, 3, "100.00", 6).contains("CRE-001")
                && estadoHttp == 409);
        comprobar("2 cuotas da 400 y CRE-003", otorgar(clave, 1, "100.00", 2).contains("CRE-003") && estadoHttp == 400);
        comprobar("19 cuotas da 400 y CRE-003", otorgar(clave, 1, "100.00", 19).contains("CRE-003") && estadoHttp == 400);
        comprobar("monto con tres decimales da 400 y CRE-005",
                otorgar(clave, 1, "100.123", 6).contains("CRE-005") && estadoHttp == 400);
        comprobar("cliente inexistente da 404 y CLI-001", otorgar(clave, 999, "100.00", 6).contains("CLI-001")
                && estadoHttp == 404);
        comprobar("monto no numérico da 400 y DATA-001", enviar("POST", "creditos", clave,
                "{\"codCliente\":1,\"monto\":\"abc\",\"numCuotas\":6}").contains("DATA-001") && estadoHttp == 400);
        comprobar("JSON mal formado da 400 y DATA-001", enviar("POST", "creditos", clave, "{malo")
                .contains("DATA-001") && estadoHttp == 400);
        comprobar("los rechazos no crearon créditos para el cliente 1",
                enviar("GET", "clientes/1/creditos", clave, null).equals("[]"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String otorgar(String clave, int codCliente, String monto, int numCuotas) throws Exception {
        return enviar("POST", "creditos", clave, "{\"codCliente\":" + codCliente + ",\"monto\":" + monto
                + ",\"numCuotas\":" + numCuotas + "}");
    }

    private static String clave() throws Exception {
        Properties propiedades = new Properties();
        try (InputStream entrada = PruebaOtorgarCredito.class.getResourceAsStream("/db.properties")) {
            propiedades.load(entrada);
        }
        return propiedades.getProperty("api.clave").trim();
    }

    private static String enviar(String metodo, String ruta, String clave, String json) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL + ruta).toURL().openConnection();
        conexion.setRequestMethod(metodo);
        conexion.setRequestProperty("X-Api-Key", clave);
        if (json != null) {
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        }
        estadoHttp = conexion.getResponseCode();
        ubicacion = conexion.getHeaderField("Location");
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
