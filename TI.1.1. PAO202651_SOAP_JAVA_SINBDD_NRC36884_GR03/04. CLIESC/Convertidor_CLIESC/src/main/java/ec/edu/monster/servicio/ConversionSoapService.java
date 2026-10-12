package ec.edu.monster.servicio;

import ec.edu.monster.servicio.Categoria;
import ec.edu.monster.servicio.ConsultarCatalogo;
import ec.edu.monster.servicio.ConsultarUnidades;
import ec.edu.monster.servicio.Respuesta;
import ec.edu.monster.servicio.SolicitudConversion;
import ec.edu.monster.servicio.Unidad;
import java.util.List;

/** Acceso al catálogo y operaciones de conversión del backend SOAP. */
public class ConversionSoapService {
    private final String endpoint;

    public ConversionSoapService() {
        String baseUrl = System.getProperty("monster.soap.baseUrl", "http://localhost:8080/WS_CON_UNI_JAVA_SOAP");
        endpoint = baseUrl.replaceAll("/$", "") + "/ConversionUnidadesWS";
    }

    public List<Categoria> consultarCategorias(String token) throws Exception {
        return consultarCatalogo(token).stream()
                .filter(c -> Boolean.TRUE.equals(c.isConvertible()))
                .toList();
    }

    public List<Categoria> consultarCatalogo(String token) throws Exception {
        Respuesta respuesta = crearPuerto().consultarCatalogo(new ConsultarCatalogo(), token).getReturn();
        validar(respuesta);
        if (respuesta.getCategorias() == null) return List.of();
        return respuesta.getCategorias().getCategoria();
    }

    public List<Unidad> consultarUnidades(String token, String categoria) throws Exception {
        ConsultarUnidades consulta = new ConsultarUnidades();
        consulta.setCategoria(categoria);
        Respuesta respuesta = crearPuerto().consultarUnidades(consulta, token).getReturn();
        validar(respuesta);
        return respuesta.getUnidades() == null ? List.of() : respuesta.getUnidades().getUnidad();
    }

    public Respuesta convertir(String token, String categoria, double valor,
                               String codigoOrigen, String codigoDestino) throws Exception {
        SolicitudConversion solicitud = new SolicitudConversion();
        solicitud.setValor(valor);
        solicitud.setUnidadOrigen(codigoOrigen);
        solicitud.setUnidadDestino(codigoDestino);

        ConversionUnidadesWS puerto = crearPuerto();
        Respuesta respuesta;
        switch (categoria.toUpperCase(java.util.Locale.ROOT)) {
            case "LONGITUD" -> {
                ConvertirLongitud peticion = new ConvertirLongitud();
                peticion.setSolicitud(solicitud);
                respuesta = puerto.convertirLongitud(peticion, token).getReturn();
            }
            case "MASA" -> {
                ConvertirMasa peticion = new ConvertirMasa();
                peticion.setSolicitud(solicitud);
                respuesta = puerto.convertirMasa(peticion, token).getReturn();
            }
            case "TEMPERATURA" -> {
                ConvertirTemperatura peticion = new ConvertirTemperatura();
                peticion.setSolicitud(solicitud);
                respuesta = puerto.convertirTemperatura(peticion, token).getReturn();
            }
            default -> throw new IllegalArgumentException("Esta categoría no tiene conversión implementada.");
        }
        validar(respuesta);
        return respuesta;
    }

    private ConversionUnidadesWS crearPuerto() {
        ConversionUnidadesWS_Service service = new ConversionUnidadesWS_Service();
        ConversionUnidadesWS puerto = service.getConversionUnidadesWSPort();
        jakarta.xml.ws.BindingProvider binding = (jakarta.xml.ws.BindingProvider) puerto;
        binding.getRequestContext().put(jakarta.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
        return puerto;
    }

    private void validar(Respuesta respuesta) {
        if (respuesta == null) throw new IllegalStateException("El servidor devolvió una respuesta vacía.");
        if (!"OK".equalsIgnoreCase(respuesta.getEstado())) {
            throw new IllegalStateException(respuesta.getMensaje() == null
                    ? "El servidor no pudo completar la operación." : respuesta.getMensaje());
        }
    }
}
