package ec.edu.monster.seguridad;

import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.servicios.ExcepcionServicio;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Credenciales y sesiones en memoria (RQF-001, RQF-007). Guarda el usuario
 * vigente y emite tokens opacos (UUID) con su expiración. Al reiniciar el
 * servidor se vuelve a las credenciales iniciales y se pierden las sesiones.
 *
 * @author carlo
 */
@ApplicationScoped
public class GestorToken {

    public record TokenEmitido(String token, Instant expiraEn) {
    }

    private record Sesion(String usuario, Instant expiraEn) {
    }

    private final Map<String, Sesion> sesiones = new ConcurrentHashMap<>();

    @Inject
    @ConfigProperty(name = "token.minutos")
    private long minutos;

    @Inject
    @ConfigProperty(name = "usuario.nombre")
    private String usuario;

    @Inject
    @ConfigProperty(name = "usuario.clave")
    private String claveInicial;

    private volatile String clave;

    @PostConstruct
    void iniciar() {
        clave = claveInicial;
    }

    public boolean credencialesValidas(String nombre, String clave) {
        return usuario.equals(nombre) && this.clave.equals(clave);
    }

    /**
     * Cambia la clave y cierra todas las sesiones.
     */
    public void cambiarClave(String claveNueva) {
        clave = claveNueva;
        sesiones.clear();
    }

    public TokenEmitido generar(String usuario) {
        Instant ahora = Instant.now();
        sesiones.values().removeIf(sesion -> sesion.expiraEn().isBefore(ahora));
        String token = UUID.randomUUID().toString();
        Instant expiraEn = ahora.plus(minutos, ChronoUnit.MINUTES);
        sesiones.put(token, new Sesion(usuario, expiraEn));
        return new TokenEmitido(token, expiraEn);
    }

    /**
     * Devuelve el usuario del token. Inexistente: AUTH-001; expirado: AUTH-002.
     */
    public String validar(String token) throws ExcepcionServicio {
        Sesion sesion = token == null ? null : sesiones.get(token.trim());
        if (sesion == null) {
            throw new ExcepcionServicio(CodigoError.AUTH_001);
        }
        if (sesion.expiraEn().isBefore(Instant.now())) {
            sesiones.remove(token.trim());
            throw new ExcepcionServicio(CodigoError.AUTH_002);
        }
        return sesion.usuario();
    }
}
