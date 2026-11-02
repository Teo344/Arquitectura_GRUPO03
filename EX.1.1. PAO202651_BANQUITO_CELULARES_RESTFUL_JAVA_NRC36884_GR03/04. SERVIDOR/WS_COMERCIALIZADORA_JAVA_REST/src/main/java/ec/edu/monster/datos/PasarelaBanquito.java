package ec.edu.monster.datos;

import ec.edu.monster.modelo.ClienteBanco;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.CreditoBanco;
import ec.edu.monster.modelo.CuotaBanco;
import ec.edu.monster.modelo.EvaluacionBanco;
import ec.edu.monster.modelo.MontoMaximoBanco;
import ec.edu.monster.servicios.ExcepcionServicio;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
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
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Acceso a los datos del banco a través de los servicios REST del Core: es el
 * "DAO" remoto de la comercializadora, que no ve la base de datos banquito.
 * Envía la clave de API en X-Api-Key (core.url y core.claveApi en
 * db.properties) y traduce las respuestas:
 * <ul>
 * <li>2xx: el objeto leído con JSON-B</li>
 * <li>errores de negocio del Core (CLI, CRE, DATA): la misma
 * ExcepcionServicio, con el detalle que dio el Core</li>
 * <li>sin respuesta, tiempo agotado, 5xx o clave rechazada: EXT-001 (502), y
 * la causa queda en el log</li>
 * </ul>
 *
 * @author carlo
 */
@ApplicationScoped
public class PasarelaBanquito {

    private static final Logger LOG = Logger.getLogger(PasarelaBanquito.class.getName());
    private static final String ARCHIVO = "/db.properties";
    private static final Duration TIEMPO_CONEXION = Duration.ofSeconds(5);
    private static final Duration TIEMPO_RESPUESTA = Duration.ofSeconds(10);

    /**
     * Cuerpo application/problem+json de los errores del Core.
     */
    public static class ProblemaCore {

        public int status;
        public String detail;
        public String codigo;
    }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIEMPO_CONEXION).build();
    private Jsonb jsonb;
    private String url;
    private String clave;

    @PostConstruct
    void configurar() {
        jsonb = JsonbBuilder.create();
        Properties propiedades = new Properties();
        try (InputStream entrada = PasarelaBanquito.class.getResourceAsStream(ARCHIVO)) {
            if (entrada != null) {
                propiedades.load(entrada);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo leer " + ARCHIVO, e);
        }
        String base = propiedades.getProperty("core.url", "").trim();
        url = base.isEmpty() ? null : (base.endsWith("/") ? base : base + "/");
        String valor = propiedades.getProperty("core.claveApi", "").trim();
        clave = valor.isEmpty() ? null : valor;
    }

    @PreDestroy
    void cerrar() {
        try {
            jsonb.close();
        } catch (Exception e) {
            LOG.log(Level.FINE, "No se pudo cerrar JSON-B", e);
        }
    }

    public ClienteBanco buscarCliente(String cedula) throws ExcepcionServicio {
        return enviar(get("clientes?cedula=" + codificar(cedula)), ClienteBanco.class);
    }

    /**
     * Servicio 1 del Core.
     */
    public EvaluacionBanco evaluar(String cedula) throws ExcepcionServicio {
        return enviar(get("clientes/sujeto-credito?cedula=" + codificar(cedula)), EvaluacionBanco.class);
    }

    /**
     * Servicio 2 del Core.
     */
    public MontoMaximoBanco montoMaximo(int codCliente) throws ExcepcionServicio {
        return enviar(get("clientes/" + codCliente + "/monto-maximo"), MontoMaximoBanco.class);
    }

    /**
     * Servicio 3 del Core: el crédito vuelve con su tabla de amortización.
     */
    public CreditoBanco otorgarCredito(int codCliente, BigDecimal monto, int numCuotas) throws ExcepcionServicio {
        String cuerpo = "{\"codCliente\":" + codCliente + ",\"monto\":" + monto.toPlainString()
                + ",\"numCuotas\":" + numCuotas + "}";
        return enviar(peticion("creditos").header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo, StandardCharsets.UTF_8)), CreditoBanco.class);
    }

    public List<CreditoBanco> creditos(int codCliente) throws ExcepcionServicio {
        return List.of(enviar(get("clientes/" + codCliente + "/creditos"), CreditoBanco[].class));
    }

    /**
     * Servicio 4 del Core.
     */
    public List<CuotaBanco> cuotas(int codCredito) throws ExcepcionServicio {
        return List.of(enviar(get("creditos/" + codCredito + "/cuotas"), CuotaBanco[].class));
    }

    public List<CuotaBanco> simular(String monto, String numCuotas) throws ExcepcionServicio {
        return List.of(enviar(get("creditos/simulacion?monto=" + codificar(monto) + "&cuotas="
                + codificar(numCuotas)), CuotaBanco[].class));
    }

    private HttpRequest.Builder get(String ruta) throws ExcepcionServicio {
        return peticion(ruta).GET();
    }

    private HttpRequest.Builder peticion(String ruta) throws ExcepcionServicio {
        if (url == null || clave == null) {
            LOG.severe("core.url o core.claveApi no están configurados en " + ARCHIVO);
            throw new ExcepcionServicio(CodigoError.EXT_001);
        }
        return HttpRequest.newBuilder(URI.create(url + ruta))
                .timeout(TIEMPO_RESPUESTA)
                .header("X-Api-Key", clave)
                .header("Accept", "application/json");
    }

    private <T> T enviar(HttpRequest.Builder peticion, Class<T> tipo) throws ExcepcionServicio {
        HttpRequest solicitud = peticion.build();
        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(solicitud, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOG.log(Level.WARNING, "El Core no respondió a {0}: {1}", new Object[]{solicitud.uri(), e.toString()});
            throw new ExcepcionServicio(CodigoError.EXT_001, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExcepcionServicio(CodigoError.EXT_001, e);
        }
        try {
            if (respuesta.statusCode() / 100 == 2) {
                return jsonb.fromJson(respuesta.body(), tipo);
            }
            throw error(solicitud, respuesta);
        } catch (JsonbException e) {
            LOG.log(Level.SEVERE, "Respuesta del Core no reconocida en " + solicitud.uri(), e);
            throw new ExcepcionServicio(CodigoError.EXT_001, e);
        }
    }

    private ExcepcionServicio error(HttpRequest solicitud, HttpResponse<String> respuesta) {
        ProblemaCore problema = jsonb.fromJson(respuesta.body(), ProblemaCore.class);
        CodigoError codigo = problema == null ? null : CodigoError.delCore(problema.codigo);
        if (codigo != null) {
            return new ExcepcionServicio(codigo, problema.detail != null ? problema.detail : codigo.getMensaje());
        }
        LOG.log(Level.SEVERE, "El Core respondió {0} {1} a {2}", new Object[]{respuesta.statusCode(),
            problema == null ? "" : problema.codigo, solicitud.uri()});
        return new ExcepcionServicio(CodigoError.EXT_001);
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
    }
}
