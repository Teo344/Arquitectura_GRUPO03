package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Resultado de un depósito, retiro o transferencia. En una transferencia,
 * nuevoSaldo es el de la cuenta de origen y numeroMovimientoDestino el número
 * del movimiento registrado en la cuenta de destino.
 */
public class ResultadoTransaccionModelo {

    private final String mensaje;
    private final BigDecimal nuevoSaldo;
    private final Integer numeroMovimiento;
    private final Integer numeroMovimientoDestino;

    public ResultadoTransaccionModelo(String mensaje, BigDecimal nuevoSaldo, Integer numeroMovimiento,
            Integer numeroMovimientoDestino) {
        this.mensaje = mensaje;
        this.nuevoSaldo = nuevoSaldo;
        this.numeroMovimiento = numeroMovimiento;
        this.numeroMovimientoDestino = numeroMovimientoDestino;
    }

    public String getMensaje() {
        return mensaje;
    }

    public BigDecimal getNuevoSaldo() {
        return nuevoSaldo;
    }

    public Integer getNumeroMovimiento() {
        return numeroMovimiento;
    }

    public Integer getNumeroMovimientoDestino() {
        return numeroMovimientoDestino;
    }
}
