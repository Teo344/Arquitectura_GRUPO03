package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;

/**
 * Datos de una cuenta (RQF-003). La fecha va en formato ISO (aaaa-mm-dd).
 *
 * @author carlo
 */
@JsonbPropertyOrder({"codigo", "titular", "moneda", "saldo", "estado", "sucursal", "fechaCreacion"})
public class CuentaDto {

    public String codigo;
    public String titular;
    public String moneda;
    public BigDecimal saldo;
    public String estado;
    public String sucursal;
    public String fechaCreacion;

    public CuentaDto() {
    }

    public CuentaDto(String codigo, String titular, String moneda, BigDecimal saldo, String estado,
            String sucursal, String fechaCreacion) {
        this.codigo = codigo;
        this.titular = titular;
        this.moneda = moneda;
        this.saldo = saldo;
        this.estado = estado;
        this.sucursal = sucursal;
        this.fechaCreacion = fechaCreacion;
    }
}
