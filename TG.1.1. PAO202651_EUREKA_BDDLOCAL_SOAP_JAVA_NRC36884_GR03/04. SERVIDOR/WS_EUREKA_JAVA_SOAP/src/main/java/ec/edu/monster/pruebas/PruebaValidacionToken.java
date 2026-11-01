package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de que las operaciones protegidas exigen un token válido en la
 * cabecera SOAP y de que cerrarSesion lo invalida (RQF-002, RN-06).
 *
 * @author carlo
 */
public class PruebaValidacionToken {

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_SOAP/";
    private static final String CONSULTA = "<ws:consultarCuenta><cuenta>00100001</cuenta></ws:consultarCuenta>";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaValidacionToken ===");
        String login = enviar("AutenticacionWS", null,
                "<ws:iniciarSesion><usuario>MONSTER</usuario><clave>MONSTER9</clave></ws:iniciarSesion>");
        String token = login.substring(login.indexOf("<token>") + 7, login.indexOf("</token>"));

        comprobar("sin token da AUTH-003", enviar("CuentaWS", null, CONSULTA).contains("AUTH-003"));
        comprobar("token inventado da AUTH-003", enviar("CuentaWS", "token-inventado", CONSULTA).contains("AUTH-003"));
        comprobar("sin token no se puede depositar", enviar("TransaccionWS", null,
                "<ws:depositar><cuenta>00100001</cuenta><importe>1.00</importe></ws:depositar>").contains("AUTH-003"));
        comprobar("token válido permite consultar", enviar("CuentaWS", token, CONSULTA).contains("<codigo>OK-000</codigo>"));
        comprobar("cerrarSesion da OK-000", enviar("AutenticacionWS", token, "<ws:cerrarSesion/>")
                .contains("<codigo>OK-000</codigo>"));
        comprobar("después del cierre el token da AUTH-003", enviar("CuentaWS", token, CONSULTA).contains("AUTH-003"));
        comprobar("cerrar de nuevo la sesión da AUTH-003", enviar("AutenticacionWS", token, "<ws:cerrarSesion/>")
                .contains("AUTH-003"));

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
