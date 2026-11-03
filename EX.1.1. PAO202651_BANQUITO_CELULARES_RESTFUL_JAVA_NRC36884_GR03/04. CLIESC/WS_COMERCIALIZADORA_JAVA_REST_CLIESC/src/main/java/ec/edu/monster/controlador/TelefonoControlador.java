package ec.edu.monster.controlador;

import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.servicio.ClienteRESTMonsterCell;
import ec.edu.monster.vista.Formato;
import ec.edu.monster.vista.TelefonosVista;
import java.math.BigDecimal;
import java.util.List;

/**
 * Mantenimiento del catálogo (CU-03 a CU-06): lista, agrega, edita y da de
 * baja. La baja es lógica: el teléfono queda INACTIVO y las facturas antiguas
 * no cambian.
 */
public class TelefonoControlador extends ControladorBase {

    private static final int LARGO_MARCA = 50;
    private static final int LARGO_MODELO = 100;

    private final TelefonosVista vista;
    private final ClienteRESTMonsterCell clienteRest;

    public TelefonoControlador(TelefonosVista vista, ClienteRESTMonsterCell clienteRest) {
        this.vista = vista;
        this.clienteRest = clienteRest;
    }

    public void cargar() {
        ejecutar(vista, vista::setCargando, clienteRest::telefonos, vista::mostrarTelefonos);
    }

    /** Con codigo null agrega un teléfono; si no, reemplaza marca, modelo y precio de ese teléfono. */
    public void guardar(Integer codigo, String textoMarca, String textoModelo, String textoPrecio) {
        final String marca;
        final String modelo;
        final BigDecimal precio;
        try {
            marca = Validacion.texto(textoMarca, "la marca", LARGO_MARCA);
            modelo = Validacion.texto(textoModelo, "el modelo", LARGO_MODELO);
            precio = Validacion.importe(textoPrecio, "precio");
        } catch (IllegalArgumentException ex) {
            vista.mostrarMensaje(ex.getMessage(), true);
            return;
        }
        ejecutar(vista, vista::setGuardando, () -> codigo == null
                ? clienteRest.crearTelefono(marca, modelo, precio)
                : clienteRest.actualizarTelefono(codigo, marca, modelo, precio), telefono -> {
                    vista.limpiarFormulario();
                    vista.mostrarMensaje(telefono.getNombre()
                            + (codigo == null ? " se agregó al catálogo." : " se actualizó."), false);
                    cargar();
                });
    }

    /** Pide confirmación con los datos del teléfono y lo da de baja. */
    public void darDeBaja(TelefonoModelo telefono) {
        boolean confirmado = vista.confirmarBaja(List.of(
                new String[]{"Marca", telefono.getMarca()},
                new String[]{"Modelo", telefono.getModelo()},
                new String[]{"Precio", Formato.importe(telefono.getPrecio())}));
        if (!confirmado) {
            return;
        }
        ejecutar(vista, () -> {
            clienteRest.eliminarTelefono(telefono.getCodTelefono());
            return telefono;
        }, eliminado -> {
            vista.limpiarFormularioSiEdita(eliminado.getCodTelefono());
            vista.mostrarMensaje(eliminado.getNombre() + " se dio de baja: ya no aparece en el punto de venta.", false);
            cargar();
        });
    }
}
