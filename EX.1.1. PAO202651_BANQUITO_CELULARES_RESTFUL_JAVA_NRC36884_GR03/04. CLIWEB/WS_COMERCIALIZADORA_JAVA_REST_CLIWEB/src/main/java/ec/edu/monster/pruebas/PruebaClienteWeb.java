package ec.edu.monster.pruebas;

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
 * Prueba de punta a punta del cliente web MonsterCell, como un navegador con
 * cookies: sesión, CSRF, mantenimiento del catálogo, venta en efectivo, ventas
 * a crédito rechazadas y aprobada, tabla de amortización, facturas y salir.
 * Necesita Payara con WS_BANQUITO_JAVA_REST, WS_COMERCIALIZADORA_JAVA_REST y
 * este cliente, y MySQL con las BD del examen recién cargadas (01 + 02): la
 * venta a crédito al cliente 1 solo se puede aprobar una vez por carga.
 *
 * @author carlo
 */
public class PruebaClienteWeb {

    private static final String URL = "http://localhost:8080/WS_COMERCIALIZADORA_JAVA_REST_CLIWEB";
    private static final String CLIENTE_1 = "1712345678";
    private static final String CLIENTE_3 = "1734567890";
    private static final Pattern CSRF = Pattern.compile("name=\"csrf\" value=\"([^\"]+)\"");

    private static final HttpClient NAVEGADOR = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private static int fallidas = 0;
    private static String csrf;

    public static void main(String[] args) throws Exception {
        System.out.println("=== PruebaClienteWeb (MonsterCell) ===");
        HttpResponse<String> sinSesion = get("/venta");
        comprobar("sin sesión, /venta redirige al login", sinSesion.statusCode() == 302
                && ubicacion(sinSesion).endsWith("/login"));

        csrf = buscar(CSRF, get("/login").body());
        comprobar("el login trae el token CSRF", csrf != null);
        comprobar("un POST sin token CSRF da 403", NAVEGADOR.send(HttpRequest.newBuilder(URI.create(URL + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("usuario=MONSTER&clave=MONSTER9")).build(),
                HttpResponse.BodyHandlers.ofString()).statusCode() == 403);
        comprobar("clave incorrecta muestra AUTH-001", post("/login", "usuario=MONSTER&clave=otra").body().contains("AUTH-001"));
        HttpResponse<String> login = post("/login", "usuario=MONSTER&clave=MONSTER9");
        comprobar("MONSTER/MONSTER9 entra a la nueva venta con la barra lateral", login.statusCode() == 302
                && ubicacion(login).endsWith("/venta") && get("/venta").body().contains("Cerrar sesión"));
        comprobar("/menu lleva a la nueva venta", ubicacion(get("/menu")).endsWith("/venta"));

        // ---- Catálogo
        String modelo = "Modelo Web " + System.currentTimeMillis();
        post("/telefonos/guardar", "marca=Prueba&modelo=" + codificar(modelo) + "&precio=100.00");
        String catalogo = get("/telefonos").body();
        comprobar("agregar un teléfono lo muestra en el catálogo", catalogo.contains(modelo));
        String codigo = catalogo.contains(modelo)
                ? buscar(Pattern.compile("telefonos\\?editar=(\\d+)"), catalogo.substring(catalogo.indexOf(modelo))) : null;
        post("/telefonos/guardar", "codTelefono=" + codigo + "&marca=Prueba&modelo=" + codificar(modelo) + "&precio=120.50");
        comprobar("editarlo cambia su precio a 120.50", get("/telefonos").body().contains("120.50"));
        comprobar("precio 0 muestra la validación", post("/telefonos/guardar", "marca=A&modelo=B&precio=0").body()
                .contains("mayor que cero"));
        post("/telefonos/eliminar", "codTelefono=" + codigo);
        comprobar("darlo de baja lo quita del punto de venta", !get("/venta").body().contains(modelo));

        // ---- Venta en efectivo: 2 x 199 + 279
        post("/venta/vaciar", "");
        post("/venta/agregar", "codTelefono=1");
        post("/venta/agregar", "codTelefono=1");
        post("/venta/agregar", "codTelefono=2");
        post("/venta/comprador", "cedula=" + CLIENTE_1);
        post("/venta/pago", "formaPago=EFECTIVO");
        String venta = get("/venta").body();
        comprobar("el carrito verifica al comprador en BanQuito", venta.contains("Sujeto de crédito"));
        comprobar("el carrito muestra el total en efectivo 392.66", venta.contains("392.66"));
        comprobar("Facturar pide confirmación", post("/venta/preparar", "").body().contains("Confirmar venta"));
        HttpResponse<String> facturada = post("/venta/facturar", "");
        String factura = facturada.statusCode() == 302 ? get(ruta(ubicacion(facturada))).body() : "";
        comprobar("la factura en efectivo tiene 677.00 - 284.34 = 392.66", factura.contains("677.00")
                && factura.contains("284.34") && factura.contains("392.66"));
        String numeroEfectivo = buscar(Pattern.compile("Factura N\\.º (\\d+)"), factura);

        // ---- Crédito rechazado: cliente 3 (hombre de 22 años)
        post("/venta/vaciar", "");
        post("/venta/agregar", "codTelefono=1");
        post("/venta/comprador", "cedula=" + CLIENTE_3);
        post("/venta/pago", "formaPago=CREDITO&numCuotas=6");
        venta = get("/venta").body();
        comprobar("el cliente 3 aparece como no sujeto de crédito", venta.contains("No sujeto de crédito"));
        post("/venta/facturar", "");
        comprobar("facturarle a crédito se rechaza con el motivo", get("/venta").body().contains("no es sujeto de crédito"));

        // ---- Crédito que supera el máximo: 2399 para el cliente 1 (máximo 2250)
        post("/venta/vaciar", "");
        post("/venta/agregar", "codTelefono=8");
        post("/venta/comprador", "cedula=" + CLIENTE_1);
        post("/venta/pago", "formaPago=CREDITO&numCuotas=6");
        comprobar("2399 a crédito avisa que supera el monto máximo", get("/venta").body().contains("supera el monto máximo"));

        // ---- Crédito aprobado: 899 a 6 cuotas para el cliente 1
        post("/venta/vaciar", "");
        post("/venta/agregar", "codTelefono=5");
        post("/venta/comprador", "cedula=" + CLIENTE_1);
        post("/venta/pago", "formaPago=CREDITO&numCuotas=6");
        venta = get("/venta").body();
        boolean sujeto = venta.contains("Sujeto de crédito");
        comprobar("el cliente 1 aún es sujeto de crédito (BD recién cargada)", sujeto);
        comprobar("a crédito se muestra la cuota mensual estimada", venta.contains("Cuota mensual estimada"));
        facturada = post("/venta/facturar", "");
        factura = facturada.statusCode() == 302 ? get(ruta(ubicacion(facturada))).body() : "";
        String codCredito = buscar(Pattern.compile("/creditos/(\\d+)"), factura);
        comprobar("la factura a crédito trae el crédito BanQuito", factura.contains("Crédito BanQuito N.º") && codCredito != null);
        String tabla = codCredito == null ? "" : get("/creditos/" + codCredito).body();
        comprobar("la tabla de amortización tiene 6 cuotas y cierra en 0.00", contar(tabla, "data-etiqueta=\"N.º\"") == 6
                && tabla.contains("fila-final"));

        // ---- Consultas
        String facturas = get("/facturas?cedula=" + CLIENTE_1).body();
        comprobar("las facturas del cliente 1 incluyen la venta en efectivo", numeroEfectivo != null
                && facturas.contains(numeroEfectivo));
        comprobar("el simulador da 78.65 para 450 a 6 cuotas", get("/simulador?monto=450&cuotas=6").body().contains("78.65"));
        comprobar("Crédito BanQuito muestra el crédito del cliente 5", get("/clientes?cedula=1756789012").body()
                .contains("Crédito N.º"));

        HttpResponse<String> salir = post("/salir", "");
        comprobar("salir vuelve al login", salir.statusCode() == 302
                && get("/login").body().contains("Sesión cerrada correctamente."));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
        if (!sujeto) {
            System.out.println("Nota: recarga las BD del examen (01 + 02) para repetir la venta a crédito.");
        }
    }

    private static HttpResponse<String> get(String ruta) throws Exception {
        return NAVEGADOR.send(HttpRequest.newBuilder(URI.create(URL + ruta)).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    /** POST de formulario con el token CSRF de la sesión. */
    private static HttpResponse<String> post(String ruta, String formulario) throws Exception {
        String cuerpo = "csrf=" + codificar(csrf) + (formulario.isEmpty() ? "" : "&" + formulario);
        return NAVEGADOR.send(HttpRequest.newBuilder(URI.create(URL + ruta))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8)).build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String ubicacion(HttpResponse<String> respuesta) {
        return respuesta.headers().firstValue("Location").orElse("");
    }

    /** Ruta relativa al cliente a partir de una Location absoluta. */
    private static String ruta(String ubicacion) {
        int inicio = ubicacion.indexOf("/WS_COMERCIALIZADORA_JAVA_REST_CLIWEB");
        return inicio < 0 ? ubicacion : ubicacion.substring(inicio + "/WS_COMERCIALIZADORA_JAVA_REST_CLIWEB".length());
    }

    private static String buscar(Pattern patron, String texto) {
        Matcher m = patron.matcher(texto);
        return m.find() ? m.group(1) : null;
    }

    private static int contar(String texto, String fragmento) {
        int cantidad = 0;
        for (int i = texto.indexOf(fragmento); i >= 0; i = texto.indexOf(fragmento, i + 1)) {
            cantidad++;
        }
        return cantidad;
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
