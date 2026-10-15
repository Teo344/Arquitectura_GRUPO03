package ec.edu.monster.servicios;

import ec.edu.monster.modelo.CodigoError;
import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Excepción de negocio. MapeadorExcepciones la convierte en una respuesta
 * HTTP cuyo cuerpo JSON es {@link ErrorServicio} (RQF-010).
 *
 * @author carlo
 */
public class ExcepcionServicio extends Exception {

    /**
     * Cuerpo de la respuesta de error; ManejadorAutenticacion responde el
     * mismo formato cuando rechaza el token.
     */
    @JsonbPropertyOrder({"estado", "codigo", "mensaje"})
    public static class ErrorServicio {

        public String estado = "ERROR";
        public String codigo;
        public String mensaje;

        public ErrorServicio() {
        }

        public ErrorServicio(String codigo, String mensaje) {
            this.codigo = codigo;
            this.mensaje = mensaje;
        }
    }

    private final CodigoError codigoError;

    public ExcepcionServicio(CodigoError codigoError) {
        super(codigoError.getMensaje());
        this.codigoError = codigoError;
    }

    public CodigoError getCodigoError() {
        return codigoError;
    }

    public ErrorServicio getFaultInfo() {
        return new ErrorServicio(codigoError.getCodigo(), codigoError.getMensaje());
    }
}
