package client.ws;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

@WebService(name = "ConversionUnidadesWS", targetNamespace = "http://wservicios.controladores.monster.edu.ec/")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface ContratoWSConversor {

    @WebMethod(operationName = "convertirLongitud")
    @WebResult(name = "return")
    Respuesta convertirLongitud(
            @WebParam(name = "solicitud") Solicitud solicitud);

    @WebMethod(operationName = "convertirMasa")
    @WebResult(name = "return")
    Respuesta convertirMasa(
            @WebParam(name = "solicitud") Solicitud solicitud);

    @WebMethod(operationName = "convertirTemperatura")
    @WebResult(name = "return")
    Respuesta convertirTemperatura(
            @WebParam(name = "solicitud") Solicitud solicitud);

    // El login esta publicado en un servicio aparte del servidor (AutenticacionWS)
    @WebService(name = "AutenticacionWS", targetNamespace = "http://wservicios.controladores.monster.edu.ec/")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
    interface Autenticacion {

        @WebMethod(operationName = "login")
        @WebResult(name = "return")
        Respuesta login(
                @WebParam(name = "usuario") String usuario,
                @WebParam(name = "clave") String contrasenia);
    }

    // Datos que se envian al servidor en cada conversion
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "solicitudConversion", propOrder = {"valor", "unidadOrigen", "unidadDestino"})
    class Solicitud {

        public Double valor;
        public String unidadOrigen;
        public String unidadDestino;

        public Solicitud() {
        }

        public Solicitud(double valor, String unidadOrigen, String unidadDestino) {
            this.valor = valor;
            this.unidadOrigen = unidadOrigen;
            this.unidadDestino = unidadDestino;
        }
    }

    // Respuesta que devuelve el servidor en todas las operaciones
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "respuesta")
    class Respuesta {

        public String estado;
        public String codigo;
        public String mensaje;
        public Double resultado;
        public String unidadOrigen;
        public String unidadDestino;
        public String token;
        public String expiraEn;
    }
}
