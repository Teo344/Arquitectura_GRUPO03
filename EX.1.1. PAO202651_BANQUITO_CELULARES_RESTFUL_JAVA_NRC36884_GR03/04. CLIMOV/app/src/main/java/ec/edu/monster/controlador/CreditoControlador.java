package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CreditoModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.CreditoRestService;
import ec.edu.monster.vista.ClientesActivity;
import java.util.Collections;
import java.util.List;

/**
 * Crédito BanQuito de un comprador (CU-07 y CU-12): si es sujeto de crédito,
 * su monto máximo, sus créditos y la tabla de amortización de cada uno
 * (servicio 4 del banco).
 */
public class CreditoControlador extends ControladorBase {

    /** Lo que responde el banco sobre una cédula: la evaluación y, si es cliente, sus créditos. */
    public static final class ConsultaCliente {

        private final EvaluacionClienteModelo evaluacion;
        private final List<CreditoModelo> creditos;

        ConsultaCliente(EvaluacionClienteModelo evaluacion, List<CreditoModelo> creditos) {
            this.evaluacion = evaluacion;
            this.creditos = creditos;
        }

        public EvaluacionClienteModelo getEvaluacion() {
            return evaluacion;
        }

        public List<CreditoModelo> getCreditos() {
            return creditos;
        }
    }

    private final ClientesActivity vista;
    private final CreditoRestService creditos;
    private final SesionModelo sesion;

    public CreditoControlador(ClientesActivity vista, CreditoRestService creditos, SesionModelo sesion) {
        this.vista = vista;
        this.creditos = creditos;
        this.sesion = sesion;
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
            EvaluacionClienteModelo evaluacion = creditos.evaluarCliente(sesion.getToken(), cedula);
            List<CreditoModelo> lista = evaluacion.isCliente()
                    ? creditos.creditos(sesion.getToken(), cedula) : Collections.emptyList();
            return new ConsultaCliente(evaluacion, lista);
        }, vista::mostrarCliente);
    }

    public void verTabla(int codCredito) {
        ejecutar(vista, () -> creditos.cuotas(sesion.getToken(), codCredito),
                cuotas -> vista.mostrarTabla(codCredito, cuotas));
    }
}
