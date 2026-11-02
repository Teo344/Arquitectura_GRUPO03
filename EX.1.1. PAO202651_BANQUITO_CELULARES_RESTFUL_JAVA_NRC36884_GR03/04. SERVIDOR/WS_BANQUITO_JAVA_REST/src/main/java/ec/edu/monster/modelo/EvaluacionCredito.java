package ec.edu.monster.modelo;

import java.util.List;

/**
 * Resultado de evaluar una cédula como sujeto de crédito. cliente es null si
 * la cédula no es de un cliente; motivos vacío significa que sí es sujeto de
 * crédito.
 *
 * @author carlo
 */
public record EvaluacionCredito(String cedula, Cliente cliente, List<MotivoRechazo> motivos) {

    public boolean sujetoCredito() {
        return motivos.isEmpty();
    }
}
