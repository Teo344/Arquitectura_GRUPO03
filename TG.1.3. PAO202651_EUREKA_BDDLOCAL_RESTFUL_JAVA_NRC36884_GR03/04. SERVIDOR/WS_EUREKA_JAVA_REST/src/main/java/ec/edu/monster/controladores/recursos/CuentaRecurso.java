package ec.edu.monster.controladores.recursos;

import ec.edu.monster.seguridad.Protegido;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioCuenta;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Recurso protegido por ManejadorAutenticacion: consulta de cuenta (RQF-003)
 * y de movimientos (RQF-004). El token viaja en la cabecera Authorization y lo
 * valida el filtro antes de llegar aquí.
 *
 * @author carlo
 */
@Path("cuentas")
@Protegido
@Produces(MediaType.APPLICATION_JSON)
public class CuentaRecurso {

    @Inject
    private ServicioCuenta servicioCuenta;

    @Inject
    private RespuestaMapeador mapeador;

    @GET
    @Path("{cuenta}")
    public RespuestaDto consultarCuenta(@PathParam("cuenta") String cuenta) throws ExcepcionServicio {
        return mapeador.deCuenta(servicioCuenta.consultarCuenta(cuenta));
    }

    @GET
    @Path("{cuenta}/movimientos")
    public RespuestaDto consultarMovimientos(@PathParam("cuenta") String cuenta) throws ExcepcionServicio {
        return mapeador.deMovimientos(servicioCuenta.consultarMovimientos(cuenta));
    }
}
