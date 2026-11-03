package ec.edu.monster.modelo;

/** Operaciones monetarias que ofrece TransaccionWS (RQF-005 a RQF-007). */
public enum TipoTransaccion {
    DEPOSITO("Depósito"),
    RETIRO("Retiro"),
    TRANSFERENCIA("Transferencia");

    private final String nombreVisible;

    TipoTransaccion(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
