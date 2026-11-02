package ec.edu.monster.controladores.recursos;

import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioCredito;
import ec.edu.monster.vista.ClienteDto;
import ec.edu.monster.vista.CreditoDto;
import ec.edu.monster.vista.EvaluacionCreditoDto;
import ec.edu.monster.vista.Mapeador;
import ec.edu.monster.vista.MontoMaximoDto;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Consultas sobre los clientes del banco: búsqueda por cédula, servicio 1
 * (sujeto de crédito), servicio 2 (monto máximo) y sus créditos. La cédula
 * va como parámetro de consulta porque es un criterio de búsqueda, no la
 * clave del recurso (que es codCliente).
 *
 * @author carlo
 */
@Path("clientes")
@Produces(MediaType.APPLICATION_JSON)
public class ClienteRecurso {

    @Inject
    private ServicioCredito servicioCredito;

    @Inject
    private Mapeador mapeador;

    @GET
    public ClienteDto buscar(@QueryParam("cedula") String cedula) throws ExcepcionServicio {
        return mapeador.deCliente(servicioCredito.buscarCliente(cedula));
    }

    @GET
    @Path("sujeto-credito")
    public EvaluacionCreditoDto evaluar(@QueryParam("cedula") String cedula) throws ExcepcionServicio {
        return mapeador.deEvaluacion(servicioCredito.evaluar(cedula));
    }

    @GET
    @Path("{codCliente: \\d+}/monto-maximo")
    public MontoMaximoDto montoMaximo(@PathParam("codCliente") int codCliente) throws ExcepcionServicio {
        return mapeador.deMontoMaximo(servicioCredito.montoMaximo(codCliente));
    }

    @GET
    @Path("{codCliente: \\d+}/creditos")
    public List<CreditoDto> creditos(@PathParam("codCliente") int codCliente) throws ExcepcionServicio {
        return mapeador.deCreditos(servicioCredito.creditosDeCliente(codCliente));
    }
}
