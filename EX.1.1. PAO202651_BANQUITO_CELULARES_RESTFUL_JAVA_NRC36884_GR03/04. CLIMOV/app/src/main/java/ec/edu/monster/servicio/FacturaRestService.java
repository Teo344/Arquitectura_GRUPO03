package ec.edu.monster.servicio;

import ec.edu.monster.modelo.DetalleFacturaModelo;
import ec.edu.monster.modelo.FacturaModelo;
import ec.edu.monster.modelo.FormaPago;
import ec.edu.monster.modelo.LineaCarrito;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Acceso al recurso REST de facturas (/facturas): CU-09, CU-10 y CU-11. */
public class FacturaRestService {

    private final ClienteRest cliente;

    public FacturaRestService() {
        this(new ClienteRest());
    }

    public FacturaRestService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    /**
     * Factura las líneas del carrito. Solo se envían el teléfono y la cantidad:
     * el servidor toma los precios del catálogo y, a crédito, pide el crédito
     * al banco.
     */
    public FacturaModelo facturar(String token, String cedula, FormaPago formaPago, int numCuotas,
            List<LineaCarrito> lineas) {
        JSONArray detalles = new JSONArray();
        for (LineaCarrito linea : lineas) {
            detalles.put(ClienteRest.crearJson("codTelefono", linea.getTelefono().getCodTelefono(),
                    "cantidad", linea.getCantidad()));
        }
        JSONObject datos = formaPago == FormaPago.CREDITO
                ? ClienteRest.crearJson("cedula", cedula, "formaPago", formaPago.name(), "numCuotas", numCuotas,
                        "detalles", detalles)
                : ClienteRest.crearJson("cedula", cedula, "formaPago", formaPago.name(), "detalles", detalles);
        return aFactura(cliente.post("/facturas", token, datos));
    }

    public FacturaModelo factura(String token, int numFactura) {
        return aFactura(cliente.get("/facturas/" + numFactura, token));
    }

    /** Las facturas más recientes primero; con cédula vacía, las de todos los clientes. */
    public List<FacturaModelo> facturas(String token, String cedula) {
        String ruta = cedula == null || cedula.trim().isEmpty()
                ? "/facturas" : "/facturas?cedula=" + ClienteRest.codificar(cedula.trim());
        JSONArray datos = cliente.getLista(ruta, token);
        List<FacturaModelo> facturas = new ArrayList<>(datos.length());
        for (int i = 0; i < datos.length(); i++) {
            JSONObject elemento = datos.optJSONObject(i);
            if (elemento != null) {
                facturas.add(aFactura(elemento));
            }
        }
        return Collections.unmodifiableList(facturas);
    }

    private static FacturaModelo aFactura(JSONObject f) {
        List<DetalleFacturaModelo> detalles = new ArrayList<>();
        JSONArray lista = f.optJSONArray("detalles");
        if (lista != null) {
            for (int i = 0; i < lista.length(); i++) {
                JSONObject d = lista.optJSONObject(i);
                if (d != null) {
                    detalles.add(new DetalleFacturaModelo(ClienteRest.entero(d, "codTelefono"),
                            ClienteRest.texto(d, "marca"), ClienteRest.texto(d, "modelo"),
                            ClienteRest.entero(d, "cantidad"), ClienteRest.decimal(d, "precioUnitario"),
                            ClienteRest.decimal(d, "subtotal")));
                }
            }
        }
        FormaPago formaPago = "CREDITO".equals(ClienteRest.texto(f, "formaPago")) ? FormaPago.CREDITO : FormaPago.EFECTIVO;
        return new FacturaModelo(ClienteRest.entero(f, "numFactura"), ClienteRest.texto(f, "fecha"),
                ClienteRest.texto(f, "usuario"), ClienteRest.texto(f, "cedula"),
                ClienteRest.entero(f, "codClienteBanco"), ClienteRest.texto(f, "nombreCliente"), formaPago,
                ClienteRest.decimal(f, "subtotal"), ClienteRest.decimal(f, "descuento"),
                ClienteRest.decimal(f, "total"), ClienteRest.enteroOpcional(f, "codCreditoBanco"),
                ClienteRest.enteroOpcional(f, "numCuotas"), detalles);
    }
}
