package ec.edu.monster.vista;

import ec.edu.monster.controlador.TelefonoControlador;
import ec.edu.monster.modelo.ErrorServicioModelo;
import ec.edu.monster.modelo.TelefonoModelo;
import ec.edu.monster.util.EntradaUtil;

/** Mantenimiento del catálogo (CU-03 a CU-06): ver, agregar, editar y dar de baja teléfonos. */
public class TelefonosVista {

    private final MensajesVista mensajes = new MensajesVista();
    private final ComponentesTienda componentes = new ComponentesTienda();
    private final TelefonoControlador telefonoControlador;

    public TelefonosVista(TelefonoControlador telefonoControlador) {
        this.telefonoControlador = telefonoControlador;
    }

    public void ejecutar() {
        while (true) {
            mensajes.mostrarTitulo("TELEFONOS");
            mensajes.mostrarMensaje("    1. Ver catalogo");
            mensajes.mostrarMensaje("    2. Ver un telefono");
            mensajes.mostrarMensaje("    3. Agregar telefono");
            mensajes.mostrarMensaje("    4. Editar telefono");
            mensajes.mostrarMensaje("    5. Dar de baja");
            mensajes.mostrarMensaje("    0. Volver al menu principal");

            int opcion = EntradaUtil.leerEntero("Seleccione una opcion: ");
            try {
                switch (opcion) {
                    case 1 -> verCatalogo();
                    case 2 -> verTelefono();
                    case 3 -> agregar();
                    case 4 -> editar();
                    case 5 -> darDeBaja();
                    case 0 -> {
                        return;
                    }
                    default -> mensajes.mostrarError("Opcion no valida. Intente nuevamente.");
                }
            } catch (ErrorServicioModelo ex) {
                // AUTH-003 lo atiende el menú principal, que vuelve al inicio de sesión
                if (ex.requiereNuevoLogin()) {
                    throw ex;
                }
                mensajes.mostrarErrorServicio(ex);
            } catch (IllegalArgumentException ex) {
                mensajes.mostrarError(ex.getMessage());
            }
        }
    }

    private void verCatalogo() {
        mensajes.mostrarTitulo("TELEFONOS A LA VENTA");
        componentes.catalogo(telefonoControlador.listar());
    }

    private void verTelefono() {
        mensajes.mostrarTitulo("VER TELEFONO");
        componentes.fichaTelefono(telefonoControlador.buscar(EntradaUtil.leerEntero("Codigo del telefono: ")));
    }

    private void agregar() {
        mensajes.mostrarTitulo("AGREGAR TELEFONO");
        String marca = EntradaUtil.leerTextoNoVacio("Marca (maximo 50 caracteres): ");
        String modelo = EntradaUtil.leerTextoNoVacio("Modelo (maximo 100 caracteres): ");
        String precio = EntradaUtil.leerTextoNoVacio("Precio (por ejemplo 199.00): ");
        TelefonoModelo telefono = telefonoControlador.guardar(null, marca, modelo, precio);
        mensajes.mostrarExito(telefono.getNombre() + " se agrego al catalogo.");
        componentes.fichaTelefono(telefono);
    }

    private void editar() {
        mensajes.mostrarTitulo("EDITAR TELEFONO");
        TelefonoModelo actual = telefonoControlador.buscarActivo(EntradaUtil.leerEntero("Codigo del telefono: "));
        componentes.fichaTelefono(actual);
        mensajes.mostrarMensaje("Presione Enter para conservar el valor actual.");
        String marca = leerOConservar("Marca", actual.getMarca());
        String modelo = leerOConservar("Modelo", actual.getModelo());
        String precio = leerOConservar("Precio", actual.getPrecio().toPlainString());
        TelefonoModelo telefono = telefonoControlador.guardar(actual.getCodTelefono(), marca, modelo, precio);
        mensajes.mostrarExito(telefono.getNombre() + " se actualizo.");
        componentes.fichaTelefono(telefono);
    }

    private void darDeBaja() {
        mensajes.mostrarTitulo("DAR DE BAJA TELEFONO");
        TelefonoModelo telefono = telefonoControlador.buscarActivo(EntradaUtil.leerEntero("Codigo del telefono: "));
        componentes.fichaTelefono(telefono);
        mensajes.mostrarMensaje("Sale del punto de venta y ya no se puede vender; las facturas ya emitidas no cambian.");
        if (!EntradaUtil.leerConfirmacion("Confirma la baja?")) {
            mensajes.mostrarMensaje("Operacion cancelada. El telefono sigue a la venta.");
            return;
        }
        telefonoControlador.darDeBaja(telefono.getCodTelefono());
        mensajes.mostrarExito(telefono.getNombre() + " se dio de baja: ya no aparece en el punto de venta.");
    }

    private static String leerOConservar(String campo, String actual) {
        String valor = EntradaUtil.leerTexto(campo + " [" + MensajesVista.sinTildes(actual) + "]: ");
        return valor.isEmpty() ? actual : valor;
    }
}
