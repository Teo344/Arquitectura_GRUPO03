package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.util.List;

/**
 * Respuesta del servicio 1 (¿es sujeto de crédito?). codCliente y nombre
 * faltan cuando la cédula no es de un cliente; motivos está vacío cuando sí
 * es sujeto de crédito.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"cedula", "codCliente", "nombre", "sujetoCredito", "motivos"})
public class EvaluacionCreditoDto {

    @JsonbPropertyOrder({"codigo", "descripcion"})
    public static class MotivoDto {

        public String codigo;
        public String descripcion;
    }

    public String cedula;
    public Integer codCliente;
    public String nombre;
    public boolean sujetoCredito;
    public List<MotivoDto> motivos;
}
