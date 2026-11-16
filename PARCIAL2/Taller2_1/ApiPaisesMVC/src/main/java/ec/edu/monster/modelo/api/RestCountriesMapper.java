package ec.edu.monster.modelo.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import ec.edu.monster.modelo.entidad.Pais;
import ec.edu.monster.modelo.entidad.Region;
import ec.edu.monster.modelo.excepcion.ExcepcionApiExterna;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Convierte el JSON de REST Countries v5 en objetos {@link Pais}. Es la única
 * clase que conoce la estructura de la respuesta; en el enunciado este trabajo
 * lo hacía el controlador.
 * <p>
 * Estructura esperada (solo los campos que se piden):
 * <pre>
 * { "data": {
 *     "objects": [ { "names": {"common": "Ecuador"}, "codes": {"alpha_3": "ECU"},
 *                    "capitals": [ {"name": "Quito", "attributes": {"primary": true}} ],
 *                    "region": "Americas", "population": 18000000,
 *                    "currencies": [ {"code": "USD", "name": "United States dollar"} ] } ],
 *     "meta": { "total": 249, "more": true } } }
 * </pre>
 * Los países sin nombre, código ISO o región conocida se descartan (y se
 * registran en el log); los que no tienen capital o moneda se aceptan con ese
 * dato vacío.
 *
 * @author carlo
 */
public final class RestCountriesMapper {

    private static final Logger LOG = Logger.getLogger(RestCountriesMapper.class.getName());
    private static final Pattern CODIGO_ISO = Pattern.compile("[A-Za-z]{3}");

    /**
     * Una página de resultados.
     *
     * @param paises    países válidos de la página
     * @param recibidos objetos que venían en la página, válidos o no (sirve
     *                  para avanzar el offset de la paginación)
     * @param hayMas    la API indica que existen más páginas
     * @param esDemo    la respuesta proviene de la clave demo (datos de muestra)
     */
    public record Pagina(List<Pais> paises, int recibidos, boolean hayMas, boolean esDemo) {

        public Pagina {
            paises = List.copyOf(paises);
        }
    }

    /**
     * @throws ExcepcionApiExterna si el JSON no es válido o no tiene la forma esperada
     */
    public Pagina leerPagina(String json) {
        JsonObject data = leerData(json);
        JsonArray objetos = arreglo(data, "objects");
        if (objetos == null) {
            throw new ExcepcionApiExterna("La respuesta de la API no contiene la lista de países.");
        }
        List<Pais> paises = new ArrayList<>();
        for (JsonElement elemento : objetos) {
            if (elemento.isJsonObject()) {
                aPais(elemento.getAsJsonObject()).ifPresent(paises::add);
            }
        }
        boolean hayMas = booleano(objeto(data, "meta"), "more");
        return new Pagina(paises, objetos.size(), hayMas, data.has("_demo"));
    }

    private static JsonObject leerData(String json) {
        JsonElement raiz;
        try {
            raiz = JsonParser.parseString(json == null ? "" : json);
        } catch (JsonParseException e) {
            throw new ExcepcionApiExterna("La API devolvió un JSON no válido.", e);
        }
        JsonObject data = raiz.isJsonObject() ? objeto(raiz.getAsJsonObject(), "data") : null;
        if (data == null) {
            throw new ExcepcionApiExterna("La API devolvió una respuesta con un formato inesperado.");
        }
        return data;
    }

    private static Optional<Pais> aPais(JsonObject json) {
        String nombre = texto(objeto(json, "names"), "common");
        String codigo = texto(objeto(json, "codes"), "alpha_3");
        String valorRegion = texto(json, "region");
        Optional<Region> region = Region.desdeApi(valorRegion);

        if (nombre == null || codigo == null || !CODIGO_ISO.matcher(codigo).matches() || region.isEmpty()) {
            LOG.log(Level.WARNING, "Se descarta un país incompleto de la API: nombre={0}, código={1}, región={2}",
                    new Object[]{nombre, codigo, valorRegion});
            return Optional.empty();
        }
        return Optional.of(Pais.nuevo(codigo, nombre, capitalPrincipal(json.get("capitals")),
                region.get(), poblacion(json.get("population")), monedaPrincipal(json.get("currencies"))));
    }

    /**
     * La capital marcada como principal; si ninguna lo está, la primera.
     * Acepta también una lista de textos simples.
     */
    private static String capitalPrincipal(JsonElement capitales) {
        if (capitales == null || !capitales.isJsonArray()) {
            return null;
        }
        String primera = null;
        for (JsonElement elemento : capitales.getAsJsonArray()) {
            String nombre;
            boolean principal = false;
            if (elemento.isJsonObject()) {
                JsonObject capital = elemento.getAsJsonObject();
                nombre = texto(capital, "name");
                principal = booleano(objeto(capital, "attributes"), "primary");
            } else {
                nombre = comoTexto(elemento);
            }
            if (nombre != null && principal) {
                return nombre;
            }
            if (primera == null) {
                primera = nombre;
            }
        }
        return primera;
    }

    /**
     * Nombre de la primera moneda. La v5 envía una lista de objetos; también
     * se acepta un mapa código → objeto (forma de versiones anteriores).
     */
    private static String monedaPrincipal(JsonElement monedas) {
        JsonElement primera = null;
        if (monedas != null && monedas.isJsonArray() && !monedas.getAsJsonArray().isEmpty()) {
            primera = monedas.getAsJsonArray().get(0);
        } else if (monedas != null && monedas.isJsonObject()) {
            primera = monedas.getAsJsonObject().entrySet().stream()
                    .map(Map.Entry::getValue).findFirst().orElse(null);
        }
        if (primera == null) {
            return null;
        }
        return primera.isJsonObject() ? texto(primera.getAsJsonObject(), "name") : comoTexto(primera);
    }

    private static long poblacion(JsonElement valor) {
        if (valor instanceof JsonPrimitive primitivo && primitivo.isNumber()) {
            return Math.max(0, primitivo.getAsBigDecimal().longValue());
        }
        return 0;
    }

    // ---------------------------------------------- lectura segura del árbol

    private static JsonObject objeto(JsonObject padre, String campo) {
        if (padre == null) {
            return null;
        }
        JsonElement valor = padre.get(campo);
        return valor != null && valor.isJsonObject() ? valor.getAsJsonObject() : null;
    }

    private static JsonArray arreglo(JsonObject padre, String campo) {
        JsonElement valor = padre.get(campo);
        return valor != null && valor.isJsonArray() ? valor.getAsJsonArray() : null;
    }

    private static String texto(JsonObject padre, String campo) {
        return padre == null ? null : comoTexto(padre.get(campo));
    }

    private static String comoTexto(JsonElement valor) {
        if (valor instanceof JsonPrimitive primitivo && primitivo.isString()) {
            String texto = primitivo.getAsString().strip();
            return texto.isEmpty() ? null : texto;
        }
        return null;
    }

    private static boolean booleano(JsonObject padre, String campo) {
        if (padre == null) {
            return false;
        }
        JsonElement valor = padre.get(campo);
        return valor instanceof JsonPrimitive primitivo && primitivo.isBoolean() && primitivo.getAsBoolean();
    }
}
