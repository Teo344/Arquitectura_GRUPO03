package ec.edu.monster.servicio;

import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.servicio.AutenticacionWS_Service;
import ec.edu.monster.servicio.Respuesta;

/** Adaptador del servicio SOAP de autenticación generado desde el WSDL. */
public class AutenticacionSoapService {
    private final String endpoint;

    public AutenticacionSoapService() {
        String baseUrl = System.getProperty("monster.soap.baseUrl", "http://localhost:8080/WS_CON_UNI_JAVA_SOAP");
        this.endpoint = baseUrl.replaceAll("/$", "") + "/AutenticacionWS";
    }

    public SesionUsuario iniciarSesion(String usuario, String clave) throws Exception {
        AutenticacionWS_Service service = new AutenticacionWS_Service();
        AutenticacionWS port = service.getAutenticacionWSPort();
        jakarta.xml.ws.BindingProvider binding = (jakarta.xml.ws.BindingProvider) port;
        binding.getRequestContext().put(jakarta.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

        Respuesta respuesta = port.login(usuario, clave);
        if (respuesta == null) {
            throw new IllegalStateException("El servidor devolvió una respuesta vacía.");
        }
        if (!"OK".equalsIgnoreCase(respuesta.getEstado())) {
            throw new IllegalStateException(mensajeServidor(respuesta));
        }
        if (respuesta.getToken() == null || respuesta.getToken().isBlank()) {
            throw new IllegalStateException("El servidor no devolvió el token de sesión.");
        }
        return new SesionUsuario(usuario, respuesta.getToken(), respuesta.getExpiraEn());
    }

    private String mensajeServidor(Respuesta respuesta) {
        return respuesta.getMensaje() == null || respuesta.getMensaje().isBlank()
                ? "No fue posible iniciar sesión." : respuesta.getMensaje();
    }
}
