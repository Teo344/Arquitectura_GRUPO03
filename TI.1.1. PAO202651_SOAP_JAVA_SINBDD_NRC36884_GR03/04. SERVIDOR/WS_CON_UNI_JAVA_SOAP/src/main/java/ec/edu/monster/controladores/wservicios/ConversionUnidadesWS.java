package ec.edu.monster.controladores.wservicios;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioCatalogo;
import ec.edu.monster.servicios.ServicioConversion;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import ec.edu.monster.vista.SolicitudConversionDto;
import jakarta.inject.Inject;
import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Web Service protegido por ManejadorAutenticacion: catálogo (RQF-002,
 * RQF-003) y conversiones (RQF-004 a RQF-006). El parámetro token solo sirve
 * para publicar la cabecera en el WSDL; lo valida el handler.
 *
 * @author carlo
 */
@WebService(serviceName = "ConversionUnidadesWS")
@HandlerChain(file = "ConversionUnidadesWS_handler.xml")
public class ConversionUnidadesWS {

    @Inject
    private ServicioCatalogo servicioCatalogo;

    @Inject
    private ServicioConversion servicioConversion;

    @Inject
    private RespuestaMapeador mapeador;

    @WebMethod(operationName = "consultarCatalogo")
    public RespuestaDto consultarCatalogo(
            @WebParam(name = "token", header = true) String token) {
        return mapeador.deCatalogo(servicioCatalogo.categorias());
    }

    @WebMethod(operationName = "consultarUnidades")
    public RespuestaDto consultarUnidades(
            @WebParam(name = "token", header = true) String token,
            @WebParam(name = "categoria") String categoria) throws ExcepcionServicio {
        return mapeador.deUnidades(servicioCatalogo.unidades(categoria));
    }

    @WebMethod(operationName = "convertirLongitud")
    public RespuestaDto convertirLongitud(
            @WebParam(name = "token", header = true) String token,
            @WebParam(name = "solicitud") SolicitudConversionDto solicitud) throws ExcepcionServicio {
        return convertir(Categoria.LONGITUD, solicitud);
    }

    @WebMethod(operationName = "convertirMasa")
    public RespuestaDto convertirMasa(
            @WebParam(name = "token", header = true) String token,
            @WebParam(name = "solicitud") SolicitudConversionDto solicitud) throws ExcepcionServicio {
        return convertir(Categoria.MASA, solicitud);
    }

    @WebMethod(operationName = "convertirTemperatura")
    public RespuestaDto convertirTemperatura(
            @WebParam(name = "token", header = true) String token,
            @WebParam(name = "solicitud") SolicitudConversionDto solicitud) throws ExcepcionServicio {
        return convertir(Categoria.TEMPERATURA, solicitud);
    }

    private RespuestaDto convertir(Categoria categoria, SolicitudConversionDto solicitud) throws ExcepcionServicio {
        SolicitudConversionDto s = solicitud != null ? solicitud : new SolicitudConversionDto();
        return mapeador.deConversion(
                servicioConversion.convertir(categoria, s.valor, s.unidadOrigen, s.unidadDestino));
    }
}
