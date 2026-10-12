package ec.edu.monster.modelo;

public enum TipoConversion {

    LONGITUD("Longitud"),
    MASA("Masa"),
    TEMPERATURA("Temperatura");

    private final String nombreVisible;

    TipoConversion(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
