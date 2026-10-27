package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de que las operaciones protegidas exigen un token válido en la
 * cabecera SOAP.
 *
 * @author carlo
 */
public class PruebaValidacionToken {

    private static final String URL = "http://localhost:8080/WS_CON_UNI_JAVA_SOAP/";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaValidacionToken ===");
        String login = enviar("AutenticacionWS", null,
                "<ws:login><usuario>MONSTER</usuario><clave>MONSTER9</clave></ws:login>");
        String token = login.substring(login.indexOf("<token>") + 7, login.indexOf("</token>"));

        comprobar("sin token da AUTH-001",
                enviar("ConversionUnidadesWS", null, "<ws:consultarCatalogo/>").contains("AUTH-001"));
        comprobar("token inventado da AUTH-001",
                enviar("ConversionUnidadesWS", "token-inventado", "<ws:consultarCatalogo/>").contains("AUTH-001"));
        comprobar("token válido permite consultar",
                enviar("ConversionUnidadesWS", token, "<ws:consultarCatalogo/>").contains("<estado>OK</estado>"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String enviar(String servicio, String token, String operacion) throws Exception {
        String sobre = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:ws=\"http://wservicios.controladores.monster.edu.ec/\">"
                + "<soapenv:Header>" + (token == null ? "" : "<ws:token>" + token + "</ws:token>") + "</soapenv:Header>"
                + "<soapenv:Body>" + operacion + "</soapenv:Body></soapenv:Envelope>";
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL + servicio).toURL().openConnection();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        conexion.getOutputStream().write(sobre.getBytes(StandardCharsets.UTF_8));
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
