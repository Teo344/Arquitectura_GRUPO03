package ec.edu.monster.controladores.recursos;

import ec.edu.monster.seguridad.Protegido;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioCliente;
import ec.edu.monster.vista.CuotaDto;
import ec.edu.monster.vista.Mapeador;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Tabla de amortización de un crédito ya otorgado (servicio 4 del Core) y
 * simulación antes de vender.
 *
 * @author carlo
 */
@Path("creditos")
@Protegido
@Produces(MediaType.APPLICATION_JSON)
public class CreditoRecurso {

    @Inject
    private ServicioCliente servicioCliente;

    @Inject
    private Mapeador mapeador;

    @GET
    @Path("simulacion")
    public List<CuotaDto> simular(@QueryParam("monto") String monto, @QueryParam("cuotas") String cuotas)
            throws ExcepcionServicio {
        return mapeador.deCuotas(servicioCliente.simular(monto, cuotas));
    }

    @GET
    @Path("{codCredito: \\d+}/cuotas")
    public List<CuotaDto> cuotas(@PathParam("codCredito") int codCredito) throws ExcepcionServicio {
        return mapeador.deCuotas(servicioCliente.cuotas(codCredito));
    }
}
