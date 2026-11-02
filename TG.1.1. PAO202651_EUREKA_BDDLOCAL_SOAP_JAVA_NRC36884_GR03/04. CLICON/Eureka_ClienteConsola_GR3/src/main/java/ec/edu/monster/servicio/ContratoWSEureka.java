package ec.edu.monster.servicio;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.util.List;

/**
 * Contrato de los tres servicios SOAP publicados por WS_EUREKA_JAVA_SOAP
 * (AutenticacionWS, CuentaWS y TransaccionWS), escrito igual que su WSDL. El
 * token viaja en la cabecera SOAP, como lo declara el servidor.
 */
public interface ContratoWSEureka {

    String ESPACIO_NOMBRES = "http://wservicios.controladores.monster.edu.ec/";

    @WebService(name = "AutenticacionWS", targetNamespace = ESPACIO_NOMBRES)
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
    interface Autenticacion {

        @WebMethod(operationName = "iniciarSesion")
        @WebResult(name = "return")
        Respuesta iniciarSesion(
                @WebParam(name = "usuario") String usuario,
                @WebParam(name = "clave") String clave,
                @WebParam(name = "equipo") String equipo);

        @WebMethod(operationName = "cerrarSesion")
        @WebResult(name = "return")
        Respuesta cerrarSesion(
                @WebParam(name = "token", header = true) String token);
    }

    @WebService(name = "CuentaWS", targetNamespace = ESPACIO_NOMBRES)
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
    interface Cuenta {

        @WebMethod(operationName = "consultarCuenta")
        @WebResult(name = "return")
        Respuesta consultarCuenta(
                @WebParam(name = "token", header = true) String token,
                @WebParam(name = "cuenta") String cuenta);

        @WebMethod(operationName = "consultarMovimientos")
        @WebResult(name = "return")
        Respuesta consultarMovimientos(
                @WebParam(name = "token", header = true) String token,
                @WebParam(name = "cuenta") String cuenta);
    }

    @WebService(name = "TransaccionWS", targetNamespace = ESPACIO_NOMBRES)
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
    interface Transaccion {

        @WebMethod(operationName = "depositar")
        @WebResult(name = "return")
        Respuesta depositar(
                @WebParam(name = "token", header = true) String token,
                @WebParam(name = "cuenta") String cuenta,
                @WebParam(name = "importe") BigDecimal importe);

        @WebMethod(operationName = "retirar")
        @WebResult(name = "return")
        Respuesta retirar(
                @WebParam(name = "token", header = true) String token,
                @WebParam(name = "cuenta") String cuenta,
                @WebParam(name = "importe") BigDecimal importe);

        @WebMethod(operationName = "transferir")
        @WebResult(name = "return")
        Respuesta transferir(
                @WebParam(name = "token", header = true) String token,
                @WebParam(name = "cuentaOrigen") String cuentaOrigen,
                @WebParam(name = "cuentaDestino") String cuentaDestino,
                @WebParam(name = "importe") BigDecimal importe);
    }

    // Respuesta exitosa de todas las operaciones; los campos que no aplican llegan nulos
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "respuesta")
    class Respuesta {

        public String estado;
        public String codigo;
        public String mensaje;
        public String token;
        public String expiraEn;
        public String codigoEmpleado;
        public String nombreEmpleado;
        public String codigoSucursal;
        public String sucursal;
        public DatosCuenta cuenta;

        @XmlElementWrapper(name = "movimientos")
        @XmlElement(name = "movimiento")
        public List<DatosMovimiento> movimientos;

        public BigDecimal nuevoSaldo;
        public Integer numeroMovimiento;
        public Integer numeroMovimientoDestino;
    }

    // Datos de la cuenta que devuelve consultarCuenta
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "cuenta")
    class DatosCuenta {

        public String codigo;
        public String titular;
        public String moneda;
        public BigDecimal saldo;
        public String estado;
        public String sucursal;
        public String fechaCreacion;
    }

    // Movimiento que devuelve consultarMovimientos
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "movimiento")
    class DatosMovimiento {

        public Integer numero;
        public String fecha;
        public String tipo;
        public String accion;
        public BigDecimal importe;
        public String cuentaReferencia;
    }
}
