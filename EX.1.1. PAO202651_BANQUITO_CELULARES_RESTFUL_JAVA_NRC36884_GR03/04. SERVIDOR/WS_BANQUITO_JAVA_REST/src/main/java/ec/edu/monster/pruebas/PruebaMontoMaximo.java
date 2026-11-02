package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Prueba del servicio 2 (monto máximo de crédito) con los valores esperados
 * de la consulta 3 de 03_consultas.sql. Los créditos no cambian los
 * promedios, así que puede repetirse.
 *
 * @author carlo
 */
public class PruebaMontoMaximo {

    private static final String URL = "http://localhost:8080/WS_BANQUITO_JAVA_REST/api/v1/";
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaMontoMaximo ===");
        String clave = clave();

        String cliente1 = monto(clave, 1);
        comprobar("cliente 1: promedios 1500.00 y 250.00", cliente1.contains("\"promedioDepositos\":1500.00")
                && cliente1.contains("\"promedioRetiros\":250.00"));
        comprobar("cliente 1: ((1500 - 250) * 30%) * 6 = 2250.00", cliente1.contains("\"montoMaximo\":2250.00")
                && cliente1.contains("\"porcentaje\":30") && cliente1.contains("\"plazoMeses\":6"));
        comprobar("cliente 2: 450.00", monto(clave, 2).contains("\"montoMaximo\":450.00"));
        comprobar("cliente 6 (suma sus dos cuentas): 945.00", monto(clave, 6).contains("\"montoMaximo\":945.00"));
        comprobar("cliente 3 también tiene monto aunque no sea sujeto: 990.00",
                monto(clave, 3).contains("\"montoMaximo\":990.00"));
        comprobar("cliente inexistente da 404 y CLI-001", monto(clave, 999).contains("CLI-001") && estadoHttp == 404);
        comprobar("código 0 da 400 y DATA-001", monto(clave, 0).contains("DATA-001") && estadoHttp == 400);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String monto(String clave, int codCliente) throws Exception {
        return enviar("GET", "clientes/" + codCliente + "/monto-maximo", clave);
    }

    private static String clave() throws Exception {
        Properties propiedades = new Properties();
        try (InputStream entrada = PruebaMontoMaximo.class.getResourceAsStream("/db.properties")) {
            propiedades.load(entrada);
        }
        return propiedades.getProperty("api.clave").trim();
    }

    private static String enviar(String metodo, String ruta, String clave) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL + ruta).toURL().openConnection();
        conexion.setRequestMethod(metodo);
        conexion.setRequestProperty("X-Api-Key", clave);
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
