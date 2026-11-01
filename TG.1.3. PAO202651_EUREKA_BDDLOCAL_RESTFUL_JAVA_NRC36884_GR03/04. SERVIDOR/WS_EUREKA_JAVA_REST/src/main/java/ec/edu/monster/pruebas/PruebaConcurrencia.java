package ec.edu.monster.pruebas;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Prueba del bloqueo de filas (RN-07): depósitos simultáneos en una cuenta no
 * pierden ninguna actualización, y transferencias cruzadas A→B y B→A al mismo
 * tiempo terminan sin deadlock y sin alterar la suma de los saldos.
 *
 * @author carlo
 */
public class PruebaConcurrencia {

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_REST/api/";
    private static final int HILOS = 10;
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaConcurrencia ===");
        String login = enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.indexOf("\"token\":\"") + 9;
        String token = login.substring(inicio, login.indexOf("\"", inicio));
        ExecutorService ejecutor = Executors.newFixedThreadPool(HILOS);

        BigDecimal antes = saldo(token, "00100001");
        List<Future<String>> depositos = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            depositos.add(ejecutor.submit(() -> enviar("POST", "transacciones/depositos", token,
                    "{\"cuenta\":\"00100001\",\"importe\":1.00}")));
        }
        comprobar(HILOS + " depósitos simultáneos dan OK-000", todosOk(depositos));
        comprobar("el saldo sube exactamente " + HILOS + ".00",
                saldo(token, "00100001").equals(antes.add(new BigDecimal(HILOS + ".00"))));

        BigDecimal suma = saldo(token, "00200001").add(saldo(token, "00200002"));
        List<Future<String>> transferencias = new ArrayList<>();
        for (int i = 0; i < HILOS; i++) {
            String origen = i % 2 == 0 ? "00200001" : "00200002";
            String destino = i % 2 == 0 ? "00200002" : "00200001";
            transferencias.add(ejecutor.submit(() -> enviar("POST", "transacciones/transferencias", token,
                    "{\"cuentaOrigen\":\"" + origen + "\",\"cuentaDestino\":\"" + destino + "\",\"importe\":1.00}")));
        }
        comprobar(HILOS + " transferencias cruzadas dan OK-000 (sin deadlock)", todosOk(transferencias));
        comprobar("la suma de los saldos no cambia",
                saldo(token, "00200001").add(saldo(token, "00200002")).equals(suma));

        ejecutor.shutdown();
        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static boolean todosOk(List<Future<String>> respuestas) throws Exception {
        boolean correcto = true;
        for (Future<String> respuesta : respuestas) {
            correcto &= respuesta.get().contains("\"codigo\":\"OK-000\"");
        }
        return correcto;
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
        InputStream entrada = conexion.getResponseCode() < 400 ? conexion.getInputStream() : conexion.getErrorStream();
        return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static synchronized void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
