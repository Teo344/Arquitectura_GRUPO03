package ec.edu.monster.vista;

import ec.edu.monster.controlador.FacturaControlador;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.util.EntradaUtil;
import java.util.ArrayList;
import java.util.List;

/** Facturas emitidas (CU-11), con filtro por cédula, detalle y tabla de amortización si son a crédito. */
public class FacturasVista {

    private static final String FILA_FACTURA = "%6s  %-16s  %-28s  %-16s  %12s";

    private final MensajesVista mensajes = new MensajesVista();
    private final ComponentesTienda componentes = new ComponentesTienda();
    private final FacturaControlador facturaControlador;

    public FacturasVista(FacturaControlador facturaControlador) {
        this.facturaControlador = facturaControlador;
    }

    public void ejecutar() {
        while (true) {
            mensajes.mostrarTitulo("FACTURAS");
            mensajes.mostrarMensaje("    1. Listar facturas");
            mensajes.mostrarMensaje("    2. Ver una factura");
            mensajes.mostrarMensaje("    0. Volver al menu principal");

            int opcion = EntradaUtil.leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1 -> listar();
                    case 2 -> verFactura();
                    case 0 -> {
                        return;
                    }
                    default -> mensajes.mostrarError("Opcion no valida. Intente nuevamente.");
                }
            } catch (ErrorServicioModelo ex) {
                // AUTH-003 lo atiende el menú principal, que vuelve al inicio de sesión
                if (ex.requiereNuevoLogin()) {
                    throw ex;
                }
                mensajes.mostrarErrorServicio(ex);
            } catch (IllegalArgumentException ex) {
                mensajes.mostrarError(ex.getMessage());
            }
        }
    }

    private void listar() {
        mensajes.mostrarTitulo("FACTURAS EMITIDAS");
        String cedula = EntradaUtil.leerTexto("Cedula del comprador (Enter para ver las ultimas de todos): ");
        List<FacturaModelo> facturas = facturaControlador.buscar(cedula);
        if (facturas.isEmpty()) {
            mensajes.mostrarMensaje(cedula.isEmpty() ? "Todavia no se ha registrado ninguna venta."
                    : "El comprador con cedula " + cedula + " no tiene facturas.");
            return;
        }
        List<Object[]> filas = new ArrayList<>();
        for (FacturaModelo factura : facturas) {
            filas.add(new Object[]{factura.getNumeroVisible(), Formato.fecha(factura.getFecha()),
                ComponentesTienda.recortar(factura.getNombreCliente(), 28), factura.getFormaPago().getNombreVisible(),
                Formato.importe(factura.getTotal())});
        }
        componentes.tabla(FILA_FACTURA, new Object[]{"No.", "Fecha", "Comprador", "Forma de pago", "Total"}, filas);
        mensajes.mostrarMensaje("Total de facturas: " + facturas.size());
    }

    private void verFactura() {
        mensajes.mostrarTitulo("VER FACTURA");
        FacturaModelo factura = facturaControlador.abrir(EntradaUtil.leerEntero("Numero de factura: "));
        componentes.comprobante(factura);
        if (factura.isCredito() && EntradaUtil.leerConfirmacion("Ver la tabla de amortizacion?")) {
            componentes.amortizacion("TABLA DE AMORTIZACION  |  CREDITO No. " + factura.getCodCreditoBanco(),
                    facturaControlador.cuotasDe(factura));
        }
    }
}
