package ec.edu.monster.servicios;

import ec.edu.monster.modelo.CodigoError;

/**
 * Excepción de negocio. MapeadorExcepciones la convierte en una respuesta
 * application/problem+json con el estado HTTP de su {@link CodigoError}.
 * detalle reemplaza al mensaje genérico cuando hay algo más concreto que
 * decir (por ejemplo, qué reglas de crédito no se cumplen).
 *
 * @author carlo
 */
public class ExcepcionServicio extends Exception {

    private final CodigoError codigoError;

    public ExcepcionServicio(CodigoError codigoError) {
        super(codigoError.getMensaje());
        this.codigoError = codigoError;
    }

    public ExcepcionServicio(CodigoError codigoError, String detalle) {
        super(detalle);
        this.codigoError = codigoError;
    }

    public ExcepcionServicio(CodigoError codigoError, Throwable causa) {
        super(codigoError.getMensaje(), causa);
        this.codigoError = codigoError;
    }

    public CodigoError getCodigoError() {
        return codigoError;
    }
}
