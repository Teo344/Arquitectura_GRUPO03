package ec.edu.monster.seguridad;

import ec.edu.monster.controladores.recursos.MapeadorExcepciones;
import ec.edu.monster.servicios.ExcepcionServicio;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

/**
 * Middleware de autenticación (RQF-007). Lee el token de la cabecera
 * "Authorization: Bearer &lt;token&gt;", lo valida con {@link GestorToken} y, si
 * falla, corta la petición con el mismo cuerpo ErrorServicio (codigo, mensaje)
 * que generan los servicios.
 *
 * @author carlo
 */
@Provider
@Protegido
@Priority(Priorities.AUTHENTICATION)
public class ManejadorAutenticacion implements ContainerRequestFilter {

    public static final String PROPIEDAD_USUARIO = "usuario";
    private static final String PREFIJO = "Bearer ";

    @Inject
    private GestorToken gestorToken;

    @Override
    public void filter(ContainerRequestContext contexto) {
        try {
            String usuario = gestorToken.validar(leerToken(contexto));
            contexto.setProperty(PROPIEDAD_USUARIO, usuario);
        } catch (ExcepcionServicio e) {
            contexto.abortWith(MapeadorExcepciones.respuesta(e));
        }
    }

    /**
     * Devuelve el token de la cabecera Authorization o null si no hay.
     */
    private static String leerToken(ContainerRequestContext contexto) {
        String cabecera = contexto.getHeaderString(HttpHeaders.AUTHORIZATION);
        return cabecera != null && cabecera.startsWith(PREFIJO) ? cabecera.substring(PREFIJO.length()) : null;
    }
}
