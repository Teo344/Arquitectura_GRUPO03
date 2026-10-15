package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Elemento del catálogo de categorías.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"codigo", "nombre", "convertible"})
public class CategoriaDto {

    public String codigo;
    public String nombre;
    public Boolean convertible;

    public CategoriaDto() {
    }

    public CategoriaDto(String codigo, String nombre, Boolean convertible) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.convertible = convertible;
    }
}
