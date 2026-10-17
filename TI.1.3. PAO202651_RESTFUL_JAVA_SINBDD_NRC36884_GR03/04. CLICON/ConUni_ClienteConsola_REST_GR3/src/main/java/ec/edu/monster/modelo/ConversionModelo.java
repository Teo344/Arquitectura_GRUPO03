package ec.edu.monster.modelo;

public class ConversionModelo {

    private TipoConversion tipoConversion;
    private double valor;
    private String unidadInicial;
    private String unidadFinal;

    public ConversionModelo() {
    }

    public ConversionModelo(TipoConversion tipoConversion, double valor, String unidadInicial, String unidadFinal) {
        this.tipoConversion = tipoConversion;
        this.valor = valor;
        this.unidadInicial = unidadInicial;
        this.unidadFinal = unidadFinal;
    }

    public TipoConversion getTipoConversion() {
        return tipoConversion;
    }

    public void setTipoConversion(TipoConversion tipoConversion) {
        this.tipoConversion = tipoConversion;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getUnidadInicial() {
        return unidadInicial;
    }

    public void setUnidadInicial(String unidadInicial) {
        this.unidadInicial = unidadInicial;
    }

    public String getUnidadFinal() {
        return unidadFinal;
    }

    public void setUnidadFinal(String unidadFinal) {
        this.unidadFinal = unidadFinal;
    }
}
