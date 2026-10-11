package ec.edu.monster.servicios;

import ec.edu.monster.modelo.CodigoError;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.ws.WebFault;

/**
 * Excepción de negocio. Metro la convierte en un SOAPFault cuyo detalle es
 * {@link ErrorServicio} (RQF-010).
 *
 * @author carlo
 */
@WebFault(name = "ErrorServicio", targetNamespace = ExcepcionServicio.NAMESPACE)
public class ExcepcionServicio extends Exception {

    public static final String NAMESPACE = "http://wservicios.controladores.monster.edu.ec/";

    /**
     * Detalle estructurado del SOAPFault; el handler arma el mismo detalle a
     * mano. {@code @XmlRootElement} evita que el WSDL lo declare nillable.
     */
    @XmlRootElement(name = "ErrorServicio", namespace = NAMESPACE)
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "errorServicio", propOrder = {"codigo", "mensaje"})
    public static class ErrorServicio {

        public String codigo;
        public String mensaje;

        public ErrorServicio() {
        }

        public ErrorServicio(String codigo, String mensaje) {
            this.codigo = codigo;
            this.mensaje = mensaje;
        }
    }

    private final ErrorServicio faultInfo;

    public ExcepcionServicio(CodigoError codigoError) {
        this(codigoError.getMensaje(), new ErrorServicio(codigoError.getCodigo(), codigoError.getMensaje()));
    }

    public ExcepcionServicio(String mensaje, ErrorServicio faultInfo) {
        super(mensaje);
        this.faultInfo = faultInfo;
    }

    public ExcepcionServicio(String mensaje, ErrorServicio faultInfo, Throwable causa) {
        super(mensaje, causa);
        this.faultInfo = faultInfo;
    }

    public ErrorServicio getFaultInfo() {
        return faultInfo;
    }
}
