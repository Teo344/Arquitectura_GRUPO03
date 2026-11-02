package ec.edu.monster.servicio;

import ec.edu.monster.modelo.CuentaModelo;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.MovimientoModelo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Element;

/** Acceso al servicio SOAP protegido de consultas (CuentaWS): RQF-003 y RQF-004. */
public class CuentaSoapService {

    private static final String SERVICIO = "CuentaWS";

    private final ClienteSoap cliente;

    public CuentaSoapService() {
        this(new ClienteSoap());
    }

    public CuentaSoapService(ClienteSoap cliente) {
        this.cliente = cliente;
    }

    public CuentaModelo consultarCuenta(String token, String cuenta) {
        Element respuesta = cliente.invocar(SERVICIO, "consultarCuenta", token,
                ClienteSoap.elemento("cuenta", cuenta));
        Element datos = ClienteSoap.hijo(respuesta, "cuenta");
        if (datos == null) {
            throw new ErrorServicioModelo(null, "El servidor no devolvió los datos de la cuenta.");
        }
        return new CuentaModelo(ClienteSoap.texto(datos, "codigo"), ClienteSoap.texto(datos, "titular"),
                ClienteSoap.texto(datos, "moneda"), ClienteSoap.decimal(datos, "saldo"),
                ClienteSoap.texto(datos, "estado"), ClienteSoap.texto(datos, "sucursal"),
                ClienteSoap.texto(datos, "fechaCreacion"));
    }

    /** Una cuenta sin movimientos devuelve una lista vacía. */
    public List<MovimientoModelo> consultarMovimientos(String token, String cuenta) {
        Element respuesta = cliente.invocar(SERVICIO, "consultarMovimientos", token,
                ClienteSoap.elemento("cuenta", cuenta));

        List<MovimientoModelo> movimientos = new ArrayList<>();
        for (Element elemento : ClienteSoap.hijos(ClienteSoap.hijo(respuesta, "movimientos"), "movimiento")) {
            Integer numero = ClienteSoap.entero(elemento, "numero");
            movimientos.add(new MovimientoModelo(numero == null ? 0 : numero,
                    ClienteSoap.texto(elemento, "fecha"), ClienteSoap.texto(elemento, "tipo"),
                    ClienteSoap.texto(elemento, "accion"), ClienteSoap.decimal(elemento, "importe"),
                    ClienteSoap.texto(elemento, "cuentaReferencia")));
        }
        return Collections.unmodifiableList(movimientos);
    }
}
