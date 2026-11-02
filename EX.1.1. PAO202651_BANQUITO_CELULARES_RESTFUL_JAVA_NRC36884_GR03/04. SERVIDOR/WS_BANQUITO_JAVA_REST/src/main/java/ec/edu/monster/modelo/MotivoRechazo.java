package ec.edu.monster.modelo;

/**
 * Las 4 reglas del sujeto de crédito; cada valor es la regla que no se
 * cumple.
 *
 * @author carlo
 */
public enum MotivoRechazo {

    NO_ES_CLIENTE("No es cliente del banco"),
    SIN_DEPOSITO_ULTIMO_MES("No tiene depósitos en el último mes"),
    HOMBRE_MENOR_25("Es hombre menor de 25 años"),
    CREDITO_ACTIVO("Tiene un crédito activo");

    private final String descripcion;

    MotivoRechazo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
