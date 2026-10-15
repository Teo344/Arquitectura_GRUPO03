package ec.edu.monster.controladores.recursos;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.seguridad.Protegido;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioCatalogo;
import ec.edu.monster.servicios.ServicioConversion;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import ec.edu.monster.vista.SolicitudConversionDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Recurso protegido por ManejadorAutenticacion: catálogo (RQF-002, RQF-003)
 * y conversiones (RQF-004 a RQF-006). El token viaja en la cabecera
 * Authorization y lo valida el filtro antes de llegar aquí.
 *
 * @author carlo
 */
@Path("/")
@Protegido
@Produces(MediaType.APPLICATION_JSON)
public class ConversionUnidadesRecurso {

    @Inject
    private ServicioCatalogo servicioCatalogo;

    @Inject
    private ServicioConversion servicioConversion;

    @Inject
    private RespuestaMapeador mapeador;

    @GET
    @Path("catalogo/categorias")
    public RespuestaDto consultarCatalogo() {
        return mapeador.deCatalogo(servicioCatalogo.categorias());
    }

    @GET
    @Path("catalogo/categorias/{categoria}/unidades")
    public RespuestaDto consultarUnidades(@PathParam("categoria") String categoria) throws ExcepcionServicio {
        return mapeador.deUnidades(servicioCatalogo.unidades(categoria));
    }

    @POST
    @Path("conversiones/longitud")
    @Consumes(MediaType.APPLICATION_JSON)
    public RespuestaDto convertirLongitud(SolicitudConversionDto solicitud) throws ExcepcionServicio {
        return convertir(Categoria.LONGITUD, solicitud);
    }

    @POST
    @Path("conversiones/masa")
    @Consumes(MediaType.APPLICATION_JSON)
    public RespuestaDto convertirMasa(SolicitudConversionDto solicitud) throws ExcepcionServicio {
        return convertir(Categoria.MASA, solicitud);
    }

    @POST
    @Path("conversiones/temperatura")
    @Consumes(MediaType.APPLICATION_JSON)
    public RespuestaDto convertirTemperatura(SolicitudConversionDto solicitud) throws ExcepcionServicio {
        return convertir(Categoria.TEMPERATURA, solicitud);
    }

    private RespuestaDto convertir(Categoria categoria, SolicitudConversionDto solicitud) throws ExcepcionServicio {
        SolicitudConversionDto s = solicitud != null ? solicitud : new SolicitudConversionDto();
        return mapeador.deConversion(
                servicioConversion.convertir(categoria, s.valor, s.unidadOrigen, s.unidadDestino));
    }
}
