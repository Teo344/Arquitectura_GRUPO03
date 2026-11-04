package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import java.math.BigDecimal;
import java.util.List;

/** Simulador de cuotas (CU-08): la tabla de amortización de un monto sin otorgar el crédito. */
public class SimuladorControlador {

    private final ClienteRESTMonsterCell clienteRest;

    public SimuladorControlador(ClienteRESTMonsterCell clienteRest) {
        this.clienteRest = clienteRest;
    }

    public List<CuotaModelo> simular(String textoMonto, int numCuotas) {
        BigDecimal monto = Validacion.importe(textoMonto, "monto");
        if (numCuotas < CarritoModelo.CUOTAS_MINIMO || numCuotas > CarritoModelo.CUOTAS_MAXIMO) {
            throw new IllegalArgumentException("Elige entre 3 y 18 cuotas.");
        }
        return clienteRest.simular(monto, numCuotas);
    }
}
