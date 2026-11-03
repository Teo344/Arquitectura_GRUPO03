package ec.edu.monster.modelo;

import java.math.BigDecimal;

/** Crédito directo que otorgó BanQuito a un comprador. */
public class CreditoModelo {

    private final int codCredito;
    private final String fecha;
    private final BigDecimal monto;
    private final int numCuotas;
    private final BigDecimal tasaAnual;
    private final BigDecimal valorCuota;
    private final String estado;

    public CreditoModelo(int codCredito, String fecha, BigDecimal monto, int numCuotas, BigDecimal tasaAnual,
            BigDecimal valorCuota, String estado) {
        this.codCredito = codCredito;
        this.fecha = fecha;
        this.monto = monto;
        this.numCuotas = numCuotas;
        this.tasaAnual = tasaAnual;
        this.valorCuota = valorCuota;
        this.estado = estado;
    }

    public int getCodCredito() {
        return codCredito;
    }

    public String getFecha() {
        return fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public int getNumCuotas() {
        return numCuotas;
    }

    public BigDecimal getTasaAnual() {
        return tasaAnual;
    }

    public BigDecimal getValorCuota() {
        return valorCuota;
    }

    public String getEstado() {
        return estado;
    }
}
