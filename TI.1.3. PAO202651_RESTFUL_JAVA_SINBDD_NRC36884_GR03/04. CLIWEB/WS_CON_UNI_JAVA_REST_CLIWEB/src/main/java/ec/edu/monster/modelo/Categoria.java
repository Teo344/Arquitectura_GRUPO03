package ec.edu.monster.modelo;

/** Categoría del catálogo. Los getters existen para que el EL de las JSP pueda leer el record. */
public record Categoria(String codigo, String nombre, boolean convertible) {
    @Override public String toString() { return nombre; }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public boolean isConvertible() { return convertible; }
}
