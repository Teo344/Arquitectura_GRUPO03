package ec.edu.monster.modelo;

import java.math.BigDecimal;

/** Una fila de la tabla de amortización: saldo es lo que queda por pagar después de esta cuota. */
public class CuotaModelo {

    private final int numero;
    private final BigDecimal valorCuota;
    private final BigDecimal interes;
    private final BigDecimal capital;
    private final BigDecimal saldo;

    public CuotaModelo(int numero, BigDecimal valorCuota, BigDecimal interes, BigDecimal capital, BigDecimal saldo) {
        this.numero = numero;
        this.valorCuota = valorCuota;
        this.interes = interes;
        this.capital = capital;
        this.saldo = saldo;
    }

    public int getNumero() {
        return numero;
    }

    public BigDecimal getValorCuota() {
        return valorCuota;
    }

    public BigDecimal getInteres() {
        return interes;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }
}
