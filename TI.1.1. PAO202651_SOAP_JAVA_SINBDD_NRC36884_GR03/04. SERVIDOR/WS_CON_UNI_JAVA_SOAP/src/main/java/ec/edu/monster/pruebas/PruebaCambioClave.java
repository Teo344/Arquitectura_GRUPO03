package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba del cambio de clave en el servidor SOAP. Al final vuelve a dejar la
 * clave MONSTER9.
 *
 * @author carlo
 */
public class PruebaCambioClave {

    private static final String URL = "http://localhost:8080/WS_CON_UNI_JAVA_SOAP/AutenticacionWS";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaCambioClave ===");

        comprobar("clave actual incorrecta da AUTH-003", cambiarClave("clave-mala", "NUEVA123").contains("AUTH-003"));
        comprobar("clave nueva vacía da DATA-001", cambiarClave("MONSTER9", "").contains("DATA-001"));
        comprobar("cambio de clave correcto", cambiarClave("MONSTER9", "NUEVA123").contains("<estado>OK</estado>"));
        comprobar("la clave anterior ya no sirve", login("MONSTER9").contains("AUTH-003"));
        comprobar("la clave nueva inicia sesión", login("NUEVA123").contains("<token>"));
        comprobar("se restaura la clave MONSTER9", cambiarClave("NUEVA123", "MONSTER9").contains("<estado>OK</estado>"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String cambiarClave(String claveActual, String claveNueva) throws Exception {
        return enviar("<ws:cambiarClave><usuario>MONSTER</usuario><claveActual>" + claveActual + "</claveActual>"
                + "<claveNueva>" + claveNueva + "</claveNueva></ws:cambiarClave>");
    }

    private static String login(String clave) throws Exception {
        return enviar("<ws:login><usuario>MONSTER</usuario><clave>" + clave + "</clave></ws:login>");
    }

    private static String enviar(String operacion) throws Exception {
        String sobre = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:ws=\"http://wservicios.controladores.monster.edu.ec/\">"
                + "<soapenv:Body>" + operacion + "</soapenv:Body></soapenv:Envelope>";
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL).toURL().openConnection();
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
