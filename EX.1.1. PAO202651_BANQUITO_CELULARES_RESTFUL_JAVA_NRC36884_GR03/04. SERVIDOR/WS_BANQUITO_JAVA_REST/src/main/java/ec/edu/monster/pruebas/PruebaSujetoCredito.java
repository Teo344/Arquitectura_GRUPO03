package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Prueba del servicio 1 (¿es sujeto de crédito?) y de la búsqueda de clientes
 * por cédula, con un cliente de los datos semilla para cada regla. Requiere la
 * BD recién cargada: después de PruebaOtorgarCredito el cliente 6 ya tiene un
 * crédito activo.
 *
 * @author carlo
 */
public class PruebaSujetoCredito {

    private static final String URL = "http://localhost:8080/WS_BANQUITO_JAVA_REST/api/v1/";
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaSujetoCredito ===");
        String clave = clave();

        comprobar("cliente 1 (mujer, 35) es sujeto de crédito", sujeto(clave, "1712345678"));
        comprobar("cliente 2 (hombre, 40) es sujeto de crédito", sujeto(clave, "1723456789"));
        comprobar("cliente 6 (mujer, 26, dos cuentas) es sujeto de crédito", sujeto(clave, "1767890123"));
        comprobar("cliente 3 no: HOMBRE_MENOR_25", rechazo(clave, "1734567890", "HOMBRE_MENOR_25"));
        comprobar("cliente 4 no: SIN_DEPOSITO_ULTIMO_MES", rechazo(clave, "1745678901", "SIN_DEPOSITO_ULTIMO_MES"));
        comprobar("cliente 5 no: CREDITO_ACTIVO", rechazo(clave, "1756789012", "CREDITO_ACTIVO"));
        String noCliente = evaluar(clave, "9999999999");
        comprobar("9999999999 no: NO_ES_CLIENTE (200, no es un error)", estadoHttp == 200
                && noCliente.contains("\"codigo\":\"NO_ES_CLIENTE\"") && !noCliente.contains("codCliente"));
        comprobar("cédula con letras da 400 y DATA-001", evaluar(clave, "17123abc78").contains("DATA-001")
                && estadoHttp == 400);
        comprobar("sin cédula da 400", enviar("GET", "clientes/sujeto-credito", clave).contains("DATA-001")
                && estadoHttp == 400);

        String cliente = enviar("GET", "clientes?cedula=1712345678", clave);
        comprobar("buscar 1712345678 da el cliente 1", estadoHttp == 200 && cliente.contains("\"codCliente\":1,"));
        comprobar("buscar 9999999999 da 404 y CLI-001", enviar("GET", "clientes?cedula=9999999999", clave)
                .contains("CLI-001") && estadoHttp == 404);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static boolean sujeto(String clave, String cedula) throws Exception {
        String respuesta = evaluar(clave, cedula);
        return estadoHttp == 200 && respuesta.contains("\"sujetoCredito\":true") && respuesta.contains("\"motivos\":[]");
    }

    private static boolean rechazo(String clave, String cedula, String motivo) throws Exception {
        String respuesta = evaluar(clave, cedula);
        return estadoHttp == 200 && respuesta.contains("\"sujetoCredito\":false")
                && respuesta.contains("\"codigo\":\"" + motivo + "\"");
    }

    private static String evaluar(String clave, String cedula) throws Exception {
        return enviar("GET", "clientes/sujeto-credito?cedula=" + cedula, clave);
    }

    private static String clave() throws Exception {
        Properties propiedades = new Properties();
        try (InputStream entrada = PruebaSujetoCredito.class.getResourceAsStream("/db.properties")) {
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
