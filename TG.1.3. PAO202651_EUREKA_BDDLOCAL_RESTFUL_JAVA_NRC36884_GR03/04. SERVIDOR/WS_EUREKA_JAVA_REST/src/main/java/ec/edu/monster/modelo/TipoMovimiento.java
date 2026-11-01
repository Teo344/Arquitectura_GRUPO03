package ec.edu.monster.modelo;

/**
 * Tipos de movimiento que genera el sistema (tabla tipomovimiento). Los demás
 * tipos solo aparecen en el historial.
 *
 * @author carlo
 */
public enum TipoMovimiento {

    DEPOSITO("003"),
    RETIRO("004"),
    TRANSFERENCIA_INGRESO("008"),
    TRANSFERENCIA_SALIDA("009");

    private final String codigo;

    TipoMovimiento(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
