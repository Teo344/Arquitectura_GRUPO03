package ec.edu.monster.seguridad;

import ec.edu.monster.controladores.recursos.MapeadorExcepciones;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.servicios.ExcepcionServicio;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Autenticación entre servicios: todos los recursos del Core exigen la
 * cabecera X-Api-Key con la clave de api.clave (db.properties, que no se sube
 * al repositorio). Solo la comercializadora la conoce; los clientes finales
 * nunca llaman al Core directamente. Sin clave configurada se rechaza todo.
 *
 * @author carlo
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class FiltroClaveApi implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(FiltroClaveApi.class.getName());
    public static final String CABECERA = "X-Api-Key";
    private static final String ARCHIVO = "/db.properties";

    private final byte[] claveEsperada;

    public FiltroClaveApi() {
        Properties propiedades = new Properties();
        try (InputStream entrada = FiltroClaveApi.class.getResourceAsStream(ARCHIVO)) {
            if (entrada != null) {
                propiedades.load(entrada);
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "No se pudo leer " + ARCHIVO, e);
        }
        String clave = propiedades.getProperty("api.clave", "").trim();
        if (clave.isEmpty()) {
            LOG.severe("api.clave no está configurada: el Core rechazará todas las peticiones");
        }
        claveEsperada = clave.isEmpty() ? null : clave.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * MessageDigest.isEqual compara en tiempo constante: el tiempo de
     * respuesta no revela cuántos caracteres de la clave son correctos.
     */
    @Override
    public void filter(ContainerRequestContext contexto) {
        String recibida = contexto.getHeaderString(CABECERA);
        boolean valida = claveEsperada != null && recibida != null
                && MessageDigest.isEqual(claveEsperada, recibida.getBytes(StandardCharsets.UTF_8));
        if (!valida) {
            contexto.abortWith(MapeadorExcepciones.respuesta(new ExcepcionServicio(CodigoError.AUTH_004),
                    "/" + contexto.getUriInfo().getPath()));
        }
    }
}
