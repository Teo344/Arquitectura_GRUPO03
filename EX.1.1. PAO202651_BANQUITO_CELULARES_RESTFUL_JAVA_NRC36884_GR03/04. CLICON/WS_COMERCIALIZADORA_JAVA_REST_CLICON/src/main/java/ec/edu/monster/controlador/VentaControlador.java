package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Punto de venta (CU-07, CU-09 y CU-10). El carrito vive aquí durante la sesión
 * y se conserva al volver al menú principal. Las reglas son las de los clientes
 * web y de escritorio: el vendedor ve el motivo por el que no puede facturar y
 * la API vuelve a validar todo (precios del catálogo, reglas del banco).
 */
public class VentaControlador {

    /** El catálogo vigente y los teléfonos que salieron de la venta porque ya no se venden. */
    public record CatalogoVenta(List<TelefonoModelo> telefonos, List<String> retirados) {
    }

    private final ClienteRESTMonsterCell clienteRest;
    private final CarritoModelo carrito = new CarritoModelo();

    public VentaControlador(ClienteRESTMonsterCell clienteRest) {
        this.clienteRest = clienteRest;
    }

    public CarritoModelo getCarrito() {
        return carrito;
    }

    // ---------------------------------------------------------------- Catálogo

    /**
     * Carga el catálogo y actualiza el carrito con él: precios editados y
     * teléfonos dados de baja desde que se agregaron.
     */
    public CatalogoVenta cargarCatalogo() {
        List<TelefonoModelo> telefonos = clienteRest.telefonos();
        Map<Integer, TelefonoModelo> porCodigo = new LinkedHashMap<>();
        telefonos.forEach(t -> porCodigo.put(t.getCodTelefono(), t));
        return new CatalogoVenta(telefonos, carrito.sincronizar(porCodigo));
    }

    // ---------------------------------------------------------------- Carrito

    /** Suma unidades de un teléfono a la venta, sin pasar de 100 por teléfono. */
    public TelefonoModelo agregar(int codTelefono, int cantidad) {
        int codigo = Validacion.codigo(codTelefono, "un código de teléfono");
        int enVenta = carrito.getCantidades().getOrDefault(codigo, 0);
        if (cantidad <= 0 || cantidad > CarritoModelo.CANTIDAD_MAXIMA - enVenta) {
            throw new IllegalArgumentException("La cantidad de un teléfono debe estar entre 1 y "
                    + CarritoModelo.CANTIDAD_MAXIMA + ".");
        }
        TelefonoModelo telefono = clienteRest.telefono(codigo);
        if (!telefono.isActivo()) {
            throw new IllegalArgumentException(telefono.getNombre() + " ya no está a la venta.");
        }
        carrito.agregar(telefono);
        carrito.cambiarCantidad(codigo, enVenta + cantidad);
        return telefono;
    }

    /** Con 0 el teléfono sale de la venta; el máximo es 100 unidades. */
    public void cambiarCantidad(int codTelefono, int cantidad) {
        exigirEnVenta(codTelefono);
        if (cantidad < 0 || cantidad > CarritoModelo.CANTIDAD_MAXIMA) {
            throw new IllegalArgumentException("La cantidad de un teléfono debe estar entre 0 y "
                    + CarritoModelo.CANTIDAD_MAXIMA + ".");
        }
        carrito.cambiarCantidad(codTelefono, cantidad);
    }

    public void quitar(int codTelefono) {
        exigirEnVenta(codTelefono);
        carrito.quitar(codTelefono);
    }

    public void vaciar() {
        carrito.vaciar();
    }

    private void exigirEnVenta(int codTelefono) {
        if (!carrito.getCantidades().containsKey(codTelefono)) {
            throw new IllegalArgumentException("El teléfono " + codTelefono + " no está en la venta.");
        }
    }

    // ---------------------------------------------------------------- Comprador (CU-07)

    /** Servicios 1 y 2 del banco: si es cliente, si es sujeto de crédito y su monto máximo. */
    public EvaluacionClienteModelo verificarComprador(String texto) {
        carrito.setCedula(texto);
        EvaluacionClienteModelo evaluacion = clienteRest.evaluarCliente(Validacion.cedula(texto));
        carrito.setEvaluacion(evaluacion);
        return evaluacion;
    }

    // ---------------------------------------------------------------- Forma de pago

    public void cambiarFormaPago(FormaPago formaPago) {
        carrito.setFormaPago(formaPago);
    }

    /** Entre 3 y 18 cuotas (RN-06). */
    public void cambiarCuotas(int numCuotas) {
        if (numCuotas < CarritoModelo.CUOTAS_MINIMO || numCuotas > CarritoModelo.CUOTAS_MAXIMO) {
            throw new IllegalArgumentException("Elige entre 3 y 18 cuotas.");
        }
        carrito.setNumCuotas(numCuotas);
    }

    // ---------------------------------------------------------------- Simulación

    /** La tabla que tendría el crédito de esta venta, sin otorgarlo. */
    public List<CuotaModelo> simularCuotas() {
        if (carrito.isVacio()) {
            throw new IllegalArgumentException("Agrega al menos un teléfono del catálogo.");
        }
        return clienteRest.simular(carrito.getTotal(), carrito.getNumCuotas());
    }

    /** Es solo informativa: si el banco no responde, la venta sigue y se devuelve null. */
    public BigDecimal cuotaEstimada() {
        if (!carrito.isCredito() || carrito.isVacio()) {
            return null;
        }
        try {
            List<CuotaModelo> tabla = clienteRest.simular(carrito.getTotal(), carrito.getNumCuotas());
            return tabla.isEmpty() ? null : tabla.get(0).getValorCuota();
        } catch (ErrorServicioModelo ex) {
            if (ex.requiereNuevoLogin()) {
                throw ex;
            }
            return null;
        }
    }

    // ---------------------------------------------------------------- Facturar (CU-09 y CU-10)

    /** Lo que el vendedor debe corregir antes de facturar, o null si la venta está lista. */
    public String motivoParaNoFacturar() {
        try {
            validarParaFacturar();
            return null;
        } catch (IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }

    /** Lo que el vendedor puede corregir antes de llamar a la API (los mismos mensajes de los otros clientes). */
    public void validarParaFacturar() {
        if (carrito.isVacio()) {
            throw new IllegalArgumentException("Agrega al menos un teléfono del catálogo.");
        }
        EvaluacionClienteModelo evaluacion = carrito.getEvaluacion();
        if (evaluacion == null) {
            throw new IllegalArgumentException("Verifica la cédula del comprador en BanQuito antes de facturar.");
        }
        if (!evaluacion.isCliente()) {
            throw new IllegalArgumentException("El comprador debe ser cliente de BanQuito.");
        }
        if (carrito.isCredito() && !evaluacion.isSujetoCredito()) {
            throw new IllegalArgumentException("El comprador no es sujeto de crédito: "
                    + evaluacion.getMotivos().stream().map(m -> m.getDescripcion().toLowerCase(Locale.ROOT))
                            .collect(Collectors.joining(", ")) + ". Puede pagar en efectivo.");
        }
        if (carrito.isSuperaMontoMaximo()) {
            throw new IllegalArgumentException("El total supera el monto máximo de crédito del comprador. Quita"
                    + " teléfonos o cambia a pago en efectivo.");
        }
    }

    /** Solo se envían teléfonos y cantidades: la API toma los precios del catálogo y pide el crédito al banco. */
    public FacturaModelo facturar() {
        validarParaFacturar();
        FacturaModelo factura = clienteRest.facturar(carrito.getCedula(), carrito.getFormaPago(),
                carrito.getNumCuotas(), carrito.getLineas());
        carrito.vaciar();
        return factura;
    }

    /** Servicio 4 del banco: la tabla del crédito con que se pagó una factura. */
    public List<CuotaModelo> cuotasDe(FacturaModelo factura) {
        return clienteRest.cuotas(factura.getCodCreditoBanco());
    }
}
