package ec.edu.monster.vista;

import ec.edu.monster.controlador.VentaControlador;
import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.util.EntradaUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Punto de venta (CU-07, CU-09 y CU-10) en los 4 pasos de los clientes web y de
 * escritorio: comprador, teléfonos, forma de pago y factura. La venta se
 * conserva al volver al menú principal.
 */
public class VentaVista {

    private final MensajesVista mensajes = new MensajesVista();
    private final ComponentesTienda componentes = new ComponentesTienda();
    private final VentaControlador ventaControlador;

    public VentaVista(VentaControlador ventaControlador) {
        this.ventaControlador = ventaControlador;
    }

    /** Las unidades de la venta en curso, junto a "Nueva venta" en el menú principal. */
    public String textoVentaEnCurso() {
        int unidades = ventaControlador.getCarrito().getArticulos();
        return unidades == 0 ? "" : " (en curso: " + textoUnidades(unidades) + ")";
    }

    public void ejecutar() {
        while (true) {
            mostrarEstado();
            mensajes.mostrarMensaje("  Paso 1. Comprador");
            mensajes.mostrarMensaje("    1. Verificar comprador en BanQuito");
            mensajes.mostrarMensaje("  Paso 2. Telefonos");
            mensajes.mostrarMensaje("    2. Agregar telefono");
            mensajes.mostrarMensaje("    3. Cambiar cantidad");
            mensajes.mostrarMensaje("    4. Quitar telefono");
            mensajes.mostrarMensaje("  Paso 3. Forma de pago");
            mensajes.mostrarMensaje("    5. Elegir efectivo o credito BanQuito");
            mensajes.mostrarMensaje("  Paso 4. Factura");
            mensajes.mostrarMensaje("    6. Facturar");
            mensajes.mostrarMensaje("  Venta");
            mensajes.mostrarMensaje("    7. Vaciar venta");
            mensajes.mostrarMensaje("    0. Volver al menu principal");

            int opcion = EntradaUtil.leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1 -> verificarComprador();
                    case 2 -> agregarTelefono();
                    case 3 -> cambiarCantidad();
                    case 4 -> quitarTelefono();
                    case 5 -> elegirFormaPago();
                    case 6 -> facturar();
                    case 7 -> vaciar();
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

    /** Resumen de la venta en curso y lo que falta para poder facturar. */
    private void mostrarEstado() {
        CarritoModelo carrito = ventaControlador.getCarrito();
        mensajes.mostrarTitulo("NUEVA VENTA");
        mensajes.mostrarRecuadro(List.of(
                "Comprador     : " + textoComprador(carrito.getEvaluacion()),
                "Telefonos     : " + textoUnidades(carrito.getArticulos()),
                "Forma de pago : " + textoFormaPago(carrito),
                "Total         : " + Formato.importe(carrito.getTotal())));
        String motivo = ventaControlador.motivoParaNoFacturar();
        mensajes.mostrarMensaje(motivo == null ? "  Lista para facturar." : "  Pendiente: " + motivo);
    }

    private void verificarComprador() {
        mensajes.mostrarTitulo("PASO 1: VERIFICAR COMPRADOR");
        String cedula = EntradaUtil.leerTextoNoVacio("Cedula del comprador (10 digitos): ");
        componentes.tarjetaEvaluacion(ventaControlador.verificarComprador(cedula));
    }

    private void agregarTelefono() {
        mensajes.mostrarTitulo("PASO 2: AGREGAR TELEFONO");
        VentaControlador.CatalogoVenta catalogo = ventaControlador.cargarCatalogo();
        avisarRetirados(catalogo.retirados());
        componentes.catalogo(catalogo.telefonos());
        if (catalogo.telefonos().isEmpty()) {
            return;
        }
        int codigo = EntradaUtil.leerEntero("Codigo del telefono: ");
        int cantidad = EntradaUtil.leerEntero("Cantidad: ");
        TelefonoModelo telefono = ventaControlador.agregar(codigo, cantidad);
        mensajes.mostrarExito(telefono.getNombre() + " se agrego a la venta.");
        componentes.carrito(ventaControlador.getCarrito());
    }

    private void cambiarCantidad() {
        mensajes.mostrarTitulo("PASO 2: CAMBIAR CANTIDAD");
        CarritoModelo carrito = ventaControlador.getCarrito();
        componentes.carrito(carrito);
        if (carrito.isVacio()) {
            return;
        }
        int codigo = EntradaUtil.leerEntero("Codigo del telefono: ");
        int cantidad = EntradaUtil.leerEntero("Nueva cantidad (0 lo quita de la venta): ");
        ventaControlador.cambiarCantidad(codigo, cantidad);
        mensajes.mostrarExito("Venta actualizada.");
        componentes.carrito(carrito);
    }

    private void quitarTelefono() {
        mensajes.mostrarTitulo("PASO 2: QUITAR TELEFONO");
        CarritoModelo carrito = ventaControlador.getCarrito();
        componentes.carrito(carrito);
        if (carrito.isVacio()) {
            return;
        }
        ventaControlador.quitar(EntradaUtil.leerEntero("Codigo del telefono: "));
        mensajes.mostrarExito("Telefono quitado de la venta.");
        componentes.carrito(carrito);
    }

    private void elegirFormaPago() {
        mensajes.mostrarTitulo("PASO 3: FORMA DE PAGO");
        mensajes.mostrarMensaje("  1. Efectivo (42 % de descuento)");
        mensajes.mostrarMensaje("  2. Credito BanQuito (3 a 18 cuotas mensuales)");
        int opcion = EntradaUtil.leerEntero("Seleccione una opcion: ");
        switch (opcion) {
            case 1 -> {
                ventaControlador.cambiarFormaPago(FormaPago.EFECTIVO);
                mensajes.mostrarExito("Forma de pago: efectivo.");
            }
            case 2 -> elegirCredito();
            default -> mensajes.mostrarError("Opcion no valida. La forma de pago no cambio.");
        }
    }

    private void elegirCredito() {
        ventaControlador.cambiarCuotas(EntradaUtil.leerEntero("Numero de cuotas (3 a 18): "));
        ventaControlador.cambiarFormaPago(FormaPago.CREDITO);
        CarritoModelo carrito = ventaControlador.getCarrito();
        mensajes.mostrarExito("Forma de pago: " + textoFormaPago(carrito) + ".");
        if (!carrito.isVacio()) {
            componentes.amortizacion("TABLA ESTIMADA DE LA VENTA", ventaControlador.simularCuotas());
        }
    }

    private void facturar() {
        mensajes.mostrarTitulo("PASO 4: FACTURAR");
        // Precios editados o teléfonos dados de baja desde que se agregaron a la venta
        avisarRetirados(ventaControlador.cargarCatalogo().retirados());
        ventaControlador.validarParaFacturar();

        CarritoModelo carrito = ventaControlador.getCarrito();
        componentes.carrito(carrito);
        mensajes.mostrarRecuadro(resumen(carrito));
        mensajes.mostrarMensaje(carrito.isCredito()
                ? "BanQuito otorgara el credito por el total y se emitira la factura."
                : "Se emitira la factura con el descuento por pago en efectivo.");
        if (!EntradaUtil.leerConfirmacion("Confirma la venta?")) {
            mensajes.mostrarMensaje("Venta sin facturar. Sigue en curso.");
            return;
        }

        FacturaModelo factura = ventaControlador.facturar();
        mensajes.mostrarExito("Factura No. " + factura.getNumeroVisible() + " registrada"
                + (factura.isCredito() ? " con el credito BanQuito No. " + factura.getCodCreditoBanco() : "") + ".");
        componentes.comprobante(factura);
        if (factura.isCredito()) {
            componentes.amortizacion("TABLA DE AMORTIZACION  |  CREDITO No. " + factura.getCodCreditoBanco(),
                    ventaControlador.cuotasDe(factura));
        }
    }

    private void vaciar() {
        if (EntradaUtil.leerConfirmacion("Vaciar la venta en curso?")) {
            ventaControlador.vaciar();
            mensajes.mostrarExito("Venta vaciada.");
        }
    }

    /** Filas de la confirmación; la última es el total a facturar. */
    private List<String> resumen(CarritoModelo carrito) {
        List<String> filas = new ArrayList<>();
        filas.add("Comprador        : " + Formato.valorO(carrito.getEvaluacion().getNombre()));
        filas.add("Cedula           : " + carrito.getCedula());
        filas.add("Telefonos        : " + textoUnidades(carrito.getArticulos()));
        filas.add("Forma de pago    : " + textoFormaPago(carrito));
        if (carrito.isCredito()) {
            BigDecimal cuota = ventaControlador.cuotaEstimada();
            if (cuota != null) {
                filas.add("Cuota mensual    : " + Formato.importe(cuota));
            }
        } else {
            filas.add("Descuento        : - " + Formato.importe(carrito.getDescuento()));
        }
        filas.add("Total a facturar : " + Formato.importe(carrito.getTotal()));
        return filas;
    }

    private void avisarRetirados(List<String> retirados) {
        if (!retirados.isEmpty()) {
            mensajes.mostrarError("Se quitaron de la venta porque ya no estan a la venta: "
                    + String.join(", ", retirados) + ".");
        }
    }

    private static String textoComprador(EvaluacionClienteModelo evaluacion) {
        if (evaluacion == null) {
            return "Sin verificar";
        }
        return evaluacion.isCliente() ? Formato.valorO(evaluacion.getNombre()) : "No es cliente de BanQuito";
    }

    private static String textoFormaPago(CarritoModelo carrito) {
        return carrito.isCredito()
                ? FormaPago.CREDITO.getNombreVisible() + " a " + carrito.getNumCuotas() + " cuotas"
                : FormaPago.EFECTIVO.getNombreVisible() + " (42 % de descuento)";
    }

    private static String textoUnidades(int unidades) {
        return unidades + (unidades == 1 ? " unidad" : " unidades");
    }
}
