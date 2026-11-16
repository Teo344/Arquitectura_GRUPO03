package ec.edu.monster.modelo.api;

import ec.edu.monster.modelo.api.RestCountriesMapper.Pagina;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.excepcion.ExcepcionApiExterna;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cliente de REST Countries v5 (https://api.restcountries.com/countries/v5).
 * <p>
 * Cambios respecto al enunciado, que usaba la v3.1 (ya retirada) con
 * HttpURLConnection:
 * <ul>
 * <li>{@link HttpClient} con tiempo máximo de conexión y de respuesta: una API
 * lenta no congela la aplicación.</li>
 * <li>Se revisa el código HTTP y cada error se traduce a un mensaje claro, en
 * lugar de imprimir la traza y devolver una cadena vacía.</li>
 * <li>La v5 exige API key (cabecera Authorization) y pagina de a 100 países en
 * el plan gratuito, por eso {@link #obtenerTodos()} recorre las páginas.</li>
 * <li>Solo se piden los campos que se usan ({@code response_fields}).</li>
 * </ul>
 *
 * @author carlo
 */
public final class RestCountriesCliente implements PaisApiCliente {

    private static final Logger LOG = Logger.getLogger(RestCountriesCliente.class.getName());

    static final String CAMPOS = "names.common,codes.alpha_3,capitals,region,population,currencies";
    /** Máximo permitido por el plan gratuito. */
    static final int TAMANO_PAGINA = 100;
    /** Tope de seguridad (1000 países) por si la API indicara "hay más" sin fin. */
    private static final int MAX_PAGINAS = 10;
    private static final int MAX_LOG_CUERPO = 300;
    /** Clave pública de REST Countries: no consume cuota, pero devuelve un solo país de muestra. */
    static final String CLAVE_DEMO = "rc_live_demo";

    private final HttpClient http;
    private final String urlBase;
    private final String clave;
    private final Duration timeout;
    private final RestCountriesMapper mapper;

    public RestCountriesCliente(String urlBase, String clave, Duration timeout, RestCountriesMapper mapper) {
        Objects.requireNonNull(urlBase, "urlBase");
        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("Falta la API key de REST Countries (api.clave)");
        }
        this.urlBase = urlBase.strip().replaceAll("/+$", "");
        this.clave = clave.strip();
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.http = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public List<Pais> obtenerTodos() {
        List<Pais> todos = new ArrayList<>();
        int offset = 0;
        for (int numero = 0; numero < MAX_PAGINAS; numero++) {
            Pagina pagina = mapper.leerPagina(consultar(
                    urlBase + "?limit=" + TAMANO_PAGINA + "&offset=" + offset + "&response_fields=" + CAMPOS));
            avisarSiEsDemo(pagina);
            todos.addAll(pagina.paises());
            if (!pagina.hayMas() || pagina.recibidos() == 0) {
                return todos;
            }
            offset += pagina.recibidos();
        }
        LOG.log(Level.WARNING, "Se alcanzó el tope de {0} páginas; la importación puede estar incompleta", MAX_PAGINAS);
        return todos;
    }

    @Override
    public List<Pais> buscarPorNombre(String nombre) {
        // Una consulta vacía responde 400, y muchos 4xx pueden suspender la cuenta gratuita
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre a buscar no puede estar vacío");
        }
        String consulta = URLEncoder.encode(nombre.strip(), StandardCharsets.UTF_8);
        // Agregado "name": busca en el nombre común, oficial, alternativos y nativos
        Pagina pagina = mapper.leerPagina(consultar(
                urlBase + "/name?q=" + consulta + "&limit=" + TAMANO_PAGINA + "&response_fields=" + CAMPOS));
        avisarSiEsDemo(pagina);
        return pagina.paises();
    }

    @Override
    public boolean esModoDemostracion() {
        return CLAVE_DEMO.equals(clave);
    }

    private String consultar(String url) {
        HttpRequest solicitud = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("Authorization", "Bearer " + clave)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(solicitud, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            throw new ExcepcionApiExterna("La API REST Countries no respondió a tiempo.", e);
        } catch (IOException e) {
            throw new ExcepcionApiExterna(
                    "No se pudo conectar con la API REST Countries. Verifique la conexión a Internet.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExcepcionApiExterna("La consulta a la API REST Countries fue interrumpida.", e);
        }

        int estado = respuesta.statusCode();
        if (estado == 200) {
            return respuesta.body();
        }
        LOG.log(Level.WARNING, "REST Countries respondió {0}: {1}", new Object[]{estado, recortar(respuesta.body())});
        throw new ExcepcionApiExterna(mensajePorEstado(estado));
    }

    /**
     * Mensaje para el usuario según los códigos que documenta la API.
     */
    static String mensajePorEstado(int estado) {
        return switch (estado) {
            case 400 -> "La API REST Countries rechazó la consulta por datos no válidos.";
            case 401 -> "La API key de REST Countries falta o no es válida (revise api.clave en config.properties).";
            case 403 -> "La cuenta de REST Countries agotó su cuota mensual o no tiene acceso a ese dato.";
            case 404 -> "La API REST Countries no reconoce la ruta consultada.";
            case 410 -> "La versión configurada de la API REST Countries ya no está activa.";
            case 429 -> "Demasiadas consultas seguidas a la API. Espere unos segundos e intente de nuevo.";
            default -> estado >= 500
                    ? "La API REST Countries no está disponible en este momento (error " + estado + ")."
                    : "La API REST Countries respondió con un error inesperado (" + estado + ").";
        };
    }

    private static void avisarSiEsDemo(Pagina pagina) {
        if (pagina.esDemo()) {
            LOG.warning("Se está usando la clave demo de REST Countries: la API solo devuelve un país de muestra. "
                    + "Configure una API key real en api.clave para obtener todos los datos.");
        }
    }

    private static String recortar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= MAX_LOG_CUERPO ? texto : texto.substring(0, MAX_LOG_CUERPO) + "...";
    }
}
