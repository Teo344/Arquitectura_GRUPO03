package ec.edu.monster.controladores.recursos;

import ec.edu.monster.seguridad.ManejadorAutenticacion;
import ec.edu.monster.seguridad.Protegido;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioAutenticacion;
import ec.edu.monster.vista.CredencialesDto;
import ec.edu.monster.vista.Mapeador;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * La sesión como recurso: POST /sesiones la crea (inicio de sesión, público)
 * y DELETE /sesiones/actual elimina la del token enviado (cierre de sesión).
 *
 * @author carlo
 */
@Path("sesiones")
@Produces(MediaType.APPLICATION_JSON)
public class SesionRecurso {

    @Inject
    private ServicioAutenticacion servicioAutenticacion;

    @Inject
    private Mapeador mapeador;

    @Context
    private UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response iniciar(CredencialesDto credenciales) throws ExcepcionServicio {
        CredencialesDto c = credenciales != null ? credenciales : new CredencialesDto();
        return Response.created(uriInfo.getAbsolutePathBuilder().path("actual").build())
                .entity(mapeador.deSesion(servicioAutenticacion.iniciarSesion(c.usuario, c.clave)))
                .build();
    }

    /**
     * 204 No Content. ManejadorAutenticacion ya validó el token.
     */
    @DELETE
    @Path("actual")
    @Protegido
    public Response cerrar(@HeaderParam(HttpHeaders.AUTHORIZATION) String autorizacion) throws ExcepcionServicio {
        servicioAutenticacion.cerrarSesion(ManejadorAutenticacion.leerToken(autorizacion));
        return Response.noContent().build();
    }
}
