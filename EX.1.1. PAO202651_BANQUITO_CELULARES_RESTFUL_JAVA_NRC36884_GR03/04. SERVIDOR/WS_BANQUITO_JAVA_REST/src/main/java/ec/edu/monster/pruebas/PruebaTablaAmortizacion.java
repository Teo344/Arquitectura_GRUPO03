package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Prueba del servicio 4 (tabla de amortización) con el crédito 1 de los datos
 * semilla, de la consulta de créditos de un cliente y de la simulación. Solo
 * consulta, así que puede repetirse.
 *
 * @author carlo
 */
public class PruebaTablaAmortizacion {

    private static final String URL = "http://localhost:8080/WS_BANQUITO_JAVA_REST/api/v1/";
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaTablaAmortizacion ===");
        String clave = clave();

        String tabla = enviar("GET", "creditos/1/cuotas", clave);
        comprobar("el crédito 1 tiene 6 cuotas", estadoHttp == 200 && tabla.contains("\"numero\":6")
                && !tabla.contains("\"numero\":7"));
        comprobar("la cuota 1 es 78.65 / 6.19 / 72.46 / 377.54", tabla.contains(
                "{\"numero\":1,\"valorCuota\":78.65,\"interes\":6.19,\"capital\":72.46,\"saldo\":377.54}"));
        comprobar("la cuota 6 cierra en 0.00 (1.06 / 77.59)", tabla.contains(
                "{\"numero\":6,\"valorCuota\":78.65,\"interes\":1.06,\"capital\":77.59,\"saldo\":0.00}"));
        comprobar("la simulación de 450 a 6 cuotas es idéntica a la tabla guardada",
                enviar("GET", "creditos/simulacion?monto=450&cuotas=6", clave).equals(tabla));

        String credito = enviar("GET", "creditos/1", clave);
        comprobar("el crédito 1 es del cliente 5, ACTIVO y trae su tabla", credito.contains("\"codCliente\":5")
                && credito.contains("\"estado\":\"ACTIVO\"") && credito.contains("\"cuotas\":["));
        String lista = enviar("GET", "clientes/5/creditos", clave);
        comprobar("los créditos del cliente 5 incluyen el 1, sin la tabla", lista.contains("\"codCredito\":1,")
                && !lista.contains("cuotas"));
        comprobar("un cliente sin créditos da una lista vacía", enviar("GET", "clientes/4/creditos", clave).equals("[]"));

        comprobar("crédito inexistente da 404 y CRE-004", enviar("GET", "creditos/999/cuotas", clave)
                .contains("CRE-004") && estadoHttp == 404);
        comprobar("simular 2 cuotas da 400 y CRE-003", enviar("GET", "creditos/simulacion?monto=450&cuotas=2", clave)
                .contains("CRE-003") && estadoHttp == 400);
        comprobar("simular un monto no numérico da 400 y CRE-005", enviar("GET",
                "creditos/simulacion?monto=abc&cuotas=6", clave).contains("CRE-005") && estadoHttp == 400);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String clave() throws Exception {
        Properties propiedades = new Properties();
        try (InputStream entrada = PruebaTablaAmortizacion.class.getResourceAsStream("/db.properties")) {
            propiedades.load(entrada);
        }
        return propiedades.getProperty("api.clave").trim();
    }

    private static String enviar(String metodo, String ruta, String clave) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL + ruta).toURL().openConnection();
        conexion.setRequestMethod(metodo);
        conexion.setRequestProperty("X-Api-Key", clave);
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
