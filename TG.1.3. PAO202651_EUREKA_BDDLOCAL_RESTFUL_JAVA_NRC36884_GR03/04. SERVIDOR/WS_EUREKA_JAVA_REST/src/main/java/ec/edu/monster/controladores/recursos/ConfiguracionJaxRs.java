package ec.edu.monster.controladores.recursos;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Activa JAX-RS: todos los recursos quedan bajo /api.
 *
 * @author carlo
 */
@ApplicationPath("api")
public class ConfiguracionJaxRs extends Application {
}
