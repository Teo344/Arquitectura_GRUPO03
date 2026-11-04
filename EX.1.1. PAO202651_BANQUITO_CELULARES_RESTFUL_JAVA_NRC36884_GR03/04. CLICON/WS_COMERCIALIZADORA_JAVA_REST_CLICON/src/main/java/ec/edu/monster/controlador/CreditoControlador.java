package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CreditoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import java.util.List;

/**
 * Crédito BanQuito de un comprador (CU-07 y CU-12): si es sujeto de crédito,
 * su monto máximo, sus créditos y la tabla de amortización de cada uno
 * (servicio 4 del banco).
 */
public class CreditoControlador {

    /** Lo que responde el banco sobre una cédula: la evaluación y, si es cliente, sus créditos. */
    public record ConsultaCliente(EvaluacionClienteModelo evaluacion, List<CreditoModelo> creditos) {
    }

    private final ClienteRESTMonsterCell clienteRest;

    public CreditoControlador(ClienteRESTMonsterCell clienteRest) {
        this.clienteRest = clienteRest;
    }

    public ConsultaCliente consultar(String texto) {
        String cedula = Validacion.cedula(texto);
        EvaluacionClienteModelo evaluacion = clienteRest.evaluarCliente(cedula);
        List<CreditoModelo> creditos = evaluacion.isCliente() ? clienteRest.creditos(cedula) : List.of();
        return new ConsultaCliente(evaluacion, creditos);
    }

    /** Solo muestra los créditos del comprador consultado, como las listas de web y escritorio. */
    public List<CuotaModelo> verTabla(ConsultaCliente consulta, int codCredito) {
        boolean esDelComprador = consulta.creditos().stream().anyMatch(c -> c.getCodCredito() == codCredito);
        if (!esDelComprador) {
            throw new IllegalArgumentException("El comprador no tiene el crédito " + codCredito + ".");
        }
        return clienteRest.cuotas(codCredito);
    }
}
