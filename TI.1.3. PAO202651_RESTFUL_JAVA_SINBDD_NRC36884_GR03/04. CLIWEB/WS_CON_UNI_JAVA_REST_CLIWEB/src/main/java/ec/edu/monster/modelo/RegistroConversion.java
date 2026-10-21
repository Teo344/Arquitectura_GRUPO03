package ec.edu.monster.modelo;

/** Entrada del historial de conversiones de la vista del conversor. */
public final class RegistroConversion {
    private final String fecha;
    private final String detalle;

    public RegistroConversion(String fecha, String detalle) {
        this.fecha = fecha;
        this.detalle = detalle;
    }

    public String getFecha() { return fecha; }
    public String getDetalle() { return detalle; }
}
