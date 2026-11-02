package ec.edu.monster.controladores.recursos;

import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.vista.ProblemaDto;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convierte toda falla en una respuesta application/problem+json (RFC 9457).
 * El estado HTTP sale del {@link CodigoError}; las fallas de JAX-RS (ruta
 * inexistente, método no permitido) conservan su estado y las no controladas
 * se registran en el log y responden SYS-001 sin exponer detalles internos.
 *
 * @author carlo
 */
@Provider
public class MapeadorExcepciones implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(MapeadorExcepciones.class.getName());

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Throwable error) {
        String instancia = uriInfo != null ? "/" + uriInfo.getPath() : null;
        if (error instanceof ExcepcionServicio negocio) {
            return respuesta(negocio, instancia);
        }
        // JSON mal formado o un valor con el tipo equivocado (texto en "monto")
        if (causadoPorJson(error) || (error instanceof WebApplicationException web
                && web.getResponse().getStatus() == Response.Status.BAD_REQUEST.getStatusCode())) {
            return respuesta(new ExcepcionServicio(CodigoError.DATA_001), instancia);
        }
        if (error instanceof WebApplicationException web) {
            Response.StatusType estado = web.getResponse().getStatusInfo();
            return construir(new ProblemaDto(estado.getReasonPhrase(), estado.getStatusCode(),
                    estado.getReasonPhrase(), instancia, "HTTP-" + estado.getStatusCode()));
        }
        LOG.log(Level.SEVERE, "Error no controlado", error);
        return respuesta(new ExcepcionServicio(CodigoError.SYS_001), instancia);
    }

    /**
     * Respuesta de una ExcepcionServicio; la usa también ManejadorAutenticacion.
     */
    public static Response respuesta(ExcepcionServicio error, String instancia) {
        CodigoError codigo = error.getCodigoError();
        int estado = codigo.getEstadoHttp();
        return construir(new ProblemaDto(Response.Status.fromStatusCode(estado).getReasonPhrase(), estado,
                error.getMessage(), instancia, codigo.getCodigo()));
    }

    private static Response construir(ProblemaDto problema) {
        return Response.status(problema.status).type(ProblemaDto.TIPO_PROBLEMA).entity(problema).build();
    }

    private static boolean causadoPorJson(Throwable error) {
        for (Throwable causa = error; causa != null; causa = causa.getCause()) {
            if (causa instanceof JsonbException) {
                return true;
            }
        }
        return false;
    }
}
