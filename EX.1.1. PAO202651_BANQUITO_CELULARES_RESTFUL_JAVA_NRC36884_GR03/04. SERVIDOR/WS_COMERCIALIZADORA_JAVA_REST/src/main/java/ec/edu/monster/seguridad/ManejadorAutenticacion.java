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
 * Middleware de autenticación de los recursos marcados con {@link Protegido}.
 * Lee el token de la cabecera "Authorization: Bearer &lt;token&gt;", lo valida
 * con {@link GestorToken} y deja la {@link Sesion} en la petición para que el
 * recurso sepa qué empleado factura. Si falla, corta la petición con 401 en
 * application/problem+json.
 *
 * @author carlo
 */
@Provider
@Protegido
@Priority(Priorities.AUTHENTICATION)
public class ManejadorAutenticacion implements ContainerRequestFilter {

    /**
     * En un contenedor de servlets las propiedades de la petición JAX-RS son
     * los atributos de la HttpServletRequest: el recurso la lee con
     * getAttribute(PROPIEDAD_SESION).
     */
    public static final String PROPIEDAD_SESION = "sesion";
    private static final String PREFIJO = "Bearer ";

    @Inject
    private GestorToken gestorToken;

    @Override
    public void filter(ContainerRequestContext contexto) {
        try {
            Sesion sesion = gestorToken.validar(leerToken(contexto.getHeaderString(HttpHeaders.AUTHORIZATION)));
            contexto.setProperty(PROPIEDAD_SESION, sesion);
        } catch (ExcepcionServicio e) {
            contexto.abortWith(MapeadorExcepciones.respuesta(e, "/" + contexto.getUriInfo().getPath()));
        }
    }

    /**
     * Devuelve el token de una cabecera Authorization o null si no hay.
     */
    public static String leerToken(String cabecera) {
        return cabecera != null && cabecera.startsWith(PREFIJO) ? cabecera.substring(PREFIJO.length()) : null;
    }
}
