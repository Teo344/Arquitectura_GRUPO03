package ec.edu.monster.servicio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ec.edu.monster.modelo.CategoriaModelo;
import ec.edu.monster.modelo.ErrorRESTModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.UnidadModelo;
import java.io.IOException;
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

/** Única capa que conoce HTTP, JSON y las rutas del servidor REST. */
public class ClienteRESTConversor {

    private static final String URL_PREDETERMINADA =
            "http://localhost:8080/WS_CON_UNI_JAVA_REST/api";

    private final String baseUrl;
    private final HttpClient http;
    private SesionModelo sesion;

    public ClienteRESTConversor() {
        this(obtenerBaseConfigurada());
    }

    public ClienteRESTConversor(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("La URL REST es obligatoria.");
        }
        this.baseUrl = baseUrl.trim().replaceAll("/+$", "");
        URI direccion = URI.create(this.baseUrl);
        if (!"http".equalsIgnoreCase(direccion.getScheme())
                && !"https".equalsIgnoreCase(direccion.getScheme())) {
            throw new IllegalArgumentException("La URL REST debe usar HTTP o HTTPS.");
        }
        http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    private static String obtenerBaseConfigurada() {
        String propiedad = System.getProperty("rest.base.url");
        if (propiedad != null && !propiedad.isBlank()) {
            return propiedad;
        }
        String variable = System.getenv("REST_BASE_URL");
        return variable != null && !variable.isBlank()
                ? variable : URL_PREDETERMINADA;
    }

    public String getEndpointActivo() {
        return baseUrl;
    }

    public SesionModelo login(String usuario, String clave) {
        cerrarSesion();
        JsonObject datos = new JsonObject();
        datos.addProperty("usuario", usuario);
        datos.addProperty("clave", clave);
        JsonObject respuesta = solicitar("POST", "/autenticacion/login", datos, false);

        String token = textoObligatorio(respuesta, "token");
        String vencimiento = textoObligatorio(respuesta, "expiraEn");
        try {
            sesion = new SesionModelo(usuario, token, Instant.parse(vencimiento));
        } catch (DateTimeParseException ex) {
            throw new ErrorRESTModelo("El servidor devolvió una expiración inválida.", ex);
        }
        return sesion;
    }

    public void cerrarSesion() {
        // La API no tiene logout: eliminamos el token de este proceso.
        sesion = null;
    }

    public List<CategoriaModelo> consultarCatalogo() {
        JsonObject respuesta = solicitar("GET", "/catalogo/categorias", null, true);
        JsonArray datos = arregloObligatorio(respuesta, "categorias");
        List<CategoriaModelo> categorias = new ArrayList<>();
        for (JsonElement elemento : datos) {
            JsonObject categoria = elemento.getAsJsonObject();
            categorias.add(new CategoriaModelo(
                    textoObligatorio(categoria, "codigo"),
                    textoObligatorio(categoria, "nombre"),
                    booleanoObligatorio(categoria, "convertible")));
        }
        return List.copyOf(categorias);
    }

    public List<UnidadModelo> consultarUnidades(String categoria) {
        String codigo = URLEncoder.encode(categoria, StandardCharsets.UTF_8);
        JsonObject respuesta = solicitar("GET",
                "/catalogo/categorias/" + codigo + "/unidades", null, true);
        JsonArray datos = arregloObligatorio(respuesta, "unidades");
        List<UnidadModelo> unidades = new ArrayList<>();
        for (JsonElement elemento : datos) {
            JsonObject unidad = elemento.getAsJsonObject();
            unidades.add(new UnidadModelo(
                    textoObligatorio(unidad, "codigo"),
                    textoObligatorio(unidad, "nombre"),
                    textoObligatorio(unidad, "simbolo")));
        }
        return List.copyOf(unidades);
    }

    public double convertirLongitud(double valor, String origen, String destino) {
        return convertir("longitud", valor, origen, destino);
    }

    public double convertirMasa(double valor, String origen, String destino) {
        return convertir("masa", valor, origen, destino);
    }

    public double convertirTemperatura(double valor, String origen, String destino) {
        return convertir("temperatura", valor, origen, destino);
    }

    private double convertir(String tipo, double valor, String origen, String destino) {
        JsonObject datos = new JsonObject();
        datos.addProperty("valor", valor);
        datos.addProperty("unidadOrigen", origen);
        datos.addProperty("unidadDestino", destino);
        JsonObject respuesta = solicitar("POST", "/conversiones/" + tipo, datos, true);
        JsonElement resultado = respuesta.get("resultado");
        if (resultado == null || resultado.isJsonNull()) {
            throw new ErrorRESTModelo("El servidor no devolvió el resultado.", null);
        }
        try {
            return resultado.getAsDouble();
        } catch (RuntimeException ex) {
            throw new ErrorRESTModelo("El resultado del servidor no es numérico.", ex);
        }
    }

    private JsonObject solicitar(String metodo, String ruta, JsonObject cuerpo,
            boolean protegido) {
        HttpRequest.Builder peticion = HttpRequest.newBuilder(URI.create(baseUrl + ruta))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json");
        if (protegido) {
            if (sesion == null) {
                throw new ErrorRESTModelo(401, "AUTH-001", "Debe iniciar sesión.");
            }
            peticion.header("Authorization", "Bearer " + sesion.getToken());
        }
        if ("POST".equals(metodo)) {
            peticion.header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            cuerpo.toString(), StandardCharsets.UTF_8));
        } else {
            peticion.GET();
        }

        final HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ErrorRESTModelo("La petición REST fue interrumpida.", ex);
        } catch (IOException ex) {
            throw new ErrorRESTModelo("No se pudo conectar con " + baseUrl
                    + ". Verifique Payara y la URL REST.", ex);
        }

        JsonObject json = leerJson(respuesta.body());
        int estado = respuesta.statusCode();
        if (estado < 200 || estado >= 300) {
            String codigo = textoOpcional(json, "codigo");
            String mensaje = textoOpcional(json, "mensaje");
            if (estado == 401 && ("AUTH-001".equals(codigo)
                    || "AUTH-002".equals(codigo))) {
                cerrarSesion();
            }
            throw new ErrorRESTModelo(estado, codigo,
                    mensaje != null ? mensaje : "Error HTTP " + estado + ".");
        }
        if (json == null) {
            throw new ErrorRESTModelo("La respuesta REST no contiene JSON válido.", null);
        }
        if (!"OK".equals(textoOpcional(json, "estado"))) {
            throw new ErrorRESTModelo("El servidor devolvió un estado inesperado.", null);
        }
        return json;
    }

    private static JsonObject leerJson(String cuerpo) {
        try {
            return JsonParser.parseString(cuerpo).getAsJsonObject();
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static JsonArray arregloObligatorio(JsonObject json, String campo) {
        try {
            JsonArray arreglo = json.getAsJsonArray(campo);
            if (arreglo == null) {
                throw new IllegalStateException("Campo ausente");
            }
            return arreglo;
        } catch (RuntimeException ex) {
            throw new ErrorRESTModelo("El servidor no devolvió " + campo + ".", ex);
        }
    }

    private static boolean booleanoObligatorio(JsonObject json, String campo) {
        JsonElement valor = json.get(campo);
        if (valor == null || valor.isJsonNull()) {
            throw new ErrorRESTModelo("El servidor no devolvió " + campo + ".", null);
        }
        return valor.getAsBoolean();
    }

    private static String textoObligatorio(JsonObject json, String campo) {
        String valor = textoOpcional(json, campo);
        if (valor == null || valor.isBlank()) {
            throw new ErrorRESTModelo("El servidor no devolvió " + campo + ".", null);
        }
        return valor;
    }

    private static String textoOpcional(JsonObject json, String campo) {
        if (json == null || !json.has(campo) || json.get(campo).isJsonNull()) {
            return null;
        }
        return json.get(campo).getAsString();
    }
}
