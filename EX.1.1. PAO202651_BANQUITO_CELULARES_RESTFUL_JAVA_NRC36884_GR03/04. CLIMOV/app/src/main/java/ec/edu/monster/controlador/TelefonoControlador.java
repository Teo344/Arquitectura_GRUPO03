package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionModelo;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.servicio.TelefonoRestService;
import ec.edu.monster.vista.TelefonosActivity;
import java.math.BigDecimal;

/**
 * Mantenimiento del catálogo (CU-03 a CU-06): lista, agrega, edita y da de
 * baja. La baja es lógica: el teléfono queda INACTIVO y las facturas antiguas
 * no cambian.
 */
public class TelefonoControlador extends ControladorBase {

    private static final int LARGO_MARCA = 50;
    private static final int LARGO_MODELO = 100;

    private final TelefonosActivity vista;
    private final TelefonoRestService telefonos;
    private final SesionModelo sesion;

    public TelefonoControlador(TelefonosActivity vista, TelefonoRestService telefonos, SesionModelo sesion) {
        this.vista = vista;
        this.telefonos = telefonos;
        this.sesion = sesion;
    }

    public void cargar() {
        ejecutar(vista, vista::setCargando, () -> telefonos.listar(sesion.getToken()), vista::mostrarTelefonos);
    }

    /** Trae el teléfono vigente y lo carga en el formulario; solo se editan los que siguen a la venta. */
    public void editar(int codTelefono) {
        ejecutar(vista, () -> telefonos.buscar(sesion.getToken(), codTelefono), telefono -> {
            if (!telefono.isActivo()) {
                vista.mostrarMensaje(telefono.getNombre() + " ya no está a la venta.", true);
                cargar();
                return;
            }
            vista.cargarFormulario(telefono);
        });
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
                ? telefonos.crear(sesion.getToken(), marca, modelo, precio)
                : telefonos.actualizar(sesion.getToken(), codigo, marca, modelo, precio), telefono -> {
                    vista.limpiarFormulario();
                    vista.mostrarMensaje(telefono.getNombre()
                            + (codigo == null ? " se agregó al catálogo." : " se actualizó."), false);
                    cargar();
                });
    }

    public void darDeBaja(TelefonoModelo telefono) {
        ejecutar(vista, () -> {
            telefonos.eliminar(sesion.getToken(), telefono.getCodTelefono());
            return telefono;
        }, eliminado -> {
            vista.limpiarFormularioSiEdita(eliminado.getCodTelefono());
            vista.mostrarMensaje(eliminado.getNombre() + " se dio de baja: ya no aparece en el punto de venta.", false);
            cargar();
        });
    }
}
