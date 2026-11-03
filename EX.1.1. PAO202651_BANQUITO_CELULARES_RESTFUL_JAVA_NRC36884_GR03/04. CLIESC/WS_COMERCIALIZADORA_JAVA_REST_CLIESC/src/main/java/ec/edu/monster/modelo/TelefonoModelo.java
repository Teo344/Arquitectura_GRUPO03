package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Teléfono del catálogo. precioEfectivo lo calcula el servidor (42 % de
 * descuento); estado es ACTIVO o INACTIVO (dado de baja).
 */
public class TelefonoModelo {

    private final int codTelefono;
    private final String marca;
    private final String modelo;
    private final BigDecimal precio;
    private final BigDecimal precioEfectivo;
    private final String estado;

    public TelefonoModelo(int codTelefono, String marca, String modelo, BigDecimal precio, BigDecimal precioEfectivo,
            String estado) {
        this.codTelefono = codTelefono;
        this.marca = marca;
        this.modelo = modelo;
        this.precio = precio;
        this.precioEfectivo = precioEfectivo;
        this.estado = estado;
    }

    public int getCodTelefono() {
        return codTelefono;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public BigDecimal getPrecioEfectivo() {
        return precioEfectivo;
    }

    public String getEstado() {
        return estado;
    }

    public boolean isActivo() {
        return "ACTIVO".equals(estado);
    }

    /** Marca y modelo juntos, como se muestran en el carrito y la factura. */
    public String getNombre() {
        return marca + " " + modelo;
    }
}
