package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CreditoModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.ClientesVista;
import java.util.List;

/**
 * Crédito BanQuito de un comprador (CU-07 y CU-12): si es sujeto de crédito,
 * su monto máximo, sus créditos y la tabla de amortización de cada uno
 * (servicio 4 del banco).
 */
public class CreditoControlador extends ControladorBase {

    /** Lo que responde el banco sobre una cédula: la evaluación y, si es cliente, sus créditos. */
    public record ConsultaCliente(EvaluacionClienteModelo evaluacion, List<CreditoModelo> creditos) {
    }

    private final ClientesVista vista;
    private final ClienteRESTMonsterCell clienteRest;

    public CreditoControlador(ClientesVista vista, ClienteRESTMonsterCell clienteRest) {
        this.vista = vista;
        this.clienteRest = clienteRest;
    }

    public void consultar(String texto) {
        final String cedula;
        try {
            cedula = Validacion.cedula(texto);
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
            return;
        }
        ejecutar(vista, () -> {
            EvaluacionClienteModelo evaluacion = clienteRest.evaluarCliente(cedula);
            List<CreditoModelo> creditos = evaluacion.isCliente() ? clienteRest.creditos(cedula) : List.of();
            return new ConsultaCliente(evaluacion, creditos);
        }, vista::mostrarCliente);
    }

    public void verTabla(int codCredito) {
        ejecutar(vista, () -> clienteRest.cuotas(codCredito), cuotas -> vista.mostrarTabla(codCredito, cuotas));
    }
}
