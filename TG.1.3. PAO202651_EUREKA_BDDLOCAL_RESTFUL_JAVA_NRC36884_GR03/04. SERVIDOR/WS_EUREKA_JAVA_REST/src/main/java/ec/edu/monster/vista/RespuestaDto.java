package ec.edu.monster.vista;

import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta exitosa de todas las operaciones (sección 8.2.7 del ERS). Los
 * campos que no aplican a la operación quedan nulos y JSON-B no los incluye en
 * el JSON; los errores salen con ErrorServicio.
 *
 * @author carlo
 */
@JsonbPropertyOrder({"estado", "codigo", "mensaje", "token", "expiraEn", "codigoEmpleado",
    "nombreEmpleado", "codigoSucursal", "sucursal", "cuenta", "movimientos", "nuevoSaldo",
    "numeroMovimiento", "numeroMovimientoDestino"})
public class RespuestaDto {

    public String estado;
    public String codigo;
    public String mensaje;

    public String token;
    public String expiraEn;
    public String codigoEmpleado;
    public String nombreEmpleado;
    public String codigoSucursal;
    public String sucursal;

    public CuentaDto cuenta;
    public List<MovimientoDto> movimientos;

    public BigDecimal nuevoSaldo;
    public Integer numeroMovimiento;
    public Integer numeroMovimientoDestino;
}
