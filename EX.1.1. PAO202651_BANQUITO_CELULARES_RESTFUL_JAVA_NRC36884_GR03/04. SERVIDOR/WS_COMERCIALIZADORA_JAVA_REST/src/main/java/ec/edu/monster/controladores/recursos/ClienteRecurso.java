package ec.edu.monster.controladores.recursos;

import ec.edu.monster.seguridad.Protegido;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioCliente;
import ec.edu.monster.vista.CreditoDto;
import ec.edu.monster.vista.EvaluacionClienteDto;
import ec.edu.monster.vista.Mapeador;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

/**
 * Consultas de un comprador en el banco, por su cédula: si es sujeto de
 * crédito (con su monto máximo) y sus créditos.
 *
 * @author carlo
 */
@Path("clientes")
@Protegido
@Produces(MediaType.APPLICATION_JSON)
public class ClienteRecurso {

    @Inject
    private ServicioCliente servicioCliente;

    @Inject
    private Mapeador mapeador;

    @GET
    @Path("sujeto-credito")
    public EvaluacionClienteDto evaluar(@QueryParam("cedula") String cedula) throws ExcepcionServicio {
        return mapeador.deEvaluacion(servicioCliente.evaluar(cedula));
    }

    @GET
    @Path("creditos")
    public List<CreditoDto> creditos(@QueryParam("cedula") String cedula) throws ExcepcionServicio {
        return mapeador.deCreditos(servicioCliente.creditos(cedula));
    }
}
