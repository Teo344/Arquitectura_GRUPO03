package ec.edu.monster.servicio;

import android.os.Build;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.SesionModelo;
import org.w3c.dom.Element;

/** Acceso al servicio SOAP público de autenticación (AutenticacionWS): RQF-001 y RQF-002. */
public class AutenticacionSoapService {

    private static final String SERVICIO = "AutenticacionWS";

    private final ClienteSoap cliente;

    public AutenticacionSoapService() {
        this(new ClienteSoap());
    }

    public AutenticacionSoapService(ClienteSoap cliente) {
        this.cliente = cliente;
    }

    /** equipo: nombre del dispositivo que el servidor guarda en logsession. */
    public SesionModelo iniciarSesion(String usuario, String clave) {
        String parametros = ClienteSoap.elemento("usuario", usuario)
                + ClienteSoap.elemento("clave", clave)
                + ClienteSoap.elemento("equipo", Build.MANUFACTURER + " " + Build.MODEL);

        Element respuesta = cliente.invocar(SERVICIO, "iniciarSesion", null, parametros);

        String token = ClienteSoap.texto(respuesta, "token");
        if (token == null || token.trim().isEmpty()) {
            throw new ErrorServicioModelo(null, "El servidor no devolvió el token de sesión.");
        }
        return new SesionModelo(token, ClienteSoap.texto(respuesta, "expiraEn"),
                ClienteSoap.texto(respuesta, "codigoEmpleado"), ClienteSoap.texto(respuesta, "nombreEmpleado"),
                ClienteSoap.texto(respuesta, "codigoSucursal"), ClienteSoap.texto(respuesta, "sucursal"));
    }

    /** Invalida el token en el servidor y registra la salida en logsession. */
    public void cerrarSesion(String token) {
        cliente.invocar(SERVICIO, "cerrarSesion", token, null);
    }
}
