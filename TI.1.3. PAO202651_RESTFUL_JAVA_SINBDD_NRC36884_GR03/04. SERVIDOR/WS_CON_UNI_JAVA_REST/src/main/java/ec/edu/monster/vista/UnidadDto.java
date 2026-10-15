package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;

/**
 * Elemento de la lista de unidades. No expone el factor de conversión.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"codigo", "nombre", "simbolo", "categoria"})
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
