package ec.edu.monster.vista;

import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.DetalleFacturaModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.LineaCarrito;
import ec.edu.monster.modelo.TelefonoModelo;
import java.util.ArrayList;
import java.util.List;

/**
 * Los componentes de la tienda en modo texto, los mismos de los clientes web y
 * de escritorio: catálogo, carrito, evaluación del banco, factura y tabla de
 * amortización. Solo reciben datos del modelo y los imprimen.
 */
public class ComponentesTienda {

    private static final String FILA_CATALOGO = "%6s  %-34s  %12s  %12s";
    private static final String FILA_CARRITO = "%6s  %-30s  %8s  %12s  %12s";
    private static final String FILA_DETALLE = "%-34s  %8s  %15s  %12s";
    private static final String FILA_AMORTIZACION = "%7s  %12s  %14s  %14s  %12s";

    private final MensajesVista mensajes = new MensajesVista();

    /** Tabla con encabezado y filas separadas por líneas; todas las filas comparten el formato. */
    public void tabla(String formato, Object[] columnas, List<Object[]> filas) {
        String encabezado = String.format(formato, columnas);
        mensajes.mostrarMensaje(encabezado);
        mensajes.mostrarMensaje("-".repeat(encabezado.length()));
        for (Object[] fila : filas) {
            mensajes.mostrarMensaje(String.format(formato, fila));
        }
        mensajes.mostrarMensaje("-".repeat(encabezado.length()));
    }

    /** Corta los textos largos para que las columnas no se desalineen. */
    public static String recortar(String texto, int ancho) {
        String valor = Formato.valorO(texto);
        return valor.length() <= ancho ? valor : valor.substring(0, ancho - 3) + "...";
    }

    // ---------------------------------------------------------------- Catálogo

    public void catalogo(List<TelefonoModelo> telefonos) {
        if (telefonos.isEmpty()) {
            mensajes.mostrarMensaje("El catalogo esta vacio.");
            return;
        }
        List<Object[]> filas = new ArrayList<>();
        for (TelefonoModelo telefono : telefonos) {
            filas.add(new Object[]{telefono.getCodTelefono(), recortar(telefono.getNombre(), 34),
                Formato.importe(telefono.getPrecio()), Formato.importe(telefono.getPrecioEfectivo())});
        }
        tabla(FILA_CATALOGO, new Object[]{"Codigo", "Telefono", "Precio", "En efectivo"}, filas);
        mensajes.mostrarMensaje("Total de telefonos a la venta: " + telefonos.size());
    }

    public void fichaTelefono(TelefonoModelo telefono) {
        mensajes.mostrarRecuadro(List.of(
                "Codigo      : " + telefono.getCodTelefono(),
                "Marca       : " + Formato.valorO(telefono.getMarca()),
                "Modelo      : " + Formato.valorO(telefono.getModelo()),
                "Precio      : " + Formato.importe(telefono.getPrecio()),
                "En efectivo : " + Formato.importe(telefono.getPrecioEfectivo()),
                "Estado      : " + Formato.valorO(telefono.getEstado())));
    }

    // ---------------------------------------------------------------- Venta

    /** Líneas de la venta en curso con sus totales; el total es una vista previa. */
    public void carrito(CarritoModelo carrito) {
        if (carrito.isVacio()) {
            mensajes.mostrarMensaje("La venta no tiene telefonos.");
            return;
        }
        List<Object[]> filas = new ArrayList<>();
        for (LineaCarrito linea : carrito.getLineas()) {
            TelefonoModelo telefono = linea.getTelefono();
            filas.add(new Object[]{telefono.getCodTelefono(), recortar(telefono.getNombre(), 30), linea.getCantidad(),
                Formato.importe(telefono.getPrecio()), Formato.importe(linea.getSubtotal())});
        }
        tabla(FILA_CARRITO, new Object[]{"Codigo", "Telefono", "Cantidad", "Precio", "Subtotal"}, filas);
        mensajes.mostrarMensaje("Subtotal  : " + Formato.importe(carrito.getSubtotal()));
        if (!carrito.isCredito()) {
            mensajes.mostrarMensaje("Descuento : - " + Formato.importe(carrito.getDescuento())
                    + "  (efectivo 42 %)");
        }
        mensajes.mostrarMensaje("Total     : " + Formato.importe(carrito.getTotal()));
    }

    // ---------------------------------------------------------------- Crédito BanQuito

    /** Si es cliente, si es sujeto de crédito con sus motivos y su monto máximo. */
    public void tarjetaEvaluacion(EvaluacionClienteModelo evaluacion) {
        if (!evaluacion.isCliente()) {
            mensajes.mostrarRecuadro(List.of(
                    "No es cliente de BanQuito",
                    "Cedula: " + Formato.valorO(evaluacion.getCedula()),
                    "Solo los clientes del banco pueden comprar."));
            return;
        }
        List<String> lineas = new ArrayList<>();
        lineas.add("Comprador     : " + Formato.valorO(evaluacion.getNombre()));
        lineas.add("Cedula        : " + Formato.valorO(evaluacion.getCedula()));
        lineas.add("Cliente No.   : " + evaluacion.getCodCliente());
        lineas.add("Evaluacion    : " + (evaluacion.isSujetoCredito() ? "Sujeto de credito" : "No es sujeto de credito"));
        for (EvaluacionClienteModelo.Motivo motivo : evaluacion.getMotivos()) {
            lineas.add("  Motivo      : " + motivo.getDescripcion());
        }
        lineas.add("Monto maximo  : " + Formato.importe(evaluacion.getMontoMaximo()));
        mensajes.mostrarRecuadro(lineas);
    }

    /** Datos del encabezado de una tabla de amortización. */
    public void resumenCredito(ResumenCredito resumen) {
        mensajes.mostrarRecuadro(List.of(
                "Monto         : " + Formato.importe(resumen.getMonto()),
                "Cuotas        : " + resumen.getNumCuotas(),
                "Cuota mensual : " + Formato.importe(resumen.getValorCuota()),
                "Interes total : " + Formato.importe(resumen.getInteresTotal()),
                "Total a pagar : " + Formato.importe(resumen.getTotalPagado())));
    }

    /** Tabla de amortización con las columnas del enunciado. */
    public void tablaAmortizacion(List<CuotaModelo> cuotas) {
        List<Object[]> filas = new ArrayList<>();
        for (CuotaModelo cuota : cuotas) {
            filas.add(new Object[]{cuota.getNumero(), Formato.importe(cuota.getValorCuota()),
                Formato.importe(cuota.getInteres()), Formato.importe(cuota.getCapital()),
                Formato.importe(cuota.getSaldo())});
        }
        tabla(FILA_AMORTIZACION, new Object[]{"# Cuota", "Valor Cuota", "Interes Pagado", "Capital Pagado", "Saldo"},
                filas);
    }

    /** Título, resumen y tabla de amortización de un crédito o de una simulación. */
    public void amortizacion(String titulo, List<CuotaModelo> cuotas) {
        mensajes.mostrarTitulo(titulo);
        ResumenCredito resumen = ResumenCredito.de(cuotas);
        if (resumen == null) {
            mensajes.mostrarMensaje("El banco no devolvio cuotas para este credito.");
            return;
        }
        resumenCredito(resumen);
        tablaAmortizacion(cuotas);
    }

    // ---------------------------------------------------------------- Factura

    /** Factura completa, con los mismos datos que imprimen los clientes web y de escritorio. */
    public void comprobante(FacturaModelo factura) {
        mensajes.mostrarTitulo("MONSTERCELL  |  FACTURA No. " + factura.getNumeroVisible());
        List<String> datos = new ArrayList<>();
        datos.add("Fecha            : " + Formato.fecha(factura.getFecha()));
        datos.add("Comprador        : " + Formato.valorO(factura.getNombreCliente()));
        datos.add("Cedula           : " + Formato.valorO(factura.getCedula()));
        datos.add("Cliente No.      : " + factura.getCodClienteBanco());
        datos.add("Vendedor         : " + Formato.valorO(factura.getUsuario()));
        datos.add("Forma de pago    : " + factura.getFormaPago().getNombreVisible());
        if (factura.isCredito()) {
            datos.add("Credito No.      : " + factura.getCodCreditoBanco());
            datos.add("Cuotas mensuales : " + factura.getNumCuotas());
        }
        mensajes.mostrarRecuadro(datos);

        List<Object[]> filas = new ArrayList<>();
        for (DetalleFacturaModelo detalle : factura.getDetalles()) {
            filas.add(new Object[]{recortar(detalle.getMarca() + " " + detalle.getModelo(), 34), detalle.getCantidad(),
                Formato.importe(detalle.getPrecioUnitario()), Formato.importe(detalle.getSubtotal())});
        }
        tabla(FILA_DETALLE, new Object[]{"Telefono", "Cantidad", "Precio unitario", "Subtotal"}, filas);
        mensajes.mostrarMensaje("Subtotal  : " + Formato.importe(factura.getSubtotal()));
        mensajes.mostrarMensaje((factura.isCredito() ? "Descuento : " : "Descuento efectivo (42 %) : ")
                + Formato.importe(factura.getDescuento()));
        mensajes.mostrarMensaje("Total     : " + Formato.importe(factura.getTotal()));
    }
}
