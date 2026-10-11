package ec.edu.monster.vista;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Elemento del catálogo de categorías.
 *
 * @author carlo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "categoria", propOrder = {"codigo", "nombre", "convertible"})
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
