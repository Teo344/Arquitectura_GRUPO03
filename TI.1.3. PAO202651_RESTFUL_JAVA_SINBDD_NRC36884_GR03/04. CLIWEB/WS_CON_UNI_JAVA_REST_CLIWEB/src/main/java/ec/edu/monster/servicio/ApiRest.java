package ec.edu.monster.servicio;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.Sesion;
import ec.edu.monster.modelo.Unidad;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/** Capa de acceso HTTP: encapsula endpoints REST y JSON del servidor. */
public final class ApiRest {
    private final String base;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    private final Gson gson = new Gson();
    private static final Type CATEGORIAS = new TypeToken<List<Categoria>>() {}.getType();
    private static final Type UNIDADES = new TypeToken<List<Unidad>>() {}.getType();

    public ApiRest(String base) { this.base = base.replaceAll("/+$", ""); }

    public Sesion login(String usuario, String clave) throws IOException, InterruptedException {
        JsonObject body = new JsonObject(); body.addProperty("usuario", usuario); body.addProperty("clave", clave);
        JsonObject json = request("POST", "/autenticacion/login", null, body.toString());
        return new Sesion(usuario, required(json, "token"), json.has("expiraEn") ? json.get("expiraEn").getAsString() : "");
    }

    public List<Categoria> categorias(String token) throws IOException, InterruptedException {
        JsonObject json = request("GET", "/catalogo/categorias", token, null);
        return gson.fromJson(json.getAsJsonArray("categorias"), CATEGORIAS);
    }

    public List<Unidad> unidades(String token, String categoria) throws IOException, InterruptedException {
        JsonObject json = request("GET", "/catalogo/categorias/" + categoria + "/unidades", token, null);
        return gson.fromJson(json.getAsJsonArray("unidades"), UNIDADES);
    }

    public double convertir(String token, String categoria, double valor, String origen, String destino)
            throws IOException, InterruptedException {
        JsonObject body = new JsonObject(); body.addProperty("valor", valor);
        body.addProperty("unidadOrigen", origen); body.addProperty("unidadDestino", destino);
        JsonObject json = request("POST", "/conversiones/" + categoria.toLowerCase(), token, body.toString());
        if (!json.has("resultado")) throw new IOException("El servidor no devolvió un resultado.");
        return json.get("resultado").getAsDouble();
    }

    private JsonObject request(String method, String path, String token, String body)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(base + path))
                .timeout(Duration.ofSeconds(20)).header("Accept", "application/json");
        if (token != null) builder.header("Authorization", "Bearer " + token);
        if (body != null) builder.header("Content-Type", "application/json; charset=UTF-8")
                .method(method, HttpRequest.BodyPublishers.ofString(body));
        else builder.method(method, HttpRequest.BodyPublishers.noBody());
        HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        JsonObject json;
        try { json = gson.fromJson(response.body(), JsonObject.class); }
        catch (RuntimeException ex) { throw new IOException("Respuesta inválida del servidor (HTTP " + response.statusCode() + ")."); }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String message = json != null && json.has("mensaje") ? json.get("mensaje").getAsString()
                    : "Error HTTP " + response.statusCode();
            String code = json != null && json.has("codigo") ? json.get("codigo").getAsString() : null;
            throw new ErrorApi(response.statusCode(), code, message);
        }
        if (json == null || "ERROR".equalsIgnoreCase(json.has("estado") ? json.get("estado").getAsString() : ""))
            throw new IOException(json != null && json.has("mensaje") ? json.get("mensaje").getAsString() : "Respuesta vacía del servidor.");
        return json;
    }

    private static String required(JsonObject json, String field) throws IOException {
        if (json == null || !json.has(field) || json.get(field).isJsonNull()) throw new IOException("La respuesta no incluye " + field + ".");
        return json.get(field).getAsString();
    }
}
