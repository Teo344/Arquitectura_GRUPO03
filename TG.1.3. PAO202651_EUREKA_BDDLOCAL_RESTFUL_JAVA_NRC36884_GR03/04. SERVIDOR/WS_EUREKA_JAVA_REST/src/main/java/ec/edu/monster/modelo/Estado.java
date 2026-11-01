package ec.edu.monster.modelo;

/**
 * Estados de usuarios y cuentas (CHECK de las tablas usuario y cuenta).
 *
 * @author carlo
 */
public enum Estado {

    ACTIVO, ANULADO, CANCELADO;

    public static boolean esActivo(String estado) {
        return ACTIVO.name().equals(estado);
    }
}
