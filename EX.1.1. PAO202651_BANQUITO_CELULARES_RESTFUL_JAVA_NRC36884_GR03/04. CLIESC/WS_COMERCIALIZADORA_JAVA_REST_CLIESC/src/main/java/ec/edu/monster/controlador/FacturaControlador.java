package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.FacturasVista;

/**
 * Facturas (CU-11): la lista de ventas, con filtro por cédula, y una factura
 * con su detalle lista para imprimir.
 */
public class FacturaControlador extends ControladorBase {

    private final FacturasVista vista;
    private final ClienteRESTMonsterCell clienteRest;

    public FacturaControlador(FacturasVista vista, ClienteRESTMonsterCell clienteRest) {
        this.vista = vista;
        this.clienteRest = clienteRest;
    }

    /** Con la cédula vacía lista las últimas facturas de todos los clientes. */
    public void buscar(String texto) {
        String cedula = texto == null ? "" : texto.trim();
        if (!cedula.isEmpty()) {
            try {
                Validacion.cedula(cedula);
            } catch (IllegalArgumentException ex) {
                vista.mostrarMensaje(ex.getMessage(), true);
                return;
            }
        }
        ejecutar(vista, () -> clienteRest.facturas(cedula), facturas -> vista.mostrarFacturas(cedula, facturas));
    }

    public void abrir(int numFactura) {
        ejecutar(vista, () -> clienteRest.factura(numFactura), vista::mostrarFactura);
    }
}
