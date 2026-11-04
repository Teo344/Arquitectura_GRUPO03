package ec.edu.monster.vista;

import ec.edu.monster.controlador.SimuladorControlador;
import ec.edu.monster.util.EntradaUtil;

/** Simulador de cuotas (CU-08): la tabla de amortización de un monto sin otorgar el crédito. */
public class SimuladorVista {

    private final MensajesVista mensajes = new MensajesVista();
    private final ComponentesTienda componentes = new ComponentesTienda();
    private final SimuladorControlador simuladorControlador;

    public SimuladorVista(SimuladorControlador simuladorControlador) {
        this.simuladorControlador = simuladorControlador;
    }

    public void simular() {
        mensajes.mostrarTitulo("SIMULADOR DE CUOTAS");
        String monto = EntradaUtil.leerTextoNoVacio("Monto a financiar (por ejemplo 450.00): ");
        int cuotas = EntradaUtil.leerEntero("Numero de cuotas (3 a 18): ");
        componentes.amortizacion("TABLA SIMULADA (NO SE OTORGA EL CREDITO)", simuladorControlador.simular(monto, cuotas));
    }
}
