package ec.edu.monster.controladores.wservicios;

import ec.edu.monster.seguridad.ManejadorAutenticacion;
import ec.edu.monster.seguridad.Sesion;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioTransaccion;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceContext;
import java.math.BigDecimal;

/**
 * Web Service protegido por ManejadorAutenticacion: depósito (RQF-005),
 * retiro (RQF-006) y transferencia (RQF-007). El empleado de cada movimiento
 * sale de la sesión que dejó el handler, nunca de un parámetro (RN-06).
 *
 * @author carlo
 */
@WebService(serviceName = "TransaccionWS")
@HandlerChain(file = "Eureka_handler.xml")
public class TransaccionWS {

    @Resource
    private WebServiceContext contexto;

    @Inject
    private ServicioTransaccion servicioTransaccion;

    @Inject
    private RespuestaMapeador mapeador;

    @WebMethod(operationName = "depositar")
    public RespuestaDto depositar(
            @WebParam(name = "token", header = true) String token,
            @WebParam(name = "cuenta") String cuenta,
            @WebParam(name = "importe") BigDecimal importe) throws ExcepcionServicio {
        return mapeador.deDeposito(servicioTransaccion.depositar(sesion(), cuenta, importe));
    }

    @WebMethod(operationName = "retirar")
    public RespuestaDto retirar(
            @WebParam(name = "token", header = true) String token,
            @WebParam(name = "cuenta") String cuenta,
            @WebParam(name = "importe") BigDecimal importe) throws ExcepcionServicio {
        return mapeador.deRetiro(servicioTransaccion.retirar(sesion(), cuenta, importe));
    }

    @WebMethod(operationName = "transferir")
    public RespuestaDto transferir(
            @WebParam(name = "token", header = true) String token,
            @WebParam(name = "cuentaOrigen") String cuentaOrigen,
            @WebParam(name = "cuentaDestino") String cuentaDestino,
            @WebParam(name = "importe") BigDecimal importe) throws ExcepcionServicio {
        return mapeador.deTransferencia(servicioTransaccion.transferir(sesion(), cuentaOrigen, cuentaDestino, importe));
    }

    private Sesion sesion() {
        return (Sesion) contexto.getMessageContext().get(ManejadorAutenticacion.PROPIEDAD_SESION);
    }
}
