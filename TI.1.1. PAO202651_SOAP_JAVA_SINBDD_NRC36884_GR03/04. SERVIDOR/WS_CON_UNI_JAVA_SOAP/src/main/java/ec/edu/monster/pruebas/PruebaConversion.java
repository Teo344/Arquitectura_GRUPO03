package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de las conversiones de longitud, masa y temperatura en el servidor
 * SOAP.
 *
 * @author carlo
 */
public class PruebaConversion {

    private static final String URL = "http://localhost:8080/WS_CON_UNI_JAVA_SOAP/";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaConversion ===");
        String login = enviar("AutenticacionWS", null,
                "<ws:login><usuario>MONSTER</usuario><clave>MONSTER9</clave></ws:login>");
        String token = login.substring(login.indexOf("<token>") + 7, login.indexOf("</token>"));

        comprobar("1 KM -> M = 1000", convertir(token, "Longitud", "1", "KM", "M").contains("<resultado>1000.0</resultado>"));
        comprobar("1 QQ -> LB = 100", convertir(token, "Masa", "1", "QQ", "LB").contains("<resultado>100.0</resultado>"));
        comprobar("0 C -> K = 273.15", convertir(token, "Temperatura", "0", "C", "K").contains("<resultado>273.15</resultado>"));
        comprobar("KG en longitud da UNIT-001", convertir(token, "Longitud", "1", "KG", "M").contains("UNIT-001"));
        comprobar("valor negativo en masa da DATA-001", convertir(token, "Masa", "-5", "KG", "G").contains("DATA-001"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String convertir(String token, String categoria, String valor, String origen, String destino)
            throws Exception {
        return enviar("ConversionUnidadesWS", token, "<ws:convertir" + categoria + "><solicitud>"
                + "<valor>" + valor + "</valor><unidadOrigen>" + origen + "</unidadOrigen>"
                + "<unidadDestino>" + destino + "</unidadDestino></solicitud></ws:convertir" + categoria + ">");
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
