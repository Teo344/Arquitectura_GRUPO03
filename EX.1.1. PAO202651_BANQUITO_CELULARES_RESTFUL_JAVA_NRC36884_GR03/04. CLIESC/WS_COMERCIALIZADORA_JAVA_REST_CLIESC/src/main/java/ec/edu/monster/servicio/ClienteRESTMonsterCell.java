package ec.edu.monster.servicio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ec.edu.monster.modelo.CreditoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.DetalleFacturaModelo;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.LineaCarrito;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.TelefonoModelo;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

/**
 * Única capa que conoce HTTP, JSON y las rutas de la API REST de la
 * comercializadora (/api/v1). Guarda la sesión del empleado, envía el token en
 * la cabecera Authorization: Bearer y convierte las respuestas en objetos del
 * modelo. Los errores llegan como application/problem+json (codigo y detail)
 * y se lanzan como ErrorServicioModelo. Es la misma clase del cliente web: el
 * LoginControlador crea una instancia por empleado y los demás controladores
 * la comparten. Sus métodos bloquean, así que los controladores los llaman
 * fuera del hilo de eventos de Swing.
 */
public class ClienteRESTMonsterCell {

    private static final String ARCHIVO_CONFIGURACION = "/servidor.properties";
    private static final String PROPIEDAD_URL = "servidor.url";
    private static final String SESION_INVALIDA = "AUTH-003";
    private static final String TIPO_JSON = "application/json";

    private static final Duration TIEMPO_CONEXION = Duration.ofSeconds(8);
    private static final Duration TIEMPO_RESPUESTA = Duration.ofSeconds(20);

    private final String baseUrl;
    // Un solo cliente HTTP reutiliza las conexiones; no sigue redirecciones para no enviar el token a otro host
    private final HttpClient http;
    private SesionModelo sesion;

    public ClienteRESTMonsterCell() {
        this.baseUrl = obtenerBaseConfigurada();
        this.http = HttpClient.newBuilder()
                .connectTimeout(TIEMPO_CONEXION)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    // -Dservidor.url tiene prioridad sobre servidor.properties
    private static String obtenerBaseConfigurada() {
        String base = System.getProperty(PROPIEDAD_URL);
        if (base == null || base.isBlank()) {
            Properties propiedades = new Properties();
            try (InputStream entrada = ClienteRESTMonsterCell.class.getResourceAsStream(ARCHIVO_CONFIGURACION)) {
                if (entrada == null) {
                    throw new IllegalStateException("No se encontró el archivo " + ARCHIVO_CONFIGURACION + ".");
                }
                propiedades.load(entrada);
            } catch (IOException ex) {
                throw new IllegalStateException("No se pudo leer " + ARCHIVO_CONFIGURACION + ".", ex);
            }
            base = propiedades.getProperty(PROPIEDAD_URL);
        }
        if (base == null || base.isBlank()) {
            throw new IllegalStateException("La URL del servidor (" + PROPIEDAD_URL + ") es obligatoria.");
        }
        String normalizada = base.trim().replaceAll("/+$", "");
        String esquema = URI.create(normalizada).getScheme();
        if (!"http".equalsIgnoreCase(esquema) && !"https".equalsIgnoreCase(esquema)) {
            throw new IllegalStateException("La URL del servidor debe usar HTTP o HTTPS.");
        }
        return normalizada;
    }

    public SesionModelo getSesion() {
        return sesion;
    }

    // ---------------------------------------------------------------- Sesión

    public SesionModelo iniciarSesion(String usuario, String clave) {
        sesion = null;
        JsonObject datos = new JsonObject();
        datos.addProperty("usuario", usuario);
        datos.addProperty("clave", clave);
        JsonObject respuesta = objeto(solicitar("POST", "/sesiones", datos, false));
        String token = textoObligatorio(respuesta, "token");
        try {
            sesion = new SesionModelo(token, Instant.parse(textoObligatorio(respuesta, "expiraEn")),
                    entero(respuesta, "codUsuario"), texto(respuesta, "usuario"), texto(respuesta, "nombre"));
        } catch (DateTimeParseException ex) {
            throw new ErrorServicioModelo("El servidor devolvió una expiración de sesión inválida.", ex);
        }
        return sesion;
    }

    /**
     * Invalida el token en el servidor. La sesión local se elimina siempre; si
     * el token ya había caducado (AUTH-003) no se considera error.
     */
    public void cerrarSesion() {
        if (sesion == null) {
            return;
        }
        try {
            solicitar("DELETE", "/sesiones/actual", null, true);
        } catch (ErrorServicioModelo ex) {
            if (!ex.requiereNuevoLogin()) {
                throw ex;
            }
        } finally {
            sesion = null;
        }
    }

    // ---------------------------------------------------------------- Catálogo

    public List<TelefonoModelo> telefonos() {
        return lista(solicitar("GET", "/telefonos", null, true), ClienteRESTMonsterCell::aTelefono);
    }

    public TelefonoModelo telefono(int codTelefono) {
        return aTelefono(objeto(solicitar("GET", "/telefonos/" + codTelefono, null, true)));
    }

    public TelefonoModelo crearTelefono(String marca, String modelo, BigDecimal precio) {
        return aTelefono(objeto(solicitar("POST", "/telefonos", datosTelefono(marca, modelo, precio), true)));
    }

    public TelefonoModelo actualizarTelefono(int codTelefono, String marca, String modelo, BigDecimal precio) {
        return aTelefono(objeto(solicitar("PUT", "/telefonos/" + codTelefono, datosTelefono(marca, modelo, precio), true)));
    }

    /** Baja lógica: el teléfono queda INACTIVO y ya no se puede vender. */
    public void eliminarTelefono(int codTelefono) {
        solicitar("DELETE", "/telefonos/" + codTelefono, null, true);
    }

    // ---------------------------------------------------------------- Crédito BanQuito

    /** Servicios 1 y 2 del banco: si es sujeto de crédito y su monto máximo. */
    public EvaluacionClienteModelo evaluarCliente(String cedula) {
        JsonObject datos = objeto(solicitar("GET", "/clientes/sujeto-credito?cedula=" + codificar(cedula), null, true));
        List<EvaluacionClienteModelo.Motivo> motivos = new ArrayList<>();
        JsonElement lista = datos.get("motivos");
        if (lista != null && lista.isJsonArray()) {
            for (JsonElement elemento : lista.getAsJsonArray()) {
                JsonObject motivo = elemento.getAsJsonObject();
                motivos.add(new EvaluacionClienteModelo.Motivo(texto(motivo, "codigo"), texto(motivo, "descripcion")));
            }
        }
        return new EvaluacionClienteModelo(texto(datos, "cedula"), enteroOpcional(datos, "codCliente"),
                texto(datos, "nombre"), datos.has("sujetoCredito") && datos.get("sujetoCredito").getAsBoolean(),
                motivos, decimal(datos, "montoMaximo"));
    }

    public List<CreditoModelo> creditos(String cedula) {
        return lista(solicitar("GET", "/clientes/creditos?cedula=" + codificar(cedula), null, true),
                c -> new CreditoModelo(entero(c, "codCredito"), texto(c, "fecha"), decimal(c, "monto"),
                        entero(c, "numCuotas"), decimal(c, "tasaAnual"), decimal(c, "valorCuota"), texto(c, "estado")));
    }

    /** Servicio 4 del banco: la tabla de amortización de un crédito otorgado. */
    public List<CuotaModelo> cuotas(int codCredito) {
        return lista(solicitar("GET", "/creditos/" + codCredito + "/cuotas", null, true), ClienteRESTMonsterCell::aCuota);
    }

    /** La tabla que tendría un crédito, sin otorgarlo. */
    public List<CuotaModelo> simular(BigDecimal monto, int cuotas) {
        return lista(solicitar("GET", "/creditos/simulacion?monto=" + codificar(monto.toPlainString())
                + "&cuotas=" + cuotas, null, true), ClienteRESTMonsterCell::aCuota);
    }

    // ---------------------------------------------------------------- Facturas

    /**
     * Factura las líneas del carrito. Solo se envían el teléfono y la cantidad:
     * el servidor toma los precios del catálogo y, a crédito, pide el crédito
     * al banco.
     */
    public FacturaModelo facturar(String cedula, FormaPago formaPago, Integer numCuotas, List<LineaCarrito> lineas) {
        JsonObject datos = new JsonObject();
        datos.addProperty("cedula", cedula);
        datos.addProperty("formaPago", formaPago.name());
        if (formaPago == FormaPago.CREDITO) {
            datos.addProperty("numCuotas", numCuotas);
        }
        JsonArray detalles = new JsonArray();
        for (LineaCarrito linea : lineas) {
            JsonObject detalle = new JsonObject();
            detalle.addProperty("codTelefono", linea.getTelefono().getCodTelefono());
            detalle.addProperty("cantidad", linea.getCantidad());
            detalles.add(detalle);
        }
        datos.add("detalles", detalles);
        return aFactura(objeto(solicitar("POST", "/facturas", datos, true)));
    }

    public FacturaModelo factura(int numFactura) {
        return aFactura(objeto(solicitar("GET", "/facturas/" + numFactura, null, true)));
    }

    /** Las facturas más recientes primero; con cédula vacía, las de todos los clientes. */
    public List<FacturaModelo> facturas(String cedula) {
        String ruta = cedula == null || cedula.isBlank() ? "/facturas" : "/facturas?cedula=" + codificar(cedula.trim());
        return lista(solicitar("GET", ruta, null, true), ClienteRESTMonsterCell::aFactura);
    }

    // ---------------------------------------------------------------- Conversión

    private static JsonObject datosTelefono(String marca, String modelo, BigDecimal precio) {
        JsonObject datos = new JsonObject();
        datos.addProperty("marca", marca);
        datos.addProperty("modelo", modelo);
        datos.addProperty("precio", precio);
        return datos;
    }

    private static TelefonoModelo aTelefono(JsonObject t) {
        return new TelefonoModelo(entero(t, "codTelefono"), texto(t, "marca"), texto(t, "modelo"), decimal(t, "precio"),
                decimal(t, "precioEfectivo"), texto(t, "estado"));
    }

    private static CuotaModelo aCuota(JsonObject c) {
        return new CuotaModelo(entero(c, "numero"), decimal(c, "valorCuota"), decimal(c, "interes"),
                decimal(c, "capital"), decimal(c, "saldo"));
    }

    private static FacturaModelo aFactura(JsonObject f) {
        List<DetalleFacturaModelo> detalles = new ArrayList<>();
        JsonElement lista = f.get("detalles");
        if (lista != null && lista.isJsonArray()) {
            for (JsonElement elemento : lista.getAsJsonArray()) {
                JsonObject d = elemento.getAsJsonObject();
                detalles.add(new DetalleFacturaModelo(entero(d, "codTelefono"), texto(d, "marca"), texto(d, "modelo"),
                        entero(d, "cantidad"), decimal(d, "precioUnitario"), decimal(d, "subtotal")));
            }
        }
        FormaPago formaPago = "CREDITO".equals(texto(f, "formaPago")) ? FormaPago.CREDITO : FormaPago.EFECTIVO;
        return new FacturaModelo(entero(f, "numFactura"), texto(f, "fecha"), texto(f, "usuario"), texto(f, "cedula"),
                entero(f, "codClienteBanco"), texto(f, "nombreCliente"), formaPago, decimal(f, "subtotal"),
                decimal(f, "descuento"), decimal(f, "total"), enteroOpcional(f, "codCreditoBanco"),
                enteroOpcional(f, "numCuotas"), detalles);
    }

    // ---------------------------------------------------------------- HTTP

    /**
     * Arma la petición, agrega el token si el recurso es protegido y devuelve
     * el JSON de una respuesta correcta (2xx), o null si no tiene cuerpo (204).
     */
    private JsonElement solicitar(String metodo, String ruta, JsonObject cuerpo, boolean protegido) {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(baseUrl + ruta))
                .timeout(TIEMPO_RESPUESTA)
                .header("Accept", TIPO_JSON);
        if (protegido) {
            if (sesion == null) {
                throw new ErrorServicioModelo(SESION_INVALIDA, "Debe iniciar sesión.");
            }
            peticion.header("Authorization", "Bearer " + sesion.getToken());
        }
        if (cuerpo != null) {
            peticion.header("Content-Type", TIPO_JSON + "; charset=UTF-8")
                    .method(metodo, HttpRequest.BodyPublishers.ofString(cuerpo.toString(), StandardCharsets.UTF_8));
        } else {
            peticion.method(metodo, HttpRequest.BodyPublishers.noBody());
        }

        final HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ErrorServicioModelo("La petición al servidor fue interrumpida.", ex);
        } catch (IOException ex) {
            throw new ErrorServicioModelo("No se pudo conectar con " + baseUrl
                    + ". Verifique que Payara esté activo.", ex);
        }

        JsonElement json = leerJson(respuesta.body());
        int estado = respuesta.statusCode();
        if (estado < 200 || estado >= 300) {
            JsonObject problema = json != null && json.isJsonObject() ? json.getAsJsonObject() : null;
            String codigo = texto(problema, "codigo");
            String detalle = texto(problema, "detail");
            if (SESION_INVALIDA.equals(codigo)) {
                sesion = null;
            }
            throw new ErrorServicioModelo(codigo, detalle != null ? detalle : "Error HTTP " + estado + ".");
        }
        return json;
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------- JSON

    private static JsonElement leerJson(String cuerpo) {
        if (cuerpo == null || cuerpo.isBlank()) {
            return null;
        }
        try {
            return JsonParser.parseString(cuerpo);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static JsonObject objeto(JsonElement json) {
        if (json == null || !json.isJsonObject()) {
            throw new ErrorServicioModelo(null, "El servidor devolvió una respuesta inesperada.");
        }
        return json.getAsJsonObject();
    }

    private static <T> List<T> lista(JsonElement json, Function<JsonObject, T> convertir) {
        if (json == null || !json.isJsonArray()) {
            throw new ErrorServicioModelo(null, "El servidor devolvió una respuesta inesperada.");
        }
        List<T> elementos = new ArrayList<>();
        for (JsonElement elemento : json.getAsJsonArray()) {
            elementos.add(convertir.apply(elemento.getAsJsonObject()));
        }
        return List.copyOf(elementos);
    }

    private static String textoObligatorio(JsonObject json, String campo) {
        String valor = texto(json, campo);
        if (valor == null || valor.isBlank()) {
            throw new ErrorServicioModelo(null, "El servidor no devolvió " + campo + ".");
        }
        return valor;
    }

    private static String texto(JsonObject json, String campo) {
        if (json == null || !json.has(campo) || json.get(campo).isJsonNull()) {
            return null;
        }
        return json.get(campo).getAsString();
    }

    private static BigDecimal decimal(JsonObject json, String campo) {
        if (json == null || !json.has(campo) || json.get(campo).isJsonNull()) {
            return null;
        }
        try {
            return json.get(campo).getAsBigDecimal();
        } catch (RuntimeException ex) {
            throw new ErrorServicioModelo("El servidor devolvió un valor no numérico en " + campo + ".", ex);
        }
    }

    private static Integer enteroOpcional(JsonObject json, String campo) {
        if (json == null || !json.has(campo) || json.get(campo).isJsonNull()) {
            return null;
        }
        try {
            return json.get(campo).getAsInt();
        } catch (RuntimeException ex) {
            throw new ErrorServicioModelo("El servidor devolvió un valor no numérico en " + campo + ".", ex);
        }
    }

    private static int entero(JsonObject json, String campo) {
        Integer valor = enteroOpcional(json, campo);
        return valor == null ? 0 : valor;
    }
}
