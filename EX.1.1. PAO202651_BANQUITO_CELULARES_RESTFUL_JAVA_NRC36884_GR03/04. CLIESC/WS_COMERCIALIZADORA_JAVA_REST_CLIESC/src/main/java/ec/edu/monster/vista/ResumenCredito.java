package ec.edu.monster.vista;

import ec.edu.monster.modelo.CuotaModelo;
import java.math.BigDecimal;
import java.util.List;

/**
 * Datos del encabezado de una tabla de amortización, sacados de la propia
 * tabla: el monto es el capital de la primera cuota más su saldo, y la cuota
 * es fija en todas las filas.
 */
public final class ResumenCredito {

    private final BigDecimal monto;
    private final int numCuotas;
    private final BigDecimal valorCuota;
    private final BigDecimal interesTotal;
    private final BigDecimal totalPagado;

    private ResumenCredito(BigDecimal monto, int numCuotas, BigDecimal valorCuota, BigDecimal interesTotal,
            BigDecimal totalPagado) {
        this.monto = monto;
        this.numCuotas = numCuotas;
        this.valorCuota = valorCuota;
        this.interesTotal = interesTotal;
        this.totalPagado = totalPagado;
    }

    public static ResumenCredito de(List<CuotaModelo> cuotas) {
        if (cuotas == null || cuotas.isEmpty()) {
            return null;
        }
        CuotaModelo primera = cuotas.get(0);
        BigDecimal interes = cuotas.stream().map(CuotaModelo::getInteres).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pagado = cuotas.stream().map(CuotaModelo::getValorCuota).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ResumenCredito(primera.getCapital().add(primera.getSaldo()), cuotas.size(), primera.getValorCuota(),
                interes, pagado);
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public int getNumCuotas() {
        return numCuotas;
    }

    public BigDecimal getValorCuota() {
        return valorCuota;
    }

    public BigDecimal getInteresTotal() {
        return interesTotal;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
    }
}
