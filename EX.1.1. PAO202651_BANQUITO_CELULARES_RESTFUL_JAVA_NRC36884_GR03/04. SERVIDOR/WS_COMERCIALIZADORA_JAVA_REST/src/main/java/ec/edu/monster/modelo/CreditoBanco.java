package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Crédito que devuelve el Core: al otorgarlo (servicio 3) trae su tabla en
 * cuotas; en la lista de créditos de un cliente cuotas es null.
 *
 * @author carlo
 */
public class CreditoBanco {

    public int codCredito;
    public int codCliente;
    public LocalDate fecha;
    public BigDecimal monto;
    public int numCuotas;
    public BigDecimal tasaAnual;
    public BigDecimal valorCuota;
    public String estado;
    public List<CuotaBanco> cuotas;
}
