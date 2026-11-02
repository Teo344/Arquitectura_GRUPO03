package ec.edu.monster.seguridad;

import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.servicios.ExcepcionServicio;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Sesiones en memoria. Emite tokens opacos (UUID) que caducan tras
 * sesion.minutos de inactividad: cada validación correcta renueva el plazo.
 * Al reiniciar el servidor se pierden las sesiones.
 *
 * @author carlo
 */
@ApplicationScoped
public class GestorToken {

    public record TokenEmitido(String token, Instant expiraEn) {
    }

    private static final class Entrada {

        private final Sesion sesion;
        private volatile Instant ultimoAcceso;

        private Entrada(Sesion sesion, Instant ultimoAcceso) {
            this.sesion = sesion;
            this.ultimoAcceso = ultimoAcceso;
        }
    }

    private final Map<String, Entrada> sesiones = new ConcurrentHashMap<>();

    @Inject
    @ConfigProperty(name = "sesion.minutos")
    private long minutos;

    public TokenEmitido generar(Sesion sesion) {
        Instant ahora = Instant.now();
        sesiones.entrySet().removeIf(e -> vencida(e.getValue(), ahora));
        String token = UUID.randomUUID().toString();
        sesiones.put(token, new Entrada(sesion, ahora));
        return new TokenEmitido(token, ahora.plus(duracion()));
    }

    /**
     * Devuelve la sesión del token y renueva su plazo. Ausente, desconocido o
     * vencido: AUTH-003.
     */
    public Sesion validar(String token) throws ExcepcionServicio {
        Entrada entrada = vigente(token);
        entrada.ultimoAcceso = Instant.now();
        return entrada.sesion;
    }

    public void invalidar(String token) throws ExcepcionServicio {
        vigente(token);
        sesiones.remove(token.trim());
    }

    private Entrada vigente(String token) throws ExcepcionServicio {
        Entrada entrada = token == null ? null : sesiones.get(token.trim());
        if (entrada == null) {
            throw new ExcepcionServicio(CodigoError.AUTH_003);
        }
        if (vencida(entrada, Instant.now())) {
            sesiones.remove(token.trim(), entrada);
            throw new ExcepcionServicio(CodigoError.AUTH_003);
        }
        return entrada;
    }

    private boolean vencida(Entrada entrada, Instant ahora) {
        return entrada.ultimoAcceso.plus(duracion()).isBefore(ahora);
    }

    private Duration duracion() {
        return Duration.ofMinutes(minutos);
    }
}
