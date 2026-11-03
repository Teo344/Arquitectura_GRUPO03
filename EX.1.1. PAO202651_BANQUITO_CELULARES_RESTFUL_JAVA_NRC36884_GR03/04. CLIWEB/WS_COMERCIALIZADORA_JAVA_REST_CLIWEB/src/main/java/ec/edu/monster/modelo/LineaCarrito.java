package ec.edu.monster.modelo;

import java.math.BigDecimal;

/** Un teléfono del carrito con su cantidad. */
public class LineaCarrito {

    private final TelefonoModelo telefono;
    private int cantidad;

    public LineaCarrito(TelefonoModelo telefono, int cantidad) {
        this.telefono = telefono;
        this.cantidad = cantidad;
    }

    public TelefonoModelo getTelefono() {
        return telefono;
    }

    public int getCantidad() {
        return cantidad;
    }

    void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return telefono.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }
}
