package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Prueba de punta a punta de la venta: necesita los dos servidores
 * desplegados y las dos BD recién cargadas (01 + 02), porque vende a crédito
 * al cliente 1 (monto máximo 2250.00) y después ya no es sujeto de crédito.
 * Catálogo: 1 = 199.00, 2 = 279.00, 5 = 899.00, 8 = 2399.00.
 *
 * @author carlo
 */
public class PruebaFacturacion {

    private static final String URL = "http://localhost:8080/WS_COMERCIALIZADORA_JAVA_REST/api/v1/";
    private static final String CLIENTE_1 = "1712345678";
    private static int fallidas = 0;
    private static int estadoHttp;
    private static String ubicacion;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaFacturacion ===");
        String token = login();

        String evaluacion = enviar("GET", "clientes/sujeto-credito?cedula=" + CLIENTE_1, token, null);
        comprobar("el cliente 1 es sujeto de crédito con máximo 2250.00 (BD recién cargada)",
                evaluacion.contains("\"sujetoCredito\":true") && evaluacion.contains("\"montoMaximo\":2250.00"));
        String cliente4 = enviar("GET", "clientes/sujeto-credito?cedula=1745678901", token, null);
        comprobar("el cliente 4 no es sujeto (sin depósito en el último mes) pero tiene monto máximo",
                cliente4.contains("SIN_DEPOSITO_ULTIMO_MES") && cliente4.contains("\"montoMaximo\":1125.00"));
        comprobar("una cédula que no es de un cliente no trae monto máximo", !enviar("GET",
                "clientes/sujeto-credito?cedula=9999999999", token, null).contains("montoMaximo"));
        comprobar("la simulación de 450 a 6 cuotas da 78.65", enviar("GET", "creditos/simulacion?monto=450&cuotas=6",
                token, null).contains("\"valorCuota\":78.65"));

        String efectivo = vender(token, CLIENTE_1, "EFECTIVO", null, "{\"codTelefono\":1,\"cantidad\":2},"
                + "{\"codTelefono\":2,\"cantidad\":1}");
        comprobar("venta en efectivo al cliente 1 da 201 con su Location", estadoHttp == 201 && ubicacion != null
                && ubicacion.matches(".*/api/v1/facturas/\\d+"));
        comprobar("efectivo: subtotal 677.00, descuento 42 % = 284.34, total 392.66",
                efectivo.contains("\"subtotal\":677.00,\"descuento\":284.34,\"total\":392.66"));
        comprobar("efectivo: sin crédito, con el nombre del banco y el usuario de la sesión",
                !efectivo.contains("codCreditoBanco") && efectivo.contains("\"nombreCliente\":\"")
                && efectivo.contains("\"usuario\":\"MONSTER\""));
        String numeroEfectivo = ubicacion == null ? "0" : ubicacion.substring(ubicacion.lastIndexOf('/') + 1);
        comprobar("GET de la Location devuelve la factura con su detalle", enviar("GET", "facturas/" + numeroEfectivo,
                token, null).contains("\"detalles\":[{\"codTelefono\":1,"));

        comprobar("efectivo a una cédula que no es cliente da 404 y CLI-001", vender(token, "9999999999", "EFECTIVO",
                null, "{\"codTelefono\":1,\"cantidad\":1}").contains("CLI-001") && estadoHttp == 404);
        comprobar("crédito al cliente 3 (hombre de 22) da 409 y CRE-001", vender(token, "1734567890", "CREDITO", 6,
                "{\"codTelefono\":1,\"cantidad\":1}").contains("CRE-001") && estadoHttp == 409);
        comprobar("crédito al cliente 1 por 2399.00 (máximo 2250.00) da 409 y CRE-002", vender(token, CLIENTE_1,
                "CREDITO", 6, "{\"codTelefono\":8,\"cantidad\":1}").contains("CRE-002") && estadoHttp == 409);
        comprobar("crédito sin número de cuotas da 400 y FAC-002", vender(token, CLIENTE_1, "CREDITO", null,
                "{\"codTelefono\":5,\"cantidad\":1}").contains("FAC-002") && estadoHttp == 400);

        String credito = vender(token, CLIENTE_1, "CREDITO", 6, "{\"codTelefono\":5,\"cantidad\":1}");
        comprobar("crédito al cliente 1 por 899.00 a 6 cuotas da 201", estadoHttp == 201);
        comprobar("crédito: sin descuento, total 899.00, con el crédito del banco y 6 cuotas",
                credito.contains("\"descuento\":0.00,\"total\":899.00") && credito.contains("\"codCreditoBanco\":")
                && credito.contains("\"numCuotas\":6"));
        String codCredito = valor(credito, "codCreditoBanco");
        String tabla = enviar("GET", "creditos/" + codCredito + "/cuotas", token, null);
        comprobar("la tabla de amortización del crédito tiene 6 cuotas y cierra en 0.00",
                tabla.contains("\"numero\":6,") && tabla.endsWith("\"saldo\":0.00}]"));
        comprobar("los créditos del cliente 1 incluyen el nuevo", enviar("GET", "clientes/creditos?cedula=" + CLIENTE_1,
                token, null).contains("\"codCredito\":" + codCredito + ","));
        comprobar("un segundo crédito al cliente 1 da 409 (ya tiene un crédito activo)", vender(token, CLIENTE_1,
                "CREDITO", 3, "{\"codTelefono\":1,\"cantidad\":1}").contains("CRE-001") && estadoHttp == 409);

        String lista = enviar("GET", "facturas?cedula=" + CLIENTE_1, token, null);
        comprobar("las facturas del cliente 1 incluyen las dos ventas, sin detalle", lista.contains("\"numFactura\":"
                + numeroEfectivo + ",") && lista.contains("\"codCreditoBanco\":" + codCredito) && !lista.contains("detalles"));

        comprobar("un teléfono inexistente da 404 y TEL-001", vender(token, CLIENTE_1, "EFECTIVO", null,
                "{\"codTelefono\":99999,\"cantidad\":1}").contains("TEL-001") && estadoHttp == 404);
        comprobar("un teléfono repetido da 400 y DATA-001", vender(token, CLIENTE_1, "EFECTIVO", null,
                "{\"codTelefono\":1,\"cantidad\":1},{\"codTelefono\":1,\"cantidad\":1}").contains("DATA-001")
                && estadoHttp == 400);
        comprobar("una forma de pago desconocida da 400 y DATA-001", vender(token, CLIENTE_1, "TARJETA", null,
                "{\"codTelefono\":1,\"cantidad\":1}").contains("DATA-001") && estadoHttp == 400);
        comprobar("una venta sin teléfonos da 400 y DATA-001", vender(token, CLIENTE_1, "EFECTIVO", null, "")
                .contains("DATA-001") && estadoHttp == 400);

        String inactivo = enviar("POST", "telefonos", token, "{\"marca\":\"Prueba\",\"modelo\":\"Baja\",\"precio\":10.00}");
        String codInactivo = valor(inactivo, "codTelefono");
        enviar("DELETE", "telefonos/" + codInactivo, token, null);
        comprobar("un teléfono dado de baja da 409 y TEL-002", vender(token, CLIENTE_1, "EFECTIVO", null,
                "{\"codTelefono\":" + codInactivo + ",\"cantidad\":1}").contains("TEL-002") && estadoHttp == 409);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String vender(String token, String cedula, String formaPago, Integer numCuotas, String detalles)
            throws Exception {
        return enviar("POST", "facturas", token, "{\"cedula\":\"" + cedula + "\",\"formaPago\":\"" + formaPago + "\","
                + (numCuotas == null ? "" : "\"numCuotas\":" + numCuotas + ",") + "\"detalles\":[" + detalles + "]}");
    }

    /**
     * Valor numérico de un campo en un JSON plano ("campo":123).
     */
    private static String valor(String json, String campo) {
        int inicio = json.indexOf("\"" + campo + "\":");
        if (inicio < 0) {
            return "0";
        }
        inicio += campo.length() + 3;
        int fin = inicio;
        while (fin < json.length() && Character.isDigit(json.charAt(fin))) {
            fin++;
        }
        return json.substring(inicio, fin);
    }

    private static String login() throws Exception {
        String login = enviar("POST", "sesiones", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        return login.substring(inicio, login.indexOf("\"", inicio));
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
        ubicacion = conexion.getHeaderField("Location");
        InputStream entrada = estadoHttp < 400 ? conexion.getInputStream() : conexion.getErrorStream();
        return entrada == null ? "" : new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
