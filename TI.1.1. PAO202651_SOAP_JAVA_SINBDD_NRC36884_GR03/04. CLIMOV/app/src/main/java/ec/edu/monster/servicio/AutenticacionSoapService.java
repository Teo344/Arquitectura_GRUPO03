package ec.edu.monster.servicio;

import ec.edu.monster.modelo.SesionUsuario;
import org.w3c.dom.Element;

/** Acceso al servicio SOAP público de autenticación (AutenticacionWS). */
public class AutenticacionSoapService {

    private static final String SERVICIO = "AutenticacionWS";

    private final ClienteSoap cliente;

    public AutenticacionSoapService() {
        this(new ClienteSoap());
    }

    public AutenticacionSoapService(ClienteSoap cliente) {
        this.cliente = cliente;
    }

    public SesionUsuario iniciarSesion(String usuario, String clave) throws ExcepcionServicio {
        String parametros = "<usuario>" + ClienteSoap.escapar(usuario) + "</usuario>"
                + "<clave>" + ClienteSoap.escapar(clave) + "</clave>";

        Element respuesta = cliente.invocar(SERVICIO, "login", null, parametros);
        ClienteSoap.validar(respuesta);

        String token = ClienteSoap.texto(respuesta, "token");
        if (token == null || token.trim().isEmpty()) {
            throw new ExcepcionServicio("RESPUESTA", "El servidor no devolvió el token de sesión.");
        }
        return new SesionUsuario(usuario, token, ClienteSoap.texto(respuesta, "expiraEn"));
    }
}
