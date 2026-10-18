package ec.edu.monster.modelo;

/** Resultado que devuelve el servidor para una conversión. */
public final class ResultadoConversion {

    private final double resultado;
    private final String unidadOrigen;
    private final String unidadDestino;

    public ResultadoConversion(double resultado, String unidadOrigen, String unidadDestino) {
        this.resultado = resultado;
        this.unidadOrigen = unidadOrigen;
        this.unidadDestino = unidadDestino;
    }

    public double getResultado() {
        return resultado;
    }

    public String getUnidadOrigen() {
        return unidadOrigen;
    }

    public String getUnidadDestino() {
        return unidadDestino;
    }
}
