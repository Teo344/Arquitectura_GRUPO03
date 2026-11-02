package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta de GET /clientes/sujeto-credito?cedula=: la evaluación del banco
 * (servicio 1) y su monto máximo (servicio 2). codCliente, nombre y
 * montoMaximo faltan si la cédula no es de un cliente.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"cedula", "codCliente", "nombre", "sujetoCredito", "motivos", "montoMaximo"})
public class EvaluacionClienteDto {

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
    public BigDecimal montoMaximo;
}
