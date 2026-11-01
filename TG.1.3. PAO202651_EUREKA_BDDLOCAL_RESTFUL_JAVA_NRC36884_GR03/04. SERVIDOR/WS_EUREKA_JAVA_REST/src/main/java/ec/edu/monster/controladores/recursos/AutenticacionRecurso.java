package ec.edu.monster.controladores.recursos;

import ec.edu.monster.seguridad.ManejadorAutenticacion;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioAutenticacion;
import ec.edu.monster.vista.CredencialesDto;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/**
 * Recurso público (sin filtro): inicio (RQF-001) y cierre (RQF-002) de
 * sesión. cerrar-sesion recibe el token en la cabecera Authorization igual que
 * los recursos protegidos, pero lo valida el servicio.
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

    @Context
    private HttpServletRequest peticion;

    @POST
    @Path("login")
    public RespuestaDto login(CredencialesDto credenciales) throws ExcepcionServicio {
        CredencialesDto c = credenciales != null ? credenciales : new CredencialesDto();
        return mapeador.deSesion(servicioAutenticacion.iniciarSesion(c.usuario, c.clave,
                peticion.getRemoteAddr(), c.equipo));
    }

    @POST
    @Path("cerrar-sesion")
    public RespuestaDto cerrarSesion(@HeaderParam(HttpHeaders.AUTHORIZATION) String autorizacion)
            throws ExcepcionServicio {
        servicioAutenticacion.cerrarSesion(ManejadorAutenticacion.leerToken(autorizacion));
        return mapeador.deCierreSesion();
    }
}
