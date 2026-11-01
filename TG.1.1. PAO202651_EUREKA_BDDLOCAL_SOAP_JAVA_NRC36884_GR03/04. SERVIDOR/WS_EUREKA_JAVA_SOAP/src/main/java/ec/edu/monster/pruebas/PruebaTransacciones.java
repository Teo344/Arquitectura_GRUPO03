package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de depósitos, retiros y transferencias (RQF-005 a RQF-007) y de las
 * reglas RN-01 a RN-05. Los saldos esperados se calculan a partir del saldo
 * consultado antes de cada operación, así que la prueba puede repetirse.
 *
 * @author carlo
 */
public class PruebaTransacciones {

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_SOAP/";
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaTransacciones ===");
        String login = enviar("AutenticacionWS", null,
                "<ws:iniciarSesion><usuario>MONSTER</usuario><clave>MONSTER9</clave></ws:iniciarSesion>");
        String token = login.substring(login.indexOf("<token>") + 7, login.indexOf("</token>"));

        BigDecimal antes = saldo(token, "00200002");
        String deposito = operar(token, "depositar", "<cuenta>00200002</cuenta><importe>100.00</importe>");
        comprobar("depositar 100 en 00200002 suma al saldo",
                deposito.contains("<nuevoSaldo>" + antes.add(new BigDecimal("100.00")) + "</nuevoSaldo>"));
        comprobar("el saldo consultado coincide con el del depósito",
                saldo(token, "00200002").equals(antes.add(new BigDecimal("100.00"))));

        antes = saldo(token, "00200002");
        String retiro = operar(token, "retirar", "<cuenta>00200002</cuenta><importe>50.25</importe>");
        comprobar("retirar 50.25 de 00200002 resta del saldo",
                retiro.contains("<nuevoSaldo>" + antes.subtract(new BigDecimal("50.25")) + "</nuevoSaldo>"));
        comprobar("retirar más que el saldo da MOV-002",
                operar(token, "retirar", "<cuenta>00200002</cuenta><importe>99999999.00</importe>").contains("MOV-002"));
        comprobar("el retiro rechazado no cambia el saldo",
                saldo(token, "00200002").equals(antes.subtract(new BigDecimal("50.25"))));

        BigDecimal origen = saldo(token, "00200001");
        BigDecimal destino = saldo(token, "00200002");
        String transferencia = operar(token, "transferir",
                "<cuentaOrigen>00200001</cuentaOrigen><cuentaDestino>00200002</cuentaDestino><importe>500.00</importe>");
        comprobar("transferir 500 de 00200001 a 00200002 da OK-000", transferencia.contains("<codigo>OK-000</codigo>"));
        comprobar("la transferencia devuelve el nuevo saldo del origen",
                transferencia.contains("<nuevoSaldo>" + origen.subtract(new BigDecimal("500.00")) + "</nuevoSaldo>"));
        comprobar("el destino recibe el importe",
                saldo(token, "00200002").equals(destino.add(new BigDecimal("500.00"))));
        comprobar("el movimiento del destino referencia al origen", enviar("CuentaWS", token,
                "<ws:consultarMovimientos><cuenta>00200002</cuenta></ws:consultarMovimientos>")
                .contains("<cuentaReferencia>00200001</cuentaReferencia>"));

        comprobar("transferir entre monedas distintas da MOV-004", operar(token, "transferir",
                "<cuentaOrigen>00200001</cuentaOrigen><cuentaDestino>00200003</cuentaDestino><importe>1.00</importe>")
                .contains("MOV-004"));
        comprobar("transferir a la misma cuenta da MOV-003", operar(token, "transferir",
                "<cuentaOrigen>00200001</cuentaOrigen><cuentaDestino>00200001</cuentaDestino><importe>1.00</importe>")
                .contains("MOV-003"));
        comprobar("depositar en una cuenta CANCELADA da CTA-002",
                operar(token, "depositar", "<cuenta>00300001</cuenta><importe>10.00</importe>").contains("CTA-002"));
        comprobar("depositar en una cuenta inexistente da CTA-001",
                operar(token, "depositar", "<cuenta>99999999</cuenta><importe>10.00</importe>").contains("CTA-001"));
        comprobar("importe con tres decimales da MOV-001",
                operar(token, "depositar", "<cuenta>00200002</cuenta><importe>1.234</importe>").contains("MOV-001"));
        comprobar("importe cero da MOV-001",
                operar(token, "depositar", "<cuenta>00200002</cuenta><importe>0</importe>").contains("MOV-001"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String operar(String token, String operacion, String parametros) throws Exception {
        return enviar("TransaccionWS", token, "<ws:" + operacion + ">" + parametros + "</ws:" + operacion + ">");
    }

    private static BigDecimal saldo(String token, String cuenta) throws Exception {
        String respuesta = enviar("CuentaWS", token,
                "<ws:consultarCuenta><cuenta>" + cuenta + "</cuenta></ws:consultarCuenta>");
        return new BigDecimal(respuesta.substring(respuesta.indexOf("<saldo>") + 7, respuesta.indexOf("</saldo>")));
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
