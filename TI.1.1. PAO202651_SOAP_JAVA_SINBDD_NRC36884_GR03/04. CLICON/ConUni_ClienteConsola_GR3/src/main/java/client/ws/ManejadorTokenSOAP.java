package client.ws;

import jakarta.xml.soap.SOAPEnvelope;
import jakarta.xml.soap.SOAPHeader;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.namespace.QName;
import java.util.Set;
import java.util.function.Supplier;

public class ManejadorTokenSOAP implements SOAPHandler<SOAPMessageContext> {

    private final Supplier<String> proveedorToken;

    public ManejadorTokenSOAP(Supplier<String> proveedorToken) {
        this.proveedorToken = proveedorToken;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext contexto) {
        Boolean esSalida = (Boolean) contexto.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (!Boolean.TRUE.equals(esSalida)) {
            return true;
        }

        QName operacion = (QName) contexto.get(MessageContext.WSDL_OPERATION);
        if (operacion != null && "login".equals(operacion.getLocalPart())) {
            return true;
        }

        String token = proveedorToken.get();
        if (token == null || token.isBlank()) {
            throw new RuntimeException("No hay sesion activa. Debe autenticarse primero.");
        }

        try {
            var mensaje = contexto.getMessage();
            SOAPEnvelope sobre = mensaje.getSOAPPart().getEnvelope();
            SOAPHeader encabezado = mensaje.getSOAPHeader();

            if (encabezado == null) {
                encabezado = sobre.addHeader();
            }

            QName nombreToken = new QName("http://wservicios.controladores.monster.edu.ec/", "token", "ws");
            encabezado.addHeaderElement(nombreToken).addTextNode(token);
            mensaje.saveChanges();
            return true;
        } catch (Exception ex) {
            throw new RuntimeException("No fue posible adjuntar el token SOAP: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleFault(SOAPMessageContext contexto) {
        return true;
    }

    @Override
    public void close(MessageContext contexto) {
    }
}
