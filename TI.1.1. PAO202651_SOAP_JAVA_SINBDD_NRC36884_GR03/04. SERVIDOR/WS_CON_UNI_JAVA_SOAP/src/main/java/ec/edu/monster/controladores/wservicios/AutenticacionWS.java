package ec.edu.monster.controladores.wservicios;

import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioAutenticacion;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Web Service público (sin handler): login y cambio de clave (RQF-001).
 *
 * @author carlo
 */
@WebService(serviceName = "AutenticacionWS")
public class AutenticacionWS {

    @Inject
    private ServicioAutenticacion servicioAutenticacion;

    @Inject
    private RespuestaMapeador mapeador;

    @WebMethod(operationName = "login")
    public RespuestaDto login(@WebParam(name = "usuario") String usuario,
            @WebParam(name = "clave") String clave) throws ExcepcionServicio {
        return mapeador.deToken(servicioAutenticacion.login(usuario, clave));
    }

    @WebMethod(operationName = "cambiarClave")
    public RespuestaDto cambiarClave(@WebParam(name = "usuario") String usuario,
            @WebParam(name = "claveActual") String claveActual,
            @WebParam(name = "claveNueva") String claveNueva) throws ExcepcionServicio {
        servicioAutenticacion.cambiarClave(usuario, claveActual, claveNueva);
        return mapeador.deCambioClave();
    }
}
