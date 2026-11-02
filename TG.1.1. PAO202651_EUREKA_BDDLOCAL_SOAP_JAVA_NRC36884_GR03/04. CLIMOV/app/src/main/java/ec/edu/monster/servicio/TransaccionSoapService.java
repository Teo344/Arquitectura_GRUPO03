package ec.edu.monster.servicio;

import ec.edu.monster.modelo.ResultadoTransaccionModelo;
import java.math.BigDecimal;
import org.w3c.dom.Element;

/**
 * Acceso al servicio SOAP protegido de transacciones (TransaccionWS): RQF-005
 * a RQF-007. El empleado del movimiento lo toma el servidor del token, nunca
 * de un parámetro (RN-06).
 */
public class TransaccionSoapService {

    private static final String SERVICIO = "TransaccionWS";

    private final ClienteSoap cliente;

    public TransaccionSoapService() {
        this(new ClienteSoap());
    }

    public TransaccionSoapService(ClienteSoap cliente) {
        this.cliente = cliente;
    }

    public ResultadoTransaccionModelo depositar(String token, String cuenta, BigDecimal importe) {
        return aResultado(cliente.invocar(SERVICIO, "depositar", token,
                ClienteSoap.elemento("cuenta", cuenta) + ClienteSoap.elemento("importe", importe.toPlainString())));
    }

    public ResultadoTransaccionModelo retirar(String token, String cuenta, BigDecimal importe) {
        return aResultado(cliente.invocar(SERVICIO, "retirar", token,
                ClienteSoap.elemento("cuenta", cuenta) + ClienteSoap.elemento("importe", importe.toPlainString())));
    }

    public ResultadoTransaccionModelo transferir(String token, String cuentaOrigen, String cuentaDestino,
            BigDecimal importe) {
        return aResultado(cliente.invocar(SERVICIO, "transferir", token,
                ClienteSoap.elemento("cuentaOrigen", cuentaOrigen)
                + ClienteSoap.elemento("cuentaDestino", cuentaDestino)
                + ClienteSoap.elemento("importe", importe.toPlainString())));
    }

    private static ResultadoTransaccionModelo aResultado(Element respuesta) {
        return new ResultadoTransaccionModelo(ClienteSoap.texto(respuesta, "mensaje"),
                ClienteSoap.decimal(respuesta, "nuevoSaldo"), ClienteSoap.entero(respuesta, "numeroMovimiento"),
                ClienteSoap.entero(respuesta, "numeroMovimientoDestino"));
    }
}
