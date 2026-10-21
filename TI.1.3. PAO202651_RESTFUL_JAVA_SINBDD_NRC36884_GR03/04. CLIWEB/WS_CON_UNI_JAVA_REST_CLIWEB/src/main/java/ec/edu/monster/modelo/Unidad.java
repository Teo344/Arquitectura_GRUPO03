package ec.edu.monster.modelo;

/** Unidad de una categoría. Los getters existen para que el EL de las JSP pueda leer el record. */
public record Unidad(String codigo, String nombre, String simbolo, String categoria) {
    @Override public String toString() { return nombre + " (" + simbolo + ")"; }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getSimbolo() { return simbolo; }
    public String getCategoria() { return categoria; }
}
