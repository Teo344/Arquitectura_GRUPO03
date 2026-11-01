package ec.edu.monster.controladores.wservicios;

import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioAutenticacion;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;

/**
 * Web Service público (sin handler): inicio (RQF-001) y cierre (RQF-002) de
 * sesión. cerrarSesion recibe el token en la cabecera SOAP igual que las
 * operaciones protegidas, pero lo valida el servicio.
 *
 * @author carlo
 */
@WebService(serviceName = "AutenticacionWS")
public class AutenticacionWS {

    @Resource
    private WebServiceContext contexto;

    @Inject
    private ServicioAutenticacion servicioAutenticacion;

    @Inject
    private RespuestaMapeador mapeador;

    /**
     * equipo es opcional: el nombre del equipo del cliente para logsession.
     */
    @WebMethod(operationName = "iniciarSesion")
    public RespuestaDto iniciarSesion(@WebParam(name = "usuario") String usuario,
            @WebParam(name = "clave") String clave,
            @WebParam(name = "equipo") String equipo) throws ExcepcionServicio {
        return mapeador.deSesion(servicioAutenticacion.iniciarSesion(usuario, clave, ipCliente(), equipo));
    }

    @WebMethod(operationName = "cerrarSesion")
    public RespuestaDto cerrarSesion(
            @WebParam(name = "token", header = true) String token) throws ExcepcionServicio {
        servicioAutenticacion.cerrarSesion(token);
        return mapeador.deCierreSesion();
    }

    private String ipCliente() {
        Object peticion = contexto.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        return peticion instanceof HttpServletRequest http ? http.getRemoteAddr() : null;
    }
}
