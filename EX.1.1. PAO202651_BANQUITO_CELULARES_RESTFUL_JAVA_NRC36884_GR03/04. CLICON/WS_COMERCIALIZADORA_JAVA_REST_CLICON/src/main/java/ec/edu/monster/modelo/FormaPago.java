package ec.edu.monster.modelo;

/** Formas de pago de una venta: en efectivo (42 % de descuento) o con crédito directo de BanQuito. */
public enum FormaPago {
    EFECTIVO("Efectivo"),
    CREDITO("Crédito BanQuito");

    private final String nombreVisible;

    FormaPago(String nombreVisible) {
        this.nombreVisible = nombreVisible;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }
}
