package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Respuesta del banco sobre un comprador: si es cliente, si es sujeto de
 * crédito (con los motivos si no lo es) y su monto máximo de crédito.
 */
public class EvaluacionClienteModelo {

    /** Una regla de crédito que no se cumple (NO_ES_CLIENTE, CREDITO_ACTIVO...). */
    public static class Motivo {

        private final String codigo;
        private final String descripcion;

        public Motivo(String codigo, String descripcion) {
            this.codigo = codigo;
            this.descripcion = descripcion;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    private final String cedula;
    private final Integer codCliente;
    private final String nombre;
    private final boolean sujetoCredito;
    private final List<Motivo> motivos;
    private final BigDecimal montoMaximo;

    public EvaluacionClienteModelo(String cedula, Integer codCliente, String nombre, boolean sujetoCredito,
            List<Motivo> motivos, BigDecimal montoMaximo) {
        this.cedula = cedula;
        this.codCliente = codCliente;
        this.nombre = nombre;
        this.sujetoCredito = sujetoCredito;
        this.motivos = List.copyOf(motivos);
        this.montoMaximo = montoMaximo;
    }

    public String getCedula() {
        return cedula;
    }

    public Integer getCodCliente() {
        return codCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isSujetoCredito() {
        return sujetoCredito;
    }

    public List<Motivo> getMotivos() {
        return motivos;
    }

    public BigDecimal getMontoMaximo() {
        return montoMaximo;
    }

    /** Solo los clientes del banco pueden comprar, en efectivo o a crédito. */
    public boolean isCliente() {
        return codCliente != null;
    }
}
