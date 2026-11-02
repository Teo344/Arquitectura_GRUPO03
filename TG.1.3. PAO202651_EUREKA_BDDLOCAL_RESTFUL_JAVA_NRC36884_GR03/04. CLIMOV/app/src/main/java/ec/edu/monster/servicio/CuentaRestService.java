package ec.edu.monster.servicio;

import ec.edu.monster.modelo.CuentaModelo;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.MovimientoModelo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Acceso al recurso REST protegido de cuentas (/cuentas): RQF-003 y RQF-004. */
public class CuentaRestService {

    private final ClienteRest cliente;

    public CuentaRestService() {
        this(new ClienteRest());
    }

    public CuentaRestService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public CuentaModelo consultarCuenta(String token, String cuenta) {
        JSONObject respuesta = cliente.get("/cuentas/" + ClienteRest.codificar(cuenta), token);
        JSONObject datos = respuesta.optJSONObject("cuenta");
        if (datos == null) {
            throw new ErrorServicioModelo(null, "El servidor no devolvió los datos de la cuenta.");
        }
        return new CuentaModelo(ClienteRest.texto(datos, "codigo"), ClienteRest.texto(datos, "titular"),
                ClienteRest.texto(datos, "moneda"), ClienteRest.decimal(datos, "saldo"),
                ClienteRest.texto(datos, "estado"), ClienteRest.texto(datos, "sucursal"),
                ClienteRest.texto(datos, "fechaCreacion"));
    }

    /** Una cuenta sin movimientos devuelve una lista vacía. */
    public List<MovimientoModelo> consultarMovimientos(String token, String cuenta) {
        JSONObject respuesta = cliente.get("/cuentas/" + ClienteRest.codificar(cuenta) + "/movimientos", token);
        JSONArray datos = respuesta.optJSONArray("movimientos");
        if (datos == null) {
            return Collections.emptyList();
        }
        List<MovimientoModelo> movimientos = new ArrayList<>(datos.length());
        for (int i = 0; i < datos.length(); i++) {
            JSONObject elemento = datos.optJSONObject(i);
            if (elemento == null) {
                continue;
            }
            Integer numero = ClienteRest.entero(elemento, "numero");
            movimientos.add(new MovimientoModelo(numero == null ? 0 : numero,
                    ClienteRest.texto(elemento, "fecha"), ClienteRest.texto(elemento, "tipo"),
                    ClienteRest.texto(elemento, "accion"), ClienteRest.decimal(elemento, "importe"),
                    ClienteRest.texto(elemento, "cuentaReferencia")));
        }
        return Collections.unmodifiableList(movimientos);
    }
}
