package ec.edu.monster.servicio;

import ec.edu.monster.modelo.TelefonoModelo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Acceso al recurso REST del catálogo (/telefonos): CU-03 a CU-06. */
public class TelefonoRestService {

    private final ClienteRest cliente;

    public TelefonoRestService() {
        this(new ClienteRest());
    }

    public TelefonoRestService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public List<TelefonoModelo> listar(String token) {
        JSONArray datos = cliente.getLista("/telefonos", token);
        List<TelefonoModelo> telefonos = new ArrayList<>(datos.length());
        for (int i = 0; i < datos.length(); i++) {
            JSONObject elemento = datos.optJSONObject(i);
            if (elemento != null) {
                telefonos.add(aTelefono(elemento));
            }
        }
        return Collections.unmodifiableList(telefonos);
    }

    public TelefonoModelo buscar(String token, int codTelefono) {
        return aTelefono(cliente.get("/telefonos/" + codTelefono, token));
    }

    public TelefonoModelo crear(String token, String marca, String modelo, BigDecimal precio) {
        return aTelefono(cliente.post("/telefonos", token, datosTelefono(marca, modelo, precio)));
    }

    public TelefonoModelo actualizar(String token, int codTelefono, String marca, String modelo, BigDecimal precio) {
        return aTelefono(cliente.put("/telefonos/" + codTelefono, token, datosTelefono(marca, modelo, precio)));
    }

    /** Baja lógica: el teléfono queda INACTIVO y ya no se puede vender. */
    public void eliminar(String token, int codTelefono) {
        cliente.delete("/telefonos/" + codTelefono, token);
    }

    private static JSONObject datosTelefono(String marca, String modelo, BigDecimal precio) {
        return ClienteRest.crearJson("marca", marca, "modelo", modelo, "precio", precio);
    }

    private static TelefonoModelo aTelefono(JSONObject t) {
        return new TelefonoModelo(ClienteRest.entero(t, "codTelefono"), ClienteRest.texto(t, "marca"),
                ClienteRest.texto(t, "modelo"), ClienteRest.decimal(t, "precio"),
                ClienteRest.decimal(t, "precioEfectivo"), ClienteRest.texto(t, "estado"));
    }
}
