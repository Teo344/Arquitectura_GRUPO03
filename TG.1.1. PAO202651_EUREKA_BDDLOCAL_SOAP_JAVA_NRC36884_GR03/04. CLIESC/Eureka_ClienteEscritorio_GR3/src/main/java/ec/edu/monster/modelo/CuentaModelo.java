package ec.edu.monster.modelo;

import java.math.BigDecimal;

/** Datos de una cuenta devueltos por CuentaWS.consultarCuenta (RQF-003). */
public class CuentaModelo {

    private final String codigo;
    private final String titular;
    private final String moneda;
    private final BigDecimal saldo;
    private final String estado;
    private final String sucursal;
    private final String fechaCreacion;

    public CuentaModelo(String codigo, String titular, String moneda, BigDecimal saldo, String estado,
            String sucursal, String fechaCreacion) {
        this.codigo = codigo;
        this.titular = titular;
        this.moneda = moneda;
        this.saldo = saldo;
        this.estado = estado;
        this.sucursal = sucursal;
        this.fechaCreacion = fechaCreacion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitular() {
        return titular;
    }

    public String getMoneda() {
        return moneda;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public String getEstado() {
        return estado;
    }

    public String getSucursal() {
        return sucursal;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }
}
