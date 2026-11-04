package ec.edu.monster.servicio;

import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.SesionModelo;
import org.json.JSONObject;

/** Acceso al recurso REST de sesiones (/sesiones): CU-01 y CU-02. */
public class AutenticacionRestService {

    private final ClienteRest cliente;

    public AutenticacionRestService() {
        this(new ClienteRest());
    }

    public AutenticacionRestService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public SesionModelo iniciarSesion(String usuario, String clave) {
        JSONObject credenciales = ClienteRest.crearJson("usuario", usuario, "clave", clave);
        JSONObject respuesta = cliente.post("/sesiones", null, credenciales);

        String token = ClienteRest.texto(respuesta, "token");
        if (token == null || token.trim().isEmpty()) {
            throw new ErrorServicioModelo(null, "El servidor no devolvió el token de sesión.");
        }
        return new SesionModelo(token, ClienteRest.texto(respuesta, "expiraEn"),
                ClienteRest.entero(respuesta, "codUsuario"), ClienteRest.texto(respuesta, "usuario"),
                ClienteRest.texto(respuesta, "nombre"));
    }

    /** Invalida el token en el servidor. */
    public void cerrarSesion(String token) {
        cliente.delete("/sesiones/actual", token);
    }
}
