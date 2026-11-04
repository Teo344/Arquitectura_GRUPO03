package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.CreditoRestService;
import ec.edu.monster.vista.SimuladorActivity;
import java.math.BigDecimal;

/** Simulador de cuotas (CU-08): la tabla de amortización de un monto sin otorgar el crédito. */
public class SimuladorControlador extends ControladorBase {

    private final SimuladorActivity vista;
    private final CreditoRestService creditos;
    private final SesionModelo sesion;

    public SimuladorControlador(SimuladorActivity vista, CreditoRestService creditos, SesionModelo sesion) {
        this.vista = vista;
        this.creditos = creditos;
        this.sesion = sesion;
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
        ejecutar(vista, () -> creditos.simular(sesion.getToken(), monto, numCuotas), vista::mostrarTabla);
    }
}
