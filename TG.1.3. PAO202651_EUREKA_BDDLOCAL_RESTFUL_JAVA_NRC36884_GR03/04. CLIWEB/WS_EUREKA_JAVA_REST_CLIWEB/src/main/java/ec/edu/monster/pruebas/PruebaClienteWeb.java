package ec.edu.monster.pruebas;

import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prueba de punta a punta del cliente web desplegado, como un navegador con
 * cookies: sesión, protección CSRF, consultas y un depósito con confirmación.
 * Necesita Payara con WS_EUREKA_JAVA_REST (y MySQL) y este cliente
 * desplegados. Cada ejecución deposita 1.00 en la cuenta 00100001.
 *
 * @author carlo
 */
public class PruebaClienteWeb {

    private static final String URL = "http://localhost:8080/WS_EUREKA_JAVA_REST_CLIWEB";
    private static final String CUENTA = "00100001";
    private static final Pattern CSRF = Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"");
    private static final Pattern SALDO = Pattern.compile("titular-mediano texto-azul-oscuro\">([^<]+)<");

    private static final HttpClient NAVEGADOR = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private static int fallidas = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaClienteWeb ===");
        HttpResponse<String> sinSesion = get("/menu");
        comprobar("sin sesión, /menu redirige al login", sinSesion.statusCode() == 302
                && sinSesion.headers().firstValue("Location").orElse("").endsWith("/login"));

        String csrf = token(get("/login").body());
        comprobar("el login trae el token CSRF", csrf != null);
        comprobar("un POST sin token CSRF da 403",
                post("/login", "usuario=MONSTER&clave=MONSTER9").statusCode() == 403);
        comprobar("clave incorrecta muestra AUTH-001",
                post("/login", "csrf=" + csrf + "&usuario=MONSTER&clave=otra").body().contains("AUTH-001"));
        HttpResponse<String> login = post("/login", "csrf=" + csrf + "&usuario=MONSTER&clave=MONSTER9");
        comprobar("MONSTER/MONSTER9 redirige al menú", login.statusCode() == 302
                && login.headers().firstValue("Location").orElse("").endsWith("/menu"));
        String menu = get("/menu").body();
        comprobar("el menú saluda al empleado y muestra su sucursal", menu.contains("Hola, ") && menu.contains("Sucursal "));

        BigDecimal antes = saldo();
        comprobar("consultar " + CUENTA + " muestra su saldo", antes != null);
        comprobar("una cuenta de 5 dígitos muestra la validación del escritorio",
                get("/consultas?modo=cuenta&cuenta=12345").body().contains("debe tener exactamente 8 dígitos"));
        comprobar("los movimientos muestran el historial",
                get("/consultas?modo=movimientos&cuenta=" + CUENTA).body().contains("Historial de movimientos"));

        String preparar = post("/transacciones/preparar",
                "csrf=" + csrf + "&tipo=DEPOSITO&cuenta=" + CUENTA + "&importe=1,00").body();
        comprobar("preparar un depósito pide confirmación", preparar.contains("Confirmar depósito"));
        HttpResponse<String> registrar = post("/transacciones/registrar", "csrf=" + csrf);
        comprobar("registrar redirige a la página de transacciones (Post/Redirect/Get)", registrar.statusCode() == 302);
        String comprobante = get("/transacciones?tipo=DEPOSITO").body();
        comprobar("el comprobante muestra la operación registrada", comprobante.contains("Operación registrada"));
        comprobar("el saldo sube exactamente 1.00", antes != null && antes.add(BigDecimal.ONE).equals(saldo()));
        post("/transacciones/registrar", "csrf=" + csrf);
        get("/transacciones?tipo=DEPOSITO");
        comprobar("reenviar o recargar no repite el depósito", antes != null && antes.add(BigDecimal.ONE).equals(saldo()));
        comprobar("importe con tres decimales muestra la validación", post("/transacciones/preparar",
                "csrf=" + csrf + "&tipo=DEPOSITO&cuenta=" + CUENTA + "&importe=1.234").body()
                .contains("máximo dos decimales"));

        HttpResponse<String> salir = post("/salir", "csrf=" + csrf);
        comprobar("salir redirige al login", salir.statusCode() == 302);
        comprobar("el login informa que la sesión se cerró", get("/login").body().contains("Sesión cerrada correctamente."));
        comprobar("después de salir, /menu vuelve a pedir login", get("/menu").statusCode() == 302);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static BigDecimal saldo() throws Exception {
        Matcher m = SALDO.matcher(get("/consultas?modo=cuenta&cuenta=" + CUENTA).body());
        return m.find() ? new BigDecimal(m.group(1).replace(",", "")) : null;
    }

    private static String token(String html) {
        Matcher m = CSRF.matcher(html);
        return m.find() ? URLEncoder.encode(m.group(1), StandardCharsets.UTF_8) : null;
    }

    private static HttpResponse<String> get(String ruta) throws Exception {
        return NAVEGADOR.send(HttpRequest.newBuilder(URI.create(URL + ruta)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static HttpResponse<String> post(String ruta, String formulario) throws Exception {
        return NAVEGADOR.send(HttpRequest.newBuilder(URI.create(URL + ruta))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formulario, StandardCharsets.UTF_8)).build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
