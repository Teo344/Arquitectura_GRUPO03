package ec.edu.monster.controladores.recursos;

import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ExcepcionServicio.ErrorServicio;
import jakarta.json.bind.JsonbException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convierte toda falla en una respuesta JSON con ErrorServicio (RNF-009):
 * errores de sesión y de credenciales salen con 401, fallas de la base de
 * datos (SYS-001) con 500 y los demás errores de datos o de negocio con 400.
 *
 * @author carlo
 */
@Provider
public class MapeadorExcepciones implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(MapeadorExcepciones.class.getName());

    @Override
    public Response toResponse(Throwable error) {
        if (error instanceof ExcepcionServicio negocio) {
            return respuesta(negocio);
        }
        // JSON mal formado o un valor que no es número en "importe"
        if (causadoPorJson(error) || (error instanceof WebApplicationException web
                && web.getResponse().getStatus() == Response.Status.BAD_REQUEST.getStatusCode())) {
            return respuesta(new ExcepcionServicio(CodigoError.DATA_001));
        }
        // Ruta inexistente, método no permitido, etc.: también estructurado
        if (error instanceof WebApplicationException web) {
            Response.StatusType estado = web.getResponse().getStatusInfo();
            return construir(estado.getStatusCode(),
                    new ErrorServicio("HTTP-" + estado.getStatusCode(), estado.getReasonPhrase()));
        }
        LOG.log(Level.SEVERE, "Error no controlado", error);
        return respuesta(new ExcepcionServicio(CodigoError.SYS_001));
    }

    /**
     * Respuesta de una ExcepcionServicio; la usa también ManejadorAutenticacion.
     */
    public static Response respuesta(ExcepcionServicio error) {
        CodigoError codigo = error.getCodigoError();
        Response.Status estado;
        if (codigo.getCodigo().startsWith("AUTH")) {
            estado = Response.Status.UNAUTHORIZED;
        } else if (codigo == CodigoError.SYS_001) {
            estado = Response.Status.INTERNAL_SERVER_ERROR;
        } else {
            estado = Response.Status.BAD_REQUEST;
        }
        return construir(estado.getStatusCode(), error.getFaultInfo());
    }

    private static Response construir(int estado, ErrorServicio cuerpo) {
        return Response.status(estado).type(MediaType.APPLICATION_JSON).entity(cuerpo).build();
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
