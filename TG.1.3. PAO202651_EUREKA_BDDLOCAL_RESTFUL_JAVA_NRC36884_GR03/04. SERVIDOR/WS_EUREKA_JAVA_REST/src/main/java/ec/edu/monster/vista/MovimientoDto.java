package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;

/**
 * Elemento del historial de movimientos (RQF-004). cuentaReferencia solo
 * aparece en las transferencias.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"numero", "fecha", "tipo", "accion", "importe", "cuentaReferencia"})
public class MovimientoDto {

    public Integer numero;
    public String fecha;
    public String tipo;
    public String accion;
    public BigDecimal importe;
    public String cuentaReferencia;

    public MovimientoDto() {
    }

    public MovimientoDto(Integer numero, String fecha, String tipo, String accion, BigDecimal importe,
            String cuentaReferencia) {
        this.numero = numero;
        this.fecha = fecha;
        this.tipo = tipo;
        this.accion = accion;
        this.importe = importe;
        this.cuentaReferencia = cuentaReferencia;
    }
}
