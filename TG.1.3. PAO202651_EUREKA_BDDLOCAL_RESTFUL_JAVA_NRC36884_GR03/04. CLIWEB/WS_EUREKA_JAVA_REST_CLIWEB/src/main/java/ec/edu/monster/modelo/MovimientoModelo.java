package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Movimiento del historial de una cuenta (RQF-004). accion es INGRESO o
 * SALIDA; cuentaReferencia solo viene en las transferencias.
 */
public class MovimientoModelo {

    private final int numero;
    private final String fecha;
    private final String tipo;
    private final String accion;
    private final BigDecimal importe;
    private final String cuentaReferencia;

    public MovimientoModelo(int numero, String fecha, String tipo, String accion, BigDecimal importe,
            String cuentaReferencia) {
        this.numero = numero;
        this.fecha = fecha;
        this.tipo = tipo;
        this.accion = accion;
        this.importe = importe;
        this.cuentaReferencia = cuentaReferencia;
    }

    public int getNumero() {
        return numero;
    }

    public String getFecha() {
        return fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public String getAccion() {
        return accion;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public String getCuentaReferencia() {
        return cuentaReferencia;
    }
}
