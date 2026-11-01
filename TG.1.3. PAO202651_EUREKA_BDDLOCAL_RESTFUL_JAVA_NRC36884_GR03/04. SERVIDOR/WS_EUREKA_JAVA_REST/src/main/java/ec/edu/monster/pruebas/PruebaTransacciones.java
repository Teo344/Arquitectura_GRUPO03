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

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_REST/api/";
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaTransacciones ===");
        String login = enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        String token = login.substring(inicio, login.indexOf("\"", inicio));

        BigDecimal antes = saldo(token, "00200002");
        String deposito = operar(token, "depositos", "{\"cuenta\":\"00200002\",\"importe\":100.00}");
        comprobar("depositar 100 en 00200002 suma al saldo",
                deposito.contains("\"nuevoSaldo\":" + antes.add(new BigDecimal("100.00"))));
        comprobar("el saldo consultado coincide con el del depósito",
                saldo(token, "00200002").equals(antes.add(new BigDecimal("100.00"))));

        antes = saldo(token, "00200002");
        String retiro = operar(token, "retiros", "{\"cuenta\":\"00200002\",\"importe\":50.25}");
        comprobar("retirar 50.25 de 00200002 resta del saldo",
                retiro.contains("\"nuevoSaldo\":" + antes.subtract(new BigDecimal("50.25"))));
        comprobar("retirar más que el saldo da 400 y MOV-002", operar(token, "retiros",
                "{\"cuenta\":\"00200002\",\"importe\":99999999.00}").contains("MOV-002") && estadoHttp == 400);
        comprobar("el retiro rechazado no cambia el saldo",
                saldo(token, "00200002").equals(antes.subtract(new BigDecimal("50.25"))));

        BigDecimal origen = saldo(token, "00200001");
        BigDecimal destino = saldo(token, "00200002");
        String transferencia = operar(token, "transferencias",
                "{\"cuentaOrigen\":\"00200001\",\"cuentaDestino\":\"00200002\",\"importe\":500.00}");
        comprobar("transferir 500 de 00200001 a 00200002 da OK-000", transferencia.contains("\"codigo\":\"OK-000\""));
        comprobar("la transferencia devuelve el nuevo saldo del origen",
                transferencia.contains("\"nuevoSaldo\":" + origen.subtract(new BigDecimal("500.00"))));
        comprobar("el destino recibe el importe",
                saldo(token, "00200002").equals(destino.add(new BigDecimal("500.00"))));
        comprobar("el movimiento del destino referencia al origen",
                enviar("GET", "cuentas/00200002/movimientos", token, null).contains("\"cuentaReferencia\":\"00200001\""));

        comprobar("transferir entre monedas distintas da MOV-004", operar(token, "transferencias",
                "{\"cuentaOrigen\":\"00200001\",\"cuentaDestino\":\"00200003\",\"importe\":1.00}").contains("MOV-004"));
        comprobar("transferir a la misma cuenta da MOV-003", operar(token, "transferencias",
                "{\"cuentaOrigen\":\"00200001\",\"cuentaDestino\":\"00200001\",\"importe\":1.00}").contains("MOV-003"));
        comprobar("depositar en una cuenta CANCELADA da CTA-002",
                operar(token, "depositos", "{\"cuenta\":\"00300001\",\"importe\":10.00}").contains("CTA-002"));
        comprobar("depositar en una cuenta inexistente da CTA-001",
                operar(token, "depositos", "{\"cuenta\":\"99999999\",\"importe\":10.00}").contains("CTA-001"));
        comprobar("importe con tres decimales da MOV-001",
                operar(token, "depositos", "{\"cuenta\":\"00200002\",\"importe\":1.234}").contains("MOV-001"));
        comprobar("importe cero da MOV-001",
                operar(token, "depositos", "{\"cuenta\":\"00200002\",\"importe\":0}").contains("MOV-001"));
        comprobar("importe no numérico da 400 y DATA-001", operar(token, "depositos",
                "{\"cuenta\":\"00200002\",\"importe\":\"abc\"}").contains("DATA-001") && estadoHttp == 400);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String operar(String token, String operacion, String json) throws Exception {
        return enviar("POST", "transacciones/" + operacion, token, json);
    }

    private static BigDecimal saldo(String token, String cuenta) throws Exception {
        String respuesta = enviar("GET", "cuentas/" + cuenta, token, null);
        int inicio = respuesta.indexOf("\"saldo\":") + 8;
        return new BigDecimal(respuesta.substring(inicio, respuesta.indexOf(",", inicio)));
    }

    private static String enviar(String metodo, String ruta, String token, String json) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL + ruta).toURL().openConnection();
        conexion.setRequestMethod(metodo);
        if (token != null) {
            conexion.setRequestProperty("Authorization", "Bearer " + token);
        }
        if (json != null) {
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        }
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
