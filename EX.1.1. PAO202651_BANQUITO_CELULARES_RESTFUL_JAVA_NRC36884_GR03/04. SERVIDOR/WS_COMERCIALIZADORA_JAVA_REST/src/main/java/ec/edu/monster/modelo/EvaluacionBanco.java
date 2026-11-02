package ec.edu.monster.modelo;

import java.util.List;

/**
 * Respuesta del servicio 1 del Core (¿es sujeto de crédito?). codCliente es
 * null si la cédula no es de un cliente.
 *
 * @author carlo
 */
public class EvaluacionBanco {

    public static class Motivo {

        public String codigo;
        public String descripcion;
    }

    public static final String NO_ES_CLIENTE = "NO_ES_CLIENTE";

    public String cedula;
    public Integer codCliente;
    public String nombre;
    public boolean sujetoCredito;
    public List<Motivo> motivos;

    public boolean esCliente() {
        return codCliente != null;
    }
}
