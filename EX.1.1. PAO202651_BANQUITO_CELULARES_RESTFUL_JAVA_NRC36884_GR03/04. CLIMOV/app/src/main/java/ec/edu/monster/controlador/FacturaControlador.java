package ec.edu.monster.controlador;

import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.servicio.CreditoRestService;
import ec.edu.monster.servicio.FacturaRestService;
import ec.edu.monster.vista.FacturasActivity;

/**
 * Facturas (CU-11): la lista de ventas, con filtro por cédula, una factura con
 * su detalle y, si se pagó a crédito, su tabla de amortización.
 */
public class FacturaControlador extends ControladorBase {

    private final FacturasActivity vista;
    private final FacturaRestService facturas;
    private final CreditoRestService creditos;
    private final SesionModelo sesion;

    public FacturaControlador(FacturasActivity vista, FacturaRestService facturas, CreditoRestService creditos,
            SesionModelo sesion) {
        this.vista = vista;
        this.facturas = facturas;
        this.creditos = creditos;
        this.sesion = sesion;
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
        ejecutar(vista, () -> facturas.facturas(sesion.getToken(), cedula),
                lista -> vista.mostrarFacturas(cedula, lista));
    }

    public void abrir(int numFactura) {
        ejecutar(vista, () -> facturas.factura(sesion.getToken(), numFactura), vista::mostrarFactura);
    }

    /** Servicio 4 del banco: la tabla del crédito con que se pagó la factura. */
    public void verTabla(FacturaModelo factura) {
        ejecutar(vista, () -> creditos.cuotas(sesion.getToken(), factura.getCodCreditoBanco()), vista::mostrarTabla);
    }
}
