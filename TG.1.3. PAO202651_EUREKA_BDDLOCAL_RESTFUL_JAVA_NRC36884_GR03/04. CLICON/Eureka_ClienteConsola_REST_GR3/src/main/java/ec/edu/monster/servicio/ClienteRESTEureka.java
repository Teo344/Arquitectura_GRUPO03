package ec.edu.monster.servicio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ec.edu.monster.modelo.CuentaModelo;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.MovimientoModelo;
import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import ec.edu.monster.modelo.SesionModelo;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
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

/**
 * Única capa que conoce HTTP, JSON y las rutas de la API REST de EurekaBank
 * (/api). Guarda la sesión activa, envía el token en la cabecera
 * Authorization: Bearer y convierte las respuestas y los errores JSON en
 * objetos del modelo.
 */
public class ClienteRESTEureka {

    private static final String ARCHIVO_CONFIGURACION = "/servidor.properties";
    private static final String PROPIEDAD_URL = "servidor.url";
    private static final String ESTADO_OK = "OK";
    private static final String SESION_INVALIDA = "AUTH-003";
    private static final String TIPO_JSON = "application/json";

    private static final Duration TIEMPO_CONEXION = Duration.ofSeconds(8);
    private static final Duration TIEMPO_RESPUESTA = Duration.ofSeconds(20);

    private final String baseUrl;
    // Un solo cliente HTTP reutiliza las conexiones; no sigue redirecciones para no enviar el token a otro host
    private final HttpClient http;
    private SesionModelo sesion;

    public ClienteRESTEureka() {
        this.baseUrl = obtenerBaseConfigurada();
        this.http = HttpClient.newBuilder()
                .connectTimeout(TIEMPO_CONEXION)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    // -Dservidor.url tiene prioridad sobre servidor.properties (RNF-010)
    private static String obtenerBaseConfigurada() {
        String base = System.getProperty(PROPIEDAD_URL);
        if (base == null || base.isBlank()) {
            Properties propiedades = new Properties();
            try (InputStream entrada = ClienteRESTEureka.class.getResourceAsStream(ARCHIVO_CONFIGURACION)) {
                if (entrada == null) {
                    throw new IllegalStateException("No se encontro el archivo " + ARCHIVO_CONFIGURACION + ".");
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

    public String getEndpointActivo() {
        return baseUrl;
    }

    public SesionModelo getSesion() {
        return sesion;
    }

    // ---------------------------------------------------------------- Autenticación

    public SesionModelo iniciarSesion(String usuario, String clave) {
        sesion = null;
        JsonObject datos = new JsonObject();
        datos.addProperty("usuario", usuario);
        datos.addProperty("clave", clave);
        datos.addProperty("equipo", nombreEquipo());
        JsonObject respuesta = solicitar("POST", "/autenticacion/login", datos, false);

        String token = textoObligatorio(respuesta, "token");
        try {
            sesion = new SesionModelo(token, Instant.parse(textoObligatorio(respuesta, "expiraEn")),
                    textoOpcional(respuesta, "codigoEmpleado"), textoOpcional(respuesta, "nombreEmpleado"),
                    textoOpcional(respuesta, "codigoSucursal"), textoOpcional(respuesta, "sucursal"));
        } catch (DateTimeParseException ex) {
            throw new ErrorServicioModelo("El servidor devolvio una expiracion de sesion invalida.", ex);
        }
        return sesion;
    }

    /**
     * Invalida el token en el servidor (RQF-002). La sesión local se elimina
     * siempre; si el token ya había caducado (AUTH-003) no se considera error.
     */
    public void cerrarSesion() {
        if (sesion == null) {
            return;
        }
        String token = sesion.getToken();
        sesion = null;
        try {
            enviar(construir("/autenticacion/cerrar-sesion", token)
                    .POST(HttpRequest.BodyPublishers.noBody()).build());
        } catch (ErrorServicioModelo ex) {
            if (!ex.requiereNuevoLogin()) {
                throw ex;
            }
        }
    }

    // ---------------------------------------------------------------- Consultas

    public CuentaModelo consultarCuenta(String cuenta) {
        JsonObject respuesta = solicitar("GET", "/cuentas/" + segmento(cuenta), null, true);
        JsonObject datos = objetoObligatorio(respuesta, "cuenta");
        return new CuentaModelo(textoObligatorio(datos, "codigo"), textoOpcional(datos, "titular"),
                textoOpcional(datos, "moneda"), decimalOpcional(datos, "saldo"), textoOpcional(datos, "estado"),
                textoOpcional(datos, "sucursal"), textoOpcional(datos, "fechaCreacion"));
    }

    /** Una cuenta sin movimientos devuelve una lista vacía. */
    public List<MovimientoModelo> consultarMovimientos(String cuenta) {
        JsonObject respuesta = solicitar("GET", "/cuentas/" + segmento(cuenta) + "/movimientos", null, true);
        JsonElement lista = respuesta.get("movimientos");
        if (lista == null || lista.isJsonNull()) {
            return List.of();
        }
        if (!lista.isJsonArray()) {
            throw new ErrorServicioModelo(null, "El servidor no devolvio la lista de movimientos.");
        }
        JsonArray datos = lista.getAsJsonArray();
        List<MovimientoModelo> movimientos = new ArrayList<>(datos.size());
        for (JsonElement elemento : datos) {
            JsonObject movimiento = elemento.getAsJsonObject();
            Integer numero = enteroOpcional(movimiento, "numero");
            movimientos.add(new MovimientoModelo(numero == null ? 0 : numero, textoOpcional(movimiento, "fecha"),
                    textoOpcional(movimiento, "tipo"), textoOpcional(movimiento, "accion"),
                    decimalOpcional(movimiento, "importe"), textoOpcional(movimiento, "cuentaReferencia")));
        }
        return List.copyOf(movimientos);
    }

    // ---------------------------------------------------------------- Transacciones

    public ResultadoTransaccionModelo depositar(String cuenta, BigDecimal importe) {
        JsonObject datos = new JsonObject();
        datos.addProperty("cuenta", cuenta);
        datos.addProperty("importe", importe);
        return aResultado(solicitar("POST", "/transacciones/depositos", datos, true));
    }

    public ResultadoTransaccionModelo retirar(String cuenta, BigDecimal importe) {
        JsonObject datos = new JsonObject();
        datos.addProperty("cuenta", cuenta);
        datos.addProperty("importe", importe);
        return aResultado(solicitar("POST", "/transacciones/retiros", datos, true));
    }

    public ResultadoTransaccionModelo transferir(String cuentaOrigen, String cuentaDestino, BigDecimal importe) {
        JsonObject datos = new JsonObject();
        datos.addProperty("cuentaOrigen", cuentaOrigen);
        datos.addProperty("cuentaDestino", cuentaDestino);
        datos.addProperty("importe", importe);
        return aResultado(solicitar("POST", "/transacciones/transferencias", datos, true));
    }

    private static ResultadoTransaccionModelo aResultado(JsonObject respuesta) {
        return new ResultadoTransaccionModelo(textoOpcional(respuesta, "mensaje"),
                decimalOpcional(respuesta, "nuevoSaldo"), enteroOpcional(respuesta, "numeroMovimiento"),
                enteroOpcional(respuesta, "numeroMovimientoDestino"));
    }

    // ---------------------------------------------------------------- HTTP

    /**
     * Arma la petición, agrega el token si el recurso es protegido y devuelve
     * el JSON de una respuesta correcta (estado OK).
     */
    private JsonObject solicitar(String metodo, String ruta, JsonObject cuerpo, boolean protegido) {
        String token = null;
        if (protegido) {
            if (sesion == null) {
                throw new ErrorServicioModelo(SESION_INVALIDA, "Debe iniciar sesion.");
            }
            token = sesion.getToken();
        }
        HttpRequest.Builder peticion = construir(ruta, token);
        if ("POST".equals(metodo)) {
            peticion.header("Content-Type", TIPO_JSON + "; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString(), StandardCharsets.UTF_8));
        } else {
            peticion.GET();
        }
        return enviar(peticion.build());
    }

    private HttpRequest.Builder construir(String ruta, String token) {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(baseUrl + ruta))
                .timeout(TIEMPO_RESPUESTA)
                .header("Accept", TIPO_JSON);
        if (token != null) {
            peticion.header("Authorization", "Bearer " + token);
        }
        return peticion;
    }

    /**
     * Envía la petición y traduce el resultado: un estado distinto de 2xx trae
     * el JSON de error {estado, codigo, mensaje}; una falla de red es un error
     * de comunicación con el servidor.
     */
    private JsonObject enviar(HttpRequest peticion) {
        final HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ErrorServicioModelo("La peticion al servidor fue interrumpida.", ex);
        } catch (IOException ex) {
            throw new ErrorServicioModelo("No se pudo conectar con " + baseUrl
                    + ". Verifique que Payara este activo.", ex);
        }

        JsonObject json = leerJson(respuesta.body());
        int estado = respuesta.statusCode();
        if (estado < 200 || estado >= 300) {
            String codigo = textoOpcional(json, "codigo");
            String mensaje = textoOpcional(json, "mensaje");
            if (SESION_INVALIDA.equals(codigo)) {
                sesion = null;
            }
            throw new ErrorServicioModelo(codigo, mensaje != null ? mensaje : "Error HTTP " + estado + ".");
        }
        if (json == null || !ESTADO_OK.equals(textoOpcional(json, "estado"))) {
            throw new ErrorServicioModelo(null, "El servidor devolvio una respuesta inesperada.");
        }
        return json;
    }

    // El controlador ya validó los 8 dígitos; se codifica igual para no alterar la ruta
    private static String segmento(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    // ---------------------------------------------------------------- JSON

    private static JsonObject leerJson(String cuerpo) {
        try {
            JsonElement elemento = JsonParser.parseString(cuerpo);
            return elemento.isJsonObject() ? elemento.getAsJsonObject() : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static JsonObject objetoObligatorio(JsonObject json, String campo) {
        JsonElement valor = json.get(campo);
        if (valor == null || !valor.isJsonObject()) {
            throw new ErrorServicioModelo(null, "El servidor no devolvio " + campo + ".");
        }
        return valor.getAsJsonObject();
    }

    private static String textoObligatorio(JsonObject json, String campo) {
        String valor = textoOpcional(json, campo);
        if (valor == null || valor.isBlank()) {
            throw new ErrorServicioModelo(null, "El servidor no devolvio " + campo + ".");
        }
        return valor;
    }

    private static String textoOpcional(JsonObject json, String campo) {
        if (json == null || !json.has(campo) || json.get(campo).isJsonNull()) {
            return null;
        }
        return json.get(campo).getAsString();
    }

    private static BigDecimal decimalOpcional(JsonObject json, String campo) {
        if (json == null || !json.has(campo) || json.get(campo).isJsonNull()) {
            return null;
        }
        try {
            return json.get(campo).getAsBigDecimal();
        } catch (RuntimeException ex) {
            throw new ErrorServicioModelo("El servidor devolvio un valor no numerico en " + campo + ".", ex);
        }
    }

    private static Integer enteroOpcional(JsonObject json, String campo) {
        if (json == null || !json.has(campo) || json.get(campo).isJsonNull()) {
            return null;
        }
        try {
            return json.get(campo).getAsInt();
        } catch (RuntimeException ex) {
            throw new ErrorServicioModelo("El servidor devolvio un valor no numerico en " + campo + ".", ex);
        }
    }

    // Nombre del equipo que el servidor guarda en logsession (RQF-001)
    private static String nombreEquipo() {
        String equipo = System.getenv("COMPUTERNAME");
        if (equipo != null && !equipo.isBlank()) {
            return equipo;
        }
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException ex) {
            return null;
        }
    }
}
