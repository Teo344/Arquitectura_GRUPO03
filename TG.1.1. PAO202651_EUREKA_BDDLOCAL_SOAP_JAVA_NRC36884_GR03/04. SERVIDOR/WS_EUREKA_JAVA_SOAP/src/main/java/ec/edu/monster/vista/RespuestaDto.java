package ec.edu.monster.vista;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta exitosa de todas las operaciones (sección 8.2.7 del ERS). Los
 * campos que no aplican a la operación quedan nulos y no se serializan; los
 * errores viajan como SOAPFault con ErrorServicio.
 *
 * @author carlo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "respuesta", propOrder = {"estado", "codigo", "mensaje", "token", "expiraEn",
    "codigoEmpleado", "nombreEmpleado", "codigoSucursal", "sucursal", "cuenta", "movimientos",
    "nuevoSaldo", "numeroMovimiento", "numeroMovimientoDestino"})
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

    @XmlElementWrapper(name = "movimientos")
    @XmlElement(name = "movimiento")
    public List<MovimientoDto> movimientos;

    public BigDecimal nuevoSaldo;
    public Integer numeroMovimiento;
    public Integer numeroMovimientoDestino;
}
