package ec.edu.monster.controladores.wservicios;

import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioCuenta;
import ec.edu.monster.vista.RespuestaDto;
import ec.edu.monster.vista.RespuestaMapeador;
import jakarta.inject.Inject;
import jakarta.jws.HandlerChain;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

/**
 * Web Service protegido por ManejadorAutenticacion: consulta de cuenta
 * (RQF-003) y de movimientos (RQF-004). El parámetro token solo sirve para
 * publicar la cabecera en el WSDL; lo valida el handler.
 *
 * @author carlo
 */
@WebService(serviceName = "CuentaWS")
@HandlerChain(file = "Eureka_handler.xml")
public class CuentaWS {

    @Inject
    private ServicioCuenta servicioCuenta;

    @Inject
    private RespuestaMapeador mapeador;

    @WebMethod(operationName = "consultarCuenta")
    public RespuestaDto consultarCuenta(
            @WebParam(name = "token", header = true) String token,
            @WebParam(name = "cuenta") String cuenta) throws ExcepcionServicio {
        return mapeador.deCuenta(servicioCuenta.consultarCuenta(cuenta));
    }

    @WebMethod(operationName = "consultarMovimientos")
    public RespuestaDto consultarMovimientos(
            @WebParam(name = "token", header = true) String token,
            @WebParam(name = "cuenta") String cuenta) throws ExcepcionServicio {
        return mapeador.deMovimientos(servicioCuenta.consultarMovimientos(cuenta));
    }
}
