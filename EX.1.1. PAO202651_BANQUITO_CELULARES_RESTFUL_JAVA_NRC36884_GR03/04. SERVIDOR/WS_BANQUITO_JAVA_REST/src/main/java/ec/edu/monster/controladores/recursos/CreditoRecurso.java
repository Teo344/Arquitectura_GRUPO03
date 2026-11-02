package ec.edu.monster.controladores.recursos;

import ec.edu.monster.modelo.Credito;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioCredito;
import ec.edu.monster.vista.CreditoDto;
import ec.edu.monster.vista.CuotaDto;
import ec.edu.monster.vista.Mapeador;
import ec.edu.monster.vista.SolicitudCreditoDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

/**
 * Créditos: servicio 3 (otorgar, POST) y servicio 4 (tabla de amortización),
 * más la consulta del crédito y la simulación sin guardar.
 *
 * @author carlo
 */
@Path("creditos")
@Produces(MediaType.APPLICATION_JSON)
public class CreditoRecurso {

    @Inject
    private ServicioCredito servicioCredito;

    @Inject
    private Mapeador mapeador;

    @Context
    private UriInfo uriInfo;

    /**
     * 201 Created con la cabecera Location del crédito nuevo y el crédito con
     * su tabla en el cuerpo.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response otorgar(SolicitudCreditoDto solicitud) throws ExcepcionServicio {
        SolicitudCreditoDto s = solicitud != null ? solicitud : new SolicitudCreditoDto();
        Credito credito = servicioCredito.otorgar(s.codCliente, s.monto, s.numCuotas);
        URI ubicacion = uriInfo.getAbsolutePathBuilder().path(String.valueOf(credito.codigo())).build();
        return Response.created(ubicacion).entity(mapeador.deCredito(credito, true)).build();
    }

    @GET
    @Path("simulacion")
    public List<CuotaDto> simular(@QueryParam("monto") String monto, @QueryParam("cuotas") String cuotas)
            throws ExcepcionServicio {
        return mapeador.deCuotas(servicioCredito.simular(monto, cuotas));
    }

    @GET
    @Path("{codCredito: \\d+}")
    public CreditoDto consultar(@PathParam("codCredito") int codCredito) throws ExcepcionServicio {
        return mapeador.deCredito(servicioCredito.consultarCredito(codCredito), true);
    }

    @GET
    @Path("{codCredito: \\d+}/cuotas")
    public List<CuotaDto> tabla(@PathParam("codCredito") int codCredito) throws ExcepcionServicio {
        return mapeador.deCuotas(servicioCredito.tabla(codCredito));
    }
}
