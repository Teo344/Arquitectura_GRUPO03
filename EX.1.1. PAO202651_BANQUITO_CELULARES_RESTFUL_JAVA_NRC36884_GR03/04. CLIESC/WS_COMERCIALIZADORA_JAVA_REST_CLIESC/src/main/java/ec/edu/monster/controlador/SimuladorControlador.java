package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.SimuladorVista;
import java.math.BigDecimal;

/** Simulador de cuotas (CU-08): la tabla de amortización de un monto sin otorgar el crédito. */
public class SimuladorControlador extends ControladorBase {

    private final SimuladorVista vista;
    private final ClienteRESTMonsterCell clienteRest;

    public SimuladorControlador(SimuladorVista vista, ClienteRESTMonsterCell clienteRest) {
        this.vista = vista;
        this.clienteRest = clienteRest;
    }

    public void simular(String textoMonto, int numCuotas) {
        final BigDecimal monto;
        try {
            monto = Validacion.importe(textoMonto, "monto");
            if (numCuotas < CarritoModelo.CUOTAS_MINIMO || numCuotas > CarritoModelo.CUOTAS_MAXIMO) {
                throw new IllegalArgumentException("Elige entre 3 y 18 cuotas.");
            }
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
            return;
        }
        ejecutar(vista, () -> clienteRest.simular(monto, numCuotas), vista::mostrarTabla);
    }
}
