package ec.edu.monster.seguridad;

import ec.edu.monster.datos.ConexionBD;
import ec.edu.monster.datos.LogSesionDao;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.servicios.ExcepcionServicio;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Sesiones en memoria (RQF-001, RQF-002, RNF-007). Emite tokens opacos (UUID)
 * que caducan tras sesion.minutos de inactividad: cada validación correcta
 * renueva el plazo. Al reiniciar el servidor se pierden las sesiones.
 *
 * @author carlo
 */
@ApplicationScoped
public class GestorToken {

    private static final Logger LOG = Logger.getLogger(GestorToken.class.getName());

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

    @Inject
    private LogSesionDao logSesionDao;

    public TokenEmitido generar(Sesion sesion) {
        Instant ahora = Instant.now();
        purgarVencidas(ahora);
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

    /**
     * Cierra la sesión del token y la devuelve (RQF-002).
     */
    public Sesion invalidar(String token) throws ExcepcionServicio {
        Entrada entrada = vigente(token);
        sesiones.remove(token.trim());
        return entrada.sesion;
    }

    private Entrada vigente(String token) throws ExcepcionServicio {
        Entrada entrada = token == null ? null : sesiones.get(token.trim());
        if (entrada == null) {
            throw new ExcepcionServicio(CodigoError.AUTH_003);
        }
        if (vencida(entrada, Instant.now())) {
            if (sesiones.remove(token.trim(), entrada)) {
                registrarSalida(entrada);
            }
            throw new ExcepcionServicio(CodigoError.AUTH_003);
        }
        return entrada;
    }

    private void purgarVencidas(Instant ahora) {
        sesiones.forEach((token, entrada) -> {
            if (vencida(entrada, ahora) && sesiones.remove(token, entrada)) {
                registrarSalida(entrada);
            }
        });
    }

    private boolean vencida(Entrada entrada, Instant ahora) {
        return entrada.ultimoAcceso.plus(duracion()).isBefore(ahora);
    }

    /**
     * Una sesión que caduca sin cerrarse registra como salida el momento en
     * que venció. Si falla la BD solo se registra en el log: la sesión ya no
     * es válida de todas formas.
     */
    private void registrarSalida(Entrada entrada) {
        try (Connection cn = ConexionBD.getInstancia().obtenerConexion()) {
            logSesionDao.registrarSalida(cn, entrada.sesion.logId(), entrada.ultimoAcceso.plus(duracion()));
        } catch (ExcepcionServicio | SQLException e) {
            LOG.log(Level.WARNING, "No se pudo registrar la salida de la sesión {0}", entrada.sesion.logId());
        }
    }

    private Duration duracion() {
        return Duration.ofMinutes(minutos);
    }
}
