package ec.edu.monster.modelo;

/** Categoría del catálogo de unidades (LONGITUD, MASA, TEMPERATURA, ...). */
public final class Categoria {

    private final String codigo;
    private final String nombre;
    private final boolean convertible;

    public Categoria(String codigo, String nombre, boolean convertible) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.convertible = convertible;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isConvertible() {
        return convertible;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
