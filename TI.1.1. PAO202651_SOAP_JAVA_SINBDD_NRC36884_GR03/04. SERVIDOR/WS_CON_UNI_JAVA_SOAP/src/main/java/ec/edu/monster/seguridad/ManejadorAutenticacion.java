package ec.edu.monster.seguridad;

import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ExcepcionServicio.ErrorServicio;
import jakarta.inject.Inject;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.util.Set;
import javax.xml.namespace.QName;
import org.w3c.dom.NodeList;

/**
 * Middleware de autenticación (RQF-007). Lee el token de la cabecera SOAP, lo
 * valida con {@link GestorToken} y, si falla, responde un SOAPFault con el
 * mismo detalle ErrorServicio (codigo, mensaje) que generan los servicios.
 *
 * @author carlo
 */
public class ManejadorAutenticacion implements SOAPHandler<SOAPMessageContext> {

    public static final String PROPIEDAD_USUARIO = "usuario";
    private static final String ELEMENTO_TOKEN = "token";

    @Inject
    private GestorToken gestorToken;

    @Override
    public boolean handleMessage(SOAPMessageContext contexto) {
        if ((Boolean) contexto.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
            return true;
        }
        try {
            String usuario = gestorToken.validar(leerToken(contexto));
            contexto.put(PROPIEDAD_USUARIO, usuario);
            contexto.setScope(PROPIEDAD_USUARIO, MessageContext.Scope.APPLICATION);
            return true;
        } catch (ExcepcionServicio e) {
            throw new SOAPFaultException(crearFault(e.getFaultInfo()));
        }
    }

    /**
     * Devuelve el texto del primer elemento token de la cabecera, sin importar
     * su namespace, o null si no hay.
     */
    private static String leerToken(SOAPMessageContext contexto) {
        try {
            SOAPHeader cabecera = contexto.getMessage().getSOAPHeader();
            NodeList tokens = cabecera == null ? null : cabecera.getElementsByTagNameNS("*", ELEMENTO_TOKEN);
            return tokens == null || tokens.getLength() == 0 ? null : tokens.item(0).getTextContent();
        } catch (SOAPException e) {
            return null;
        }
    }

    private static SOAPFault crearFault(ErrorServicio error) {
        try {
            SOAPFault fault = SOAPFactory.newInstance().createFault(error.mensaje,
                    new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "Server"));
            SOAPElement detalle = fault.addDetail()
                    .addDetailEntry(new QName(ExcepcionServicio.NAMESPACE, "ErrorServicio", "ns2"));
            detalle.addChildElement(new QName("codigo")).addTextNode(error.codigo);
            detalle.addChildElement(new QName("mensaje")).addTextNode(error.mensaje);
            return fault;
        } catch (SOAPException e) {
            throw new WebServiceException("No se pudo construir el SOAPFault", e);
        }
    }

    @Override
    public Set<QName> getHeaders() {
        return Set.of(new QName(ExcepcionServicio.NAMESPACE, ELEMENTO_TOKEN));
    }

    @Override
    public boolean handleFault(SOAPMessageContext contexto) {
        return true;
    }

    @Override
    public void close(MessageContext contexto) {
    }
}
