package ec.edu.monster.modelo;

public record Categoria(String codigo, String nombre, boolean convertible) {
    @Override public String toString() { return nombre; }
}
