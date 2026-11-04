package ec.edu.monster.controlador;

import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import java.math.BigDecimal;
import java.util.List;

/**
 * Mantenimiento del catálogo (CU-03 a CU-06): lista, consulta, agrega, edita y
 * da de baja. La baja es lógica: el teléfono queda INACTIVO y las facturas
 * antiguas no cambian.
 */
public class TelefonoControlador {

    private static final int LARGO_MARCA = 50;
    private static final int LARGO_MODELO = 100;

    private final ClienteRESTMonsterCell clienteRest;

    public TelefonoControlador(ClienteRESTMonsterCell clienteRest) {
        this.clienteRest = clienteRest;
    }

    public List<TelefonoModelo> listar() {
        return clienteRest.telefonos();
    }

    /** Devuelve el teléfono aunque esté dado de baja. */
    public TelefonoModelo buscar(int codTelefono) {
        return clienteRest.telefono(Validacion.codigo(codTelefono, "un código de teléfono"));
    }

    /** Web y escritorio solo editan y dan de baja los teléfonos que siguen a la venta. */
    public TelefonoModelo buscarActivo(int codTelefono) {
        TelefonoModelo telefono = buscar(codTelefono);
        if (!telefono.isActivo()) {
            throw new IllegalArgumentException(telefono.getNombre() + " ya no está a la venta.");
        }
        return telefono;
    }

    /** Con codigo null agrega un teléfono; si no, reemplaza marca, modelo y precio de ese teléfono. */
    public TelefonoModelo guardar(Integer codigo, String textoMarca, String textoModelo, String textoPrecio) {
        String marca = Validacion.texto(textoMarca, "la marca", LARGO_MARCA);
        String modelo = Validacion.texto(textoModelo, "el modelo", LARGO_MODELO);
        BigDecimal precio = Validacion.importe(textoPrecio, "precio");
        return codigo == null
                ? clienteRest.crearTelefono(marca, modelo, precio)
                : clienteRest.actualizarTelefono(codigo, marca, modelo, precio);
    }

    public void darDeBaja(int codTelefono) {
        clienteRest.eliminarTelefono(Validacion.codigo(codTelefono, "un código de teléfono"));
    }
}
