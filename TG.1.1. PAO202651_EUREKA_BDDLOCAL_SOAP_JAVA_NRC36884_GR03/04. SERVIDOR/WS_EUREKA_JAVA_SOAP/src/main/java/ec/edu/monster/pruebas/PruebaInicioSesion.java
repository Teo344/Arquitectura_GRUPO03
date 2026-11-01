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

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_SOAP/";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaInicioSesion ===");
        String correcto = login("MONSTER", "MONSTER9");
        comprobar("MONSTER/MONSTER9 da OK-000", correcto.contains("<codigo>OK-000</codigo>"));
        comprobar("la respuesta trae el token", correcto.contains("<token>"));
        comprobar("la respuesta trae el empleado 0015", correcto.contains("<codigoEmpleado>0015</codigoEmpleado>"));
        comprobar("la respuesta trae la sucursal Sipan", correcto.contains("<sucursal>Sipan</sucursal>"));
        comprobar("clave incorrecta da AUTH-001", login("MONSTER", "otra").contains("AUTH-001"));
        comprobar("usuario inexistente da AUTH-001", login("nadie", "MONSTER9").contains("AUTH-001"));
        comprobar("usuario ANULADO (creyes) da AUTH-002", login("creyes", "linda").contains("AUTH-002"));
        comprobar("usuario sin sucursal (internet) da AUTH-002", login("internet", "internet").contains("AUTH-002"));
        comprobar("usuario vacío da DATA-001", login("", "MONSTER9").contains("DATA-001"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String login(String usuario, String clave) throws Exception {
        return enviar("AutenticacionWS", null, "<ws:iniciarSesion><usuario>" + usuario + "</usuario><clave>"
                + clave + "</clave><equipo>PruebaInicioSesion</equipo></ws:iniciarSesion>");
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
