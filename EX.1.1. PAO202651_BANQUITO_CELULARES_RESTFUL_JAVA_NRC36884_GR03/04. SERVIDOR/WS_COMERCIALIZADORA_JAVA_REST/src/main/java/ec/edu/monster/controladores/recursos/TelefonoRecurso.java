package ec.edu.monster.controladores.recursos;

import ec.edu.monster.modelo.Telefono;
import ec.edu.monster.seguridad.Protegido;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioTelefono;
import ec.edu.monster.vista.Mapeador;
import ec.edu.monster.vista.SolicitudTelefonoDto;
import ec.edu.monster.vista.TelefonoDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

/**
 * Mantenimiento del catálogo: GET lista los teléfonos a la venta, POST crea
 * (201 + Location), PUT reemplaza marca, modelo y precio, y DELETE da de baja
 * (204).
 *
 * @author carlo
 */
@Path("telefonos")
@Protegido
@Produces(MediaType.APPLICATION_JSON)
public class TelefonoRecurso {

    @Inject
    private ServicioTelefono servicioTelefono;

    @Inject
    private Mapeador mapeador;

    @Context
    private UriInfo uriInfo;

    @GET
    public List<TelefonoDto> listar() throws ExcepcionServicio {
        return mapeador.deTelefonos(servicioTelefono.listar());
    }

    @GET
    @Path("{codTelefono: \\d+}")
    public TelefonoDto buscar(@PathParam("codTelefono") int codTelefono) throws ExcepcionServicio {
        return mapeador.deTelefono(servicioTelefono.buscar(codTelefono));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(SolicitudTelefonoDto solicitud) throws ExcepcionServicio {
        SolicitudTelefonoDto s = solicitud != null ? solicitud : new SolicitudTelefonoDto();
        Telefono telefono = servicioTelefono.crear(s.marca, s.modelo, s.precio);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(telefono.codigo())).build())
                .entity(mapeador.deTelefono(telefono))
                .build();
    }

    @PUT
    @Path("{codTelefono: \\d+}")
    @Consumes(MediaType.APPLICATION_JSON)
    public TelefonoDto actualizar(@PathParam("codTelefono") int codTelefono, SolicitudTelefonoDto solicitud)
            throws ExcepcionServicio {
        SolicitudTelefonoDto s = solicitud != null ? solicitud : new SolicitudTelefonoDto();
        return mapeador.deTelefono(servicioTelefono.actualizar(codTelefono, s.marca, s.modelo, s.precio));
    }

    @DELETE
    @Path("{codTelefono: \\d+}")
    public Response eliminar(@PathParam("codTelefono") int codTelefono) throws ExcepcionServicio {
        servicioTelefono.eliminar(codTelefono);
        return Response.noContent().build();
    }
}
