package ec.edu.monster.servicio;

import android.os.Build;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.SesionModelo;
import org.json.JSONObject;

/** Acceso al recurso REST público de autenticación (/autenticacion): RQF-001 y RQF-002. */
public class AutenticacionRestService {

    private final ClienteRest cliente;

    public AutenticacionRestService() {
        this(new ClienteRest());
    }

    public AutenticacionRestService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    /** equipo: nombre del dispositivo que el servidor guarda en logsession. */
    public SesionModelo iniciarSesion(String usuario, String clave) {
        JSONObject credenciales = ClienteRest.crearJson("usuario", usuario, "clave", clave,
                "equipo", Build.MANUFACTURER + " " + Build.MODEL);
        JSONObject respuesta = cliente.post("/autenticacion/login", null, credenciales);

        String token = ClienteRest.texto(respuesta, "token");
        if (token == null || token.trim().isEmpty()) {
            throw new ErrorServicioModelo(null, "El servidor no devolvió el token de sesión.");
        }
        return new SesionModelo(token, ClienteRest.texto(respuesta, "expiraEn"),
                ClienteRest.texto(respuesta, "codigoEmpleado"), ClienteRest.texto(respuesta, "nombreEmpleado"),
                ClienteRest.texto(respuesta, "codigoSucursal"), ClienteRest.texto(respuesta, "sucursal"));
    }

    /** Invalida el token en el servidor y registra la salida en logsession. */
    public void cerrarSesion(String token) {
        cliente.post("/autenticacion/cerrar-sesion", token, null);
    }
}
