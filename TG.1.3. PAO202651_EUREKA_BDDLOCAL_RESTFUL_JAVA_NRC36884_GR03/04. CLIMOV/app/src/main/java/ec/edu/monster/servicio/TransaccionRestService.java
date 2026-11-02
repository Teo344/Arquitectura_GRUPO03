package ec.edu.monster.servicio;

import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import java.math.BigDecimal;
import org.json.JSONObject;

/**
 * Acceso al recurso REST protegido de transacciones (/transacciones): RQF-005
 * a RQF-007. El empleado del movimiento lo toma el servidor del token, nunca
 * del cuerpo de la petición (RN-06).
 */
public class TransaccionRestService {

    private final ClienteRest cliente;

    public TransaccionRestService() {
        this(new ClienteRest());
    }

    public TransaccionRestService(ClienteRest cliente) {
        this.cliente = cliente;
    }

    public ResultadoTransaccionModelo depositar(String token, String cuenta, BigDecimal importe) {
        return aResultado(cliente.post("/transacciones/depositos", token,
                ClienteRest.crearJson("cuenta", cuenta, "importe", importe)));
    }

    public ResultadoTransaccionModelo retirar(String token, String cuenta, BigDecimal importe) {
        return aResultado(cliente.post("/transacciones/retiros", token,
                ClienteRest.crearJson("cuenta", cuenta, "importe", importe)));
    }

    public ResultadoTransaccionModelo transferir(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe) {
        return aResultado(cliente.post("/transacciones/transferencias", token,
                ClienteRest.crearJson("cuentaOrigen", cuentaOrigen, "cuentaDestino", cuentaDestino,
                        "importe", importe)));
    }

    private static ResultadoTransaccionModelo aResultado(JSONObject respuesta) {
        return new ResultadoTransaccionModelo(ClienteRest.texto(respuesta, "mensaje"),
                ClienteRest.decimal(respuesta, "nuevoSaldo"), ClienteRest.entero(respuesta, "numeroMovimiento"),
                ClienteRest.entero(respuesta, "numeroMovimientoDestino"));
    }
}
