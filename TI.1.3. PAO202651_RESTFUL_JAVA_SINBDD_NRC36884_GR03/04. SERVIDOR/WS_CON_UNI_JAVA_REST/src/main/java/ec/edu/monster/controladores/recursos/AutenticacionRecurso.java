package ec.edu.monster.controladores.recursos;

import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioAutenticacion;
import ec.edu.monster.vista.CambioClaveDto;
import ec.edu.monster.vista.CredencialesDto;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Recurso público (sin token): login y cambio de clave (RQF-001).
 *
 * @author carlo
 */
@Path("autenticacion")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AutenticacionRecurso {

    @Inject
    private ServicioAutenticacion servicioAutenticacion;

    @Inject
    private RespuestaMapeador mapeador;

    @POST
    @Path("login")
    public RespuestaDto login(CredencialesDto credenciales) throws ExcepcionServicio {
        CredencialesDto c = credenciales != null ? credenciales : new CredencialesDto();
        return mapeador.deToken(servicioAutenticacion.login(c.usuario, c.clave));
    }

    @POST
    @Path("cambiar-clave")
    public RespuestaDto cambiarClave(CambioClaveDto cambio) throws ExcepcionServicio {
        CambioClaveDto c = cambio != null ? cambio : new CambioClaveDto();
        servicioAutenticacion.cambiarClave(c.usuario, c.claveActual, c.claveNueva);
        return mapeador.deCambioClave();
    }
}
