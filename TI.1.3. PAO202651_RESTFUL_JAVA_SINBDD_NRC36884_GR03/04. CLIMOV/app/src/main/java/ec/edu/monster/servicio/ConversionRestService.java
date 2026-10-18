package ec.edu.monster.servicio;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.ResultadoConversion;
import ec.edu.monster.modelo.Unidad;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONObject;

/** Acceso al catálogo y a las conversiones del recurso REST protegido. */
public class ConversionRestService {

    private final ClienteRest cliente;

    public ConversionRestService() {
        this(new ClienteRest());
    }

    public ConversionRestService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    /** Solo las categorías que el servidor puede convertir (longitud, masa y temperatura). */
    public List<Categoria> consultarCategorias(String token) throws ExcepcionServicio {
        List<Categoria> convertibles = new ArrayList<>();
        for (Categoria categoria : consultarCatalogo(token)) {
            if (categoria.isConvertible()) {
                convertibles.add(categoria);
            }
        }
        return convertibles;
    }

    public List<Categoria> consultarCatalogo(String token) throws ExcepcionServicio {
        JSONObject respuesta = cliente.get("/catalogo/categorias", token);

        List<Categoria> categorias = new ArrayList<>();
        JSONArray lista = respuesta.optJSONArray("categorias");
        for (int i = 0; lista != null && i < lista.length(); i++) {
            JSONObject elemento = lista.optJSONObject(i);
            categorias.add(new Categoria(
                    ClienteRest.texto(elemento, "codigo"),
                    ClienteRest.texto(elemento, "nombre"),
                    elemento.optBoolean("convertible")));
        }
        return categorias;
    }

    public List<Unidad> consultarUnidades(String token, String categoria) throws ExcepcionServicio {
        JSONObject respuesta = cliente.get(
                "/catalogo/categorias/" + ClienteRest.codificar(categoria) + "/unidades", token);

        List<Unidad> unidades = new ArrayList<>();
        JSONArray lista = respuesta.optJSONArray("unidades");
        for (int i = 0; lista != null && i < lista.length(); i++) {
            JSONObject elemento = lista.optJSONObject(i);
            unidades.add(new Unidad(
                    ClienteRest.texto(elemento, "codigo"),
                    ClienteRest.texto(elemento, "nombre"),
                    ClienteRest.texto(elemento, "simbolo"),
                    ClienteRest.texto(elemento, "categoria")));
        }
        return unidades;
    }

    public ResultadoConversion convertir(String token, String categoria, double valor,
                                         String codigoOrigen, String codigoDestino) throws ExcepcionServicio {
        String recurso;
        switch (categoria.toUpperCase(Locale.ROOT)) {
            case "LONGITUD":
                recurso = "longitud";
                break;
            case "MASA":
                recurso = "masa";
                break;
            case "TEMPERATURA":
                recurso = "temperatura";
                break;
            default:
                throw new ExcepcionServicio("DATA-001", "Esta categoría no tiene conversión implementada.");
        }

        JSONObject solicitud = ClienteRest.crearJson(
                "valor", valor, "unidadOrigen", codigoOrigen, "unidadDestino", codigoDestino);
        JSONObject respuesta = cliente.post("/conversiones/" + recurso, token, solicitud);

        if (respuesta.isNull("resultado")) {
            throw new ExcepcionServicio("RESPUESTA", "El servidor no devolvió un resultado.");
        }
        return new ResultadoConversion(respuesta.optDouble("resultado"),
                ClienteRest.texto(respuesta, "unidadOrigen"),
                ClienteRest.texto(respuesta, "unidadDestino"));
    }
}
