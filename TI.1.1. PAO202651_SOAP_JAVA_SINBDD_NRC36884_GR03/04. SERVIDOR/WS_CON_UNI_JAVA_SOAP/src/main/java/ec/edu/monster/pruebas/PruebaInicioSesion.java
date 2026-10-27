package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba del inicio de sesión contra el servidor SOAP desplegado en Payara.
 *
 * @author carlo
 */
public class PruebaInicioSesion {

    private static final String URL = "http://localhost:8080/WS_CON_UNI_JAVA_SOAP/AutenticacionWS";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaInicioSesion ===");

        comprobar("credenciales correctas devuelven token", login("MONSTER", "MONSTER9").contains("<token>"));
        comprobar("clave incorrecta da AUTH-003", login("MONSTER", "clave-mala").contains("AUTH-003"));
        comprobar("usuario incorrecto da AUTH-003", login("OTRO", "MONSTER9").contains("AUTH-003"));
        comprobar("credenciales vacías dan AUTH-003", login("", "").contains("AUTH-003"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String login(String usuario, String clave) throws Exception {
        return enviar("<ws:login><usuario>" + usuario + "</usuario><clave>" + clave + "</clave></ws:login>");
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
