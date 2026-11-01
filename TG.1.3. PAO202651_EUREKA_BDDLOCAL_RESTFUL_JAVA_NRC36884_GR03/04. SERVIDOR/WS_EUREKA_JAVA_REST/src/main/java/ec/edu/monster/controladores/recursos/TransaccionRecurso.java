package ec.edu.monster.controladores.recursos;

import ec.edu.monster.seguridad.ManejadorAutenticacion;
import ec.edu.monster.seguridad.Protegido;
import ec.edu.monster.seguridad.Sesion;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioTransaccion;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import ec.edu.monster.vista.SolicitudTransaccionDto;
import ec.edu.monster.vista.SolicitudTransferenciaDto;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

/**
 * Recurso protegido por ManejadorAutenticacion: depósito (RQF-005), retiro
 * (RQF-006) y transferencia (RQF-007). El empleado de cada movimiento sale de
 * la sesión que dejó el filtro, nunca del cuerpo de la petición (RN-06).
 *
 * @author carlo
 */
@Path("transacciones")
@Protegido
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransaccionRecurso {

    @Inject
    private ServicioTransaccion servicioTransaccion;

    @Inject
    private RespuestaMapeador mapeador;

    @Context
    private HttpServletRequest peticion;

    @POST
    @Path("depositos")
    public RespuestaDto depositar(SolicitudTransaccionDto solicitud) throws ExcepcionServicio {
        SolicitudTransaccionDto s = solicitud != null ? solicitud : new SolicitudTransaccionDto();
        return mapeador.deDeposito(servicioTransaccion.depositar(sesion(), s.cuenta, s.importe));
    }

    @POST
    @Path("retiros")
    public RespuestaDto retirar(SolicitudTransaccionDto solicitud) throws ExcepcionServicio {
        SolicitudTransaccionDto s = solicitud != null ? solicitud : new SolicitudTransaccionDto();
        return mapeador.deRetiro(servicioTransaccion.retirar(sesion(), s.cuenta, s.importe));
    }

    @POST
    @Path("transferencias")
    public RespuestaDto transferir(SolicitudTransferenciaDto solicitud) throws ExcepcionServicio {
        SolicitudTransferenciaDto s = solicitud != null ? solicitud : new SolicitudTransferenciaDto();
        return mapeador.deTransferencia(
                servicioTransaccion.transferir(sesion(), s.cuentaOrigen, s.cuentaDestino, s.importe));
    }

    private Sesion sesion() {
        return (Sesion) peticion.getAttribute(ManejadorAutenticacion.PROPIEDAD_SESION);
    }
}
