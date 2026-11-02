package ec.edu.monster.controladores.recursos;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Activa JAX-RS: todos los recursos quedan bajo /api/v1. La versión en la
 * ruta permite publicar una v2 sin romper a la comercializadora.
 *
 * @author carlo
 */
@ApplicationPath("api/v1")
public class ConfiguracionJaxRs extends Application {
}
