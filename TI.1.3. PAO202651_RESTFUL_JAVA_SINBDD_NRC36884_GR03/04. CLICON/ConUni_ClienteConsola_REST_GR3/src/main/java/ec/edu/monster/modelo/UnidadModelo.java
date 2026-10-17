package ec.edu.monster.modelo;

public class UnidadModelo {

    private String codigo;
    private String nombre;
    private String simbolo;

    public UnidadModelo() {
    }

    public UnidadModelo(String codigo, String nombre, String simbolo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.simbolo = simbolo;
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

    public String getSimbolo() {
        return simbolo;
    }

    public void setSimbolo(String simbolo) {
        this.simbolo = simbolo;
    }
}
