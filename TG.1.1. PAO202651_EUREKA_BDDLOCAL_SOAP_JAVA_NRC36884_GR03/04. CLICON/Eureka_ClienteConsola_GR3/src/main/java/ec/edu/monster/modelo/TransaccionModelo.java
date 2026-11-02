package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Datos de una transacción que ingresa el empleado. cuentaDestino solo se usa
 * en las transferencias; en ellas cuenta es la cuenta de origen.
 */
public class TransaccionModelo {

    private final TipoTransaccion tipo;
    private final String cuenta;
    private final String cuentaDestino;
    private final BigDecimal importe;

    public TransaccionModelo(TipoTransaccion tipo, String cuenta, BigDecimal importe) {
        this(tipo, cuenta, null, importe);
    }

    public TransaccionModelo(TipoTransaccion tipo, String cuenta, String cuentaDestino, BigDecimal importe) {
        this.tipo = tipo;
        this.cuenta = cuenta;
        this.cuentaDestino = cuentaDestino;
        this.importe = importe;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public String getCuenta() {
        return cuenta;
    }

    public String getCuentaDestino() {
        return cuentaDestino;
    }

    public BigDecimal getImporte() {
        return importe;
    }
}
