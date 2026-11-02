package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Una factura. codCreditoBanco y numCuotas solo aparecen en las ventas a
 * crédito; detalles solo al consultar una factura (no en la lista).
 *
 * @author carlo
 */
@JsonbPropertyOrder({"numFactura", "fecha", "usuario", "cedula", "codClienteBanco", "nombreCliente", "formaPago",
    "subtotal", "descuento", "total", "codCreditoBanco", "numCuotas", "detalles"})
public class FacturaDto {

    @JsonbPropertyOrder({"codTelefono", "marca", "modelo", "cantidad", "precioUnitario", "subtotal"})
    public static class DetalleDto {

        public int codTelefono;
        public String marca;
        public String modelo;
        public int cantidad;
        public BigDecimal precioUnitario;
        public BigDecimal subtotal;
    }

    public int numFactura;
    public LocalDateTime fecha;
    public String usuario;
    public String cedula;
    public int codClienteBanco;
    public String nombreCliente;
    public String formaPago;
    public BigDecimal subtotal;
    public BigDecimal descuento;
    public BigDecimal total;
    public Integer codCreditoBanco;
    public Integer numCuotas;
    public List<DetalleDto> detalles;
}
