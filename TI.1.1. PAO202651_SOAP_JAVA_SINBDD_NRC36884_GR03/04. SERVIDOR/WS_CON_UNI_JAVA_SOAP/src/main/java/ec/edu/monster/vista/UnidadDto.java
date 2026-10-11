package ec.edu.monster.vista;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Elemento de la lista de unidades. No expone el factor de conversión.
 *
 * @author carlo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "unidad", propOrder = {"codigo", "nombre", "simbolo", "categoria"})
public class UnidadDto {

    public String codigo;
    public String nombre;
    public String simbolo;
    public String categoria;

    public UnidadDto() {
    }

    public UnidadDto(String codigo, String nombre, String simbolo, String categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.simbolo = simbolo;
        this.categoria = categoria;
    }
}
