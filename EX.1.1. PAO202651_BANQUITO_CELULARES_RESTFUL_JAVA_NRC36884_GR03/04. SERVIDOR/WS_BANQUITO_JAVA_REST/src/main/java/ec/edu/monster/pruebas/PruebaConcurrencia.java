package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Prueba del bloqueo del cliente al otorgar un crédito: varias solicitudes
 * simultáneas para el cliente 2 terminan con exactamente un crédito (201) y
 * las demás rechazadas por crédito activo (409). Requiere la BD recién
 * cargada.
 *
 * @author carlo
 */
public class PruebaConcurrencia {

    private static final String URL = "http://localhost:8080/WS_BANQUITO_JAVA_REST/api/v1/";
    private static final int HILOS = 5;
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaConcurrencia ===");
        String clave = clave();
        comprobar("el cliente 2 aún es sujeto de crédito (BD recién cargada)",
                enviar("GET", "clientes/sujeto-credito?cedula=1723456789", clave, null).endsWith("true,\"motivos\":[]}"));

        ExecutorService ejecutor = Executors.newFixedThreadPool(HILOS);
        List<Future<String>> respuestas = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            respuestas.add(ejecutor.submit(() -> enviar("POST", "creditos", clave,
                    "{\"codCliente\":2,\"monto\":400.00,\"numCuotas\":3}")));
        }
        int creados = 0;
        int rechazados = 0;
        for (Future<String> respuesta : respuestas) {
            String cuerpo = respuesta.get();
            if (cuerpo.startsWith("201 ")) {
                creados++;
            } else if (cuerpo.startsWith("409 ") && cuerpo.contains("CRE-001")) {
                rechazados++;
            }
        }
        ejecutor.shutdown();
        comprobar(HILOS + " solicitudes simultáneas: exactamente un crédito creado", creados == 1);
        comprobar("las otras " + (HILOS - 1) + " dan 409 por crédito activo", rechazados == HILOS - 1);
        String lista = enviar("GET", "clientes/2/creditos", clave, null);
        comprobar("el cliente 2 tiene un solo crédito", lista.indexOf("codCredito") == lista.lastIndexOf("codCredito")
                && lista.contains("codCredito"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static String clave() throws Exception {
        Properties propiedades = new Properties();
        try (InputStream entrada = PruebaConcurrencia.class.getResourceAsStream("/db.properties")) {
            propiedades.load(entrada);
        }
        return propiedades.getProperty("api.clave").trim();
    }

    /**
     * Las respuestas de POST empiezan con el estado HTTP ("201 {...}") para
     * contarlas desde varios hilos sin compartir variables.
     */
    private static String enviar(String metodo, String ruta, String clave, String json) throws Exception {
        HttpURLConnection conexion = (HttpURLConnection) URI.create(URL + ruta).toURL().openConnection();
        conexion.setRequestMethod(metodo);
        conexion.setRequestProperty("X-Api-Key", clave);
        if (json != null) {
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json");
            conexion.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        }
        int estado = conexion.getResponseCode();
        InputStream entrada = estado < 400 ? conexion.getInputStream() : conexion.getErrorStream();
        String cuerpo = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        return json != null ? estado + " " + cuerpo : cuerpo;
    }

    private static synchronized void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
