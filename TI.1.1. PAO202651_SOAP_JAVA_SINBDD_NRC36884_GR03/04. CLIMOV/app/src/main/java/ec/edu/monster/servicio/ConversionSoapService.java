package ec.edu.monster.servicio;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.ResultadoConversion;
import ec.edu.monster.modelo.Unidad;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.w3c.dom.Element;

/** Acceso al catálogo y a las conversiones del servicio SOAP protegido (ConversionUnidadesWS). */
public class ConversionSoapService {

    private static final String SERVICIO = "ConversionUnidadesWS";

    private final ClienteSoap cliente;

    public ConversionSoapService() {
        this(new ClienteSoap());
    }

    public ConversionSoapService(ClienteSoap cliente) {
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
        Element respuesta = cliente.invocar(SERVICIO, "consultarCatalogo", token, null);
        ClienteSoap.validar(respuesta);

        List<Categoria> categorias = new ArrayList<>();
        for (Element elemento : ClienteSoap.hijos(ClienteSoap.hijo(respuesta, "categorias"), "categoria")) {
            categorias.add(new Categoria(
                    ClienteSoap.texto(elemento, "codigo"),
                    ClienteSoap.texto(elemento, "nombre"),
                    Boolean.parseBoolean(ClienteSoap.texto(elemento, "convertible"))));
        }
        return categorias;
    }

    public List<Unidad> consultarUnidades(String token, String categoria) throws ExcepcionServicio {
        String parametros = "<categoria>" + ClienteSoap.escapar(categoria) + "</categoria>";
        Element respuesta = cliente.invocar(SERVICIO, "consultarUnidades", token, parametros);
        ClienteSoap.validar(respuesta);

        List<Unidad> unidades = new ArrayList<>();
        for (Element elemento : ClienteSoap.hijos(ClienteSoap.hijo(respuesta, "unidades"), "unidad")) {
            unidades.add(new Unidad(
                    ClienteSoap.texto(elemento, "codigo"),
                    ClienteSoap.texto(elemento, "nombre"),
                    ClienteSoap.texto(elemento, "simbolo"),
                    ClienteSoap.texto(elemento, "categoria")));
        }
        return unidades;
    }

    public ResultadoConversion convertir(String token, String categoria, double valor,
                                         String codigoOrigen, String codigoDestino) throws ExcepcionServicio {
        String operacion;
        switch (categoria.toUpperCase(Locale.ROOT)) {
            case "LONGITUD":
                operacion = "convertirLongitud";
                break;
            case "MASA":
                operacion = "convertirMasa";
                break;
            case "TEMPERATURA":
                operacion = "convertirTemperatura";
                break;
            default:
                throw new ExcepcionServicio("DATA-001", "Esta categoría no tiene conversión implementada.");
        }

        String parametros = "<solicitud>"
                + "<valor>" + valor + "</valor>"
                + "<unidadOrigen>" + ClienteSoap.escapar(codigoOrigen) + "</unidadOrigen>"
                + "<unidadDestino>" + ClienteSoap.escapar(codigoDestino) + "</unidadDestino>"
                + "</solicitud>";

        Element respuesta = cliente.invocar(SERVICIO, operacion, token, parametros);
        ClienteSoap.validar(respuesta);

        String resultado = ClienteSoap.texto(respuesta, "resultado");
        if (resultado == null) {
            throw new ExcepcionServicio("RESPUESTA", "El servidor no devolvió un resultado.");
        }
        return new ResultadoConversion(Double.parseDouble(resultado),
                ClienteSoap.texto(respuesta, "unidadOrigen"),
                ClienteSoap.texto(respuesta, "unidadDestino"));
    }
}
