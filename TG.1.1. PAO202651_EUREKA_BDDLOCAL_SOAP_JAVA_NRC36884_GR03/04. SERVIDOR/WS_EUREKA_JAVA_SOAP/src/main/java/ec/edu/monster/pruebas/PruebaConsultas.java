package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de la consulta de cuenta (RQF-003) y de movimientos (RQF-004).
 * Requiere la BD recién cargada con los datos semilla.
 *
 * @author carlo
 */
public class PruebaConsultas {

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_SOAP/";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaConsultas ===");
        String login = enviar("AutenticacionWS", null,
                "<ws:iniciarSesion><usuario>MONSTER</usuario><clave>MONSTER9</clave></ws:iniciarSesion>");
        String token = login.substring(login.indexOf("<token>") + 7, login.indexOf("</token>"));

        String cuenta = cuenta(token, "00100001");
        comprobar("00100001 tiene saldo 6900.00", cuenta.contains("<saldo>6900.00</saldo>"));
        comprobar("00100001 es de ALAN ALBERTO ARANDA LUNA", cuenta.contains("<titular>ALAN ALBERTO ARANDA LUNA</titular>"));
        comprobar("00100001 está ACTIVO", cuenta.contains("<estado>ACTIVO</estado>"));

        String cancelada = cuenta(token, "00300001");
        comprobar("00300001 se consulta con OK-000", cancelada.contains("<codigo>OK-000</codigo>"));
        comprobar("00300001 muestra el estado CANCELADO", cancelada.contains("<estado>CANCELADO</estado>"));

        comprobar("cuenta '1234' da DATA-001", cuenta(token, "1234").contains("DATA-001"));
        comprobar("cuenta 99999999 da CTA-001", cuenta(token, "99999999").contains("CTA-001"));

        String movimientos = enviar("CuentaWS", token,
                "<ws:consultarMovimientos><cuenta>00100001</cuenta></ws:consultarMovimientos>");
        comprobar("00100001 tiene 7 movimientos", contar(movimientos, "<movimiento>") == 7);
        comprobar("el primer movimiento es la apertura", movimientos.indexOf("<numero>1</numero>")
                < movimientos.indexOf("<numero>2</numero>") && movimientos.contains("Apertura de Cuenta"));
        comprobar("movimientos de 99999999 da CTA-001", enviar("CuentaWS", token,
                "<ws:consultarMovimientos><cuenta>99999999</cuenta></ws:consultarMovimientos>").contains("CTA-001"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String cuenta(String token, String cuenta) throws Exception {
        return enviar("CuentaWS", token, "<ws:consultarCuenta><cuenta>" + cuenta + "</cuenta></ws:consultarCuenta>");
    }

    private static int contar(String texto, String buscado) {
        int total = 0;
        for (int i = texto.indexOf(buscado); i >= 0; i = texto.indexOf(buscado, i + 1)) {
            total++;
        }
        return total;
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
