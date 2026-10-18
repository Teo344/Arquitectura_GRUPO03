package ec.edu.monster.modelo;

/** Unidad de medida de una categoría del catálogo. */
public final class Unidad {

    private final String codigo;
    private final String nombre;
    private final String simbolo;
    private final String categoria;

    public Unidad(String codigo, String nombre, String simbolo, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.categoria = categoria;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getCategoria() {
        return categoria;
    }

    @Override
    public String toString() {
        return nombre + " (" + simbolo + ")";
    }
}
