package ec.edu.monster.servicio;

import ec.edu.monster.modelo.SesionUsuario;
import org.json.JSONObject;

/** Acceso al recurso REST público de autenticación (POST /autenticacion/login). */
public class AutenticacionRestService {

    private final ClienteRest cliente;

    public AutenticacionRestService() {
        this(new ClienteRest());
    }

    public AutenticacionRestService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public SesionUsuario iniciarSesion(String usuario, String clave) throws ExcepcionServicio {
        JSONObject credenciales = ClienteRest.crearJson("usuario", usuario, "clave", clave);
        JSONObject respuesta = cliente.post("/autenticacion/login", null, credenciales);

        String token = ClienteRest.texto(respuesta, "token");
        if (token == null || token.trim().isEmpty()) {
            throw new ExcepcionServicio("RESPUESTA", "El servidor no devolvió el token de sesión.");
        }
        return new SesionUsuario(usuario, token, ClienteRest.texto(respuesta, "expiraEn"));
    }
}
