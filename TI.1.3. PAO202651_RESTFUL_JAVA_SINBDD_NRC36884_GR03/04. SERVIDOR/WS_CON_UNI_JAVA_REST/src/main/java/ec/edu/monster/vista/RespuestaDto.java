package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.util.List;

/**
 * Respuesta exitosa de todas las operaciones (RQF-009). Los campos que no
 * aplican a la operación quedan nulos y JSON-B no los incluye en el JSON.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"estado", "codigo", "mensaje", "resultado", "unidadOrigen",
    "unidadDestino", "token", "expiraEn", "categorias", "unidades"})
public class RespuestaDto {

    public String estado;
    public String codigo;
    public String mensaje;
    public Double resultado;
    public String unidadOrigen;
    public String unidadDestino;
    public String token;
    public String expiraEn;
    public List<CategoriaDto> categorias;
    public List<UnidadDto> unidades;
}
