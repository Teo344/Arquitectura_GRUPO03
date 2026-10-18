package ec.edu.monster.modelo;

public record Unidad(String codigo, String nombre, String simbolo, String categoria) {
    @Override public String toString() { return nombre + " (" + simbolo + ")"; }
}
