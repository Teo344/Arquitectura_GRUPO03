package ec.edu.monster.modelo;

public class CategoriaModelo {

    private String codigo;
    private String nombre;
    private boolean convertible;

    public CategoriaModelo() {
    }

    public CategoriaModelo(String codigo, String nombre, boolean convertible) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.convertible = convertible;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isConvertible() {
        return convertible;
    }

    public void setConvertible(boolean convertible) {
        this.convertible = convertible;
    }
}
