package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import java.util.List;

/**
 * Facturas (CU-11): la lista de ventas, con filtro por cédula, una factura con
 * su detalle y, si se pagó a crédito, su tabla de amortización.
 */
public class FacturaControlador {

    private final ClienteRESTMonsterCell clienteRest;

    public FacturaControlador(ClienteRESTMonsterCell clienteRest) {
        this.clienteRest = clienteRest;
    }

    /** Con la cédula vacía lista las últimas facturas de todos los clientes. */
    public List<FacturaModelo> buscar(String texto) {
        String cedula = texto == null ? "" : texto.trim();
        return clienteRest.facturas(cedula.isEmpty() ? cedula : Validacion.cedula(cedula));
    }

    public FacturaModelo abrir(int numFactura) {
        return clienteRest.factura(Validacion.codigo(numFactura, "un número de factura"));
    }

    /** Servicio 4 del banco: la tabla del crédito con que se pagó la factura. */
    public List<CuotaModelo> cuotasDe(FacturaModelo factura) {
        if (!factura.isCredito()) {
            throw new IllegalArgumentException("La factura se pagó en efectivo: no tiene tabla de amortización.");
        }
        return clienteRest.cuotas(factura.getCodCreditoBanco());
    }
}
