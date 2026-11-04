package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.CarritoModelo;
import ec.edu.monster.modelo.CuotaModelo;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.EvaluacionClienteModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.LineaCarrito;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import java.math.BigDecimal;
import java.util.List;

/**
 * Prueba de punta a punta del cliente de consola sin los menús: las mismas
 * capas de servicio y modelo que usan los controladores, con los casos de
 * PruebaClienteWeb (sesión, catálogo, venta en efectivo, créditos rechazados y
 * aprobado, tabla de amortización, facturas, simulador y salir). Necesita
 * Payara con WS_BANQUITO_JAVA_REST y WS_COMERCIALIZADORA_JAVA_REST, y MySQL con
 * las BD del examen recién cargadas (01 + 02): la venta a crédito al cliente 1
 * solo se puede aprobar una vez por carga.
 */
public class PruebaClienteConsola {

    private static final String CLIENTE_1 = "1712345678";
    private static final String CLIENTE_3 = "1734567890";
    private static final String CLIENTE_5 = "1756789012";

    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== PruebaClienteConsola (MonsterCell) ===");
        ClienteRESTMonsterCell api = new ClienteRESTMonsterCell();

        comprobar("sin sesión, el catálogo pide iniciar sesión (AUTH-003)", codigoDe(api::telefonos, "AUTH-003"));
        comprobar("clave incorrecta da AUTH-001", codigoDe(() -> api.iniciarSesion("MONSTER", "otra"), "AUTH-001"));
        comprobar("MONSTER/MONSTER9 inicia sesión", api.iniciarSesion("MONSTER", "MONSTER9").getToken() != null);

        // ---- Catálogo
        String modelo = "Modelo Consola " + System.currentTimeMillis();
        TelefonoModelo nuevo = api.crearTelefono("Prueba", modelo, new BigDecimal("100.00"));
        comprobar("agregar un teléfono lo muestra en el catálogo", contiene(api.telefonos(), nuevo.getCodTelefono()));
        TelefonoModelo editado = api.actualizarTelefono(nuevo.getCodTelefono(), "Prueba", modelo, new BigDecimal("120.50"));
        comprobar("editarlo cambia su precio a 120.50", editado.getPrecio().compareTo(new BigDecimal("120.50")) == 0);
        comprobar("precio 0 da TEL-003", codigoDe(() -> api.crearTelefono("A", "B", BigDecimal.ZERO), "TEL-003"));
        api.eliminarTelefono(nuevo.getCodTelefono());
        comprobar("darlo de baja lo quita del catálogo", !contiene(api.telefonos(), nuevo.getCodTelefono()));

        // ---- Venta en efectivo: 2 x 199 + 279 (la vista previa del carrito coincide con la factura)
        CarritoModelo carrito = new CarritoModelo();
        carrito.agregar(api.telefono(1));
        carrito.agregar(api.telefono(1));
        carrito.agregar(api.telefono(2));
        carrito.setCedula(CLIENTE_1);
        carrito.setEvaluacion(api.evaluarCliente(CLIENTE_1));
        comprobar("el comprador 1 es cliente de BanQuito", carrito.getEvaluacion().isCliente());
        comprobar("el carrito calcula el total en efectivo 392.66", carrito.getTotal().compareTo(new BigDecimal("392.66")) == 0);
        FacturaModelo efectivo = api.facturar(CLIENTE_1, FormaPago.EFECTIVO, null, carrito.getLineas());
        comprobar("la factura en efectivo tiene 677.00 - 284.34 = 392.66",
                igual(efectivo.getSubtotal(), "677.00") && igual(efectivo.getDescuento(), "284.34")
                && igual(efectivo.getTotal(), "392.66") && efectivo.getDetalles().size() == 2);

        // ---- Crédito rechazado: cliente 3 (hombre de 22 años)
        EvaluacionClienteModelo cliente3 = api.evaluarCliente(CLIENTE_3);
        comprobar("el cliente 3 no es sujeto de crédito", cliente3.isCliente() && !cliente3.isSujetoCredito()
                && !cliente3.getMotivos().isEmpty());
        comprobar("venderle a crédito da CRE-001", codigoDe(() -> api.facturar(CLIENTE_3, FormaPago.CREDITO, 6,
                lineas(api, 1)), "CRE-001"));

        // ---- Crédito que supera el máximo: 2399 para el cliente 1 (máximo 2250)
        CarritoModelo caro = new CarritoModelo();
        caro.agregar(api.telefono(8));
        caro.setCedula(CLIENTE_1);
        caro.setFormaPago(FormaPago.CREDITO);
        caro.setEvaluacion(api.evaluarCliente(CLIENTE_1));
        comprobar("2399 a crédito supera el monto máximo en el carrito", caro.isSuperaMontoMaximo());
        comprobar("y la API lo rechaza con CRE-002", codigoDe(() -> api.facturar(CLIENTE_1, FormaPago.CREDITO, 6,
                caro.getLineas()), "CRE-002"));

        // ---- Crédito aprobado: 899 a 6 cuotas para el cliente 1
        EvaluacionClienteModelo cliente1 = api.evaluarCliente(CLIENTE_1);
        boolean sujeto = cliente1.isSujetoCredito();
        comprobar("el cliente 1 aún es sujeto de crédito (BD recién cargada)", sujeto);
        if (sujeto) {
            FacturaModelo credito = api.facturar(CLIENTE_1, FormaPago.CREDITO, 6, lineas(api, 5));
            comprobar("la factura a crédito trae el crédito BanQuito", credito.isCredito()
                    && credito.getCodCreditoBanco() != null && credito.getNumCuotas() == 6);
            List<CuotaModelo> tabla = credito.getCodCreditoBanco() == null ? List.of()
                    : api.cuotas(credito.getCodCreditoBanco());
            comprobar("la tabla de amortización tiene 6 cuotas y cierra en 0.00", tabla.size() == 6
                    && igual(tabla.get(5).getSaldo(), "0.00"));
        }

        // ---- Consultas
        comprobar("las facturas del cliente 1 incluyen la venta en efectivo", api.facturas(CLIENTE_1).stream()
                .anyMatch(f -> f.getNumFactura() == efectivo.getNumFactura()));
        List<CuotaModelo> simulacion = api.simular(new BigDecimal("450"), 6);
        comprobar("el simulador da 78.65 para 450 a 6 cuotas", !simulacion.isEmpty()
                && igual(simulacion.get(0).getValorCuota(), "78.65"));
        comprobar("el cliente 5 tiene un crédito en BanQuito", !api.creditos(CLIENTE_5).isEmpty());

        api.cerrarSesion();
        comprobar("salir invalida la sesión", api.getSesion() == null && codigoDe(api::telefonos, "AUTH-003"));

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
        if (!sujeto) {
            System.out.println("Nota: recarga las BD del examen (01 + 02) para repetir la venta a crédito.");
        }
    }

    private static List<LineaCarrito> lineas(ClienteRESTMonsterCell api, int codTelefono) {
        CarritoModelo carrito = new CarritoModelo();
        carrito.agregar(api.telefono(codTelefono));
        return carrito.getLineas();
    }

    private static boolean contiene(List<TelefonoModelo> telefonos, int codTelefono) {
        return telefonos.stream().anyMatch(t -> t.getCodTelefono() == codTelefono);
    }

    private static boolean igual(BigDecimal valor, String esperado) {
        return valor != null && valor.compareTo(new BigDecimal(esperado)) == 0;
    }

    /** true si la llamada falla con ese código de error de la API. */
    private static boolean codigoDe(Runnable llamada, String codigo) {
        try {
            llamada.run();
            return false;
        } catch (ErrorServicioModelo ex) {
            return codigo.equals(ex.getCodigo());
        }
    }

    private static void comprobar(String caso, boolean correcto) {
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + caso);
        if (!correcto) {
            fallidas++;
        }
    }
}
