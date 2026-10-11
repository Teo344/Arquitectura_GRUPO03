package ec.edu.monster.controlador;

import ec.edu.monster.servicio.Categoria;
import ec.edu.monster.servicio.ConversionSoapService;
import ec.edu.monster.servicio.Unidad;
import ec.edu.monster.vista.CatalogoView;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/** Controla la consulta del catálogo y de unidades mediante el servicio SOAP protegido. */
public class CatalogoController {
    private final CatalogoView vista;
    private final ConversionSoapService servicio;
    private java.util.List<Categoria> categorias = java.util.List.of();

    public CatalogoController(CatalogoView vista, ConversionSoapService servicio) {
        this.vista = vista;
        this.servicio = servicio;
        vista.getBotonVolver().addActionListener(e -> volverAlInicio());
        cargarCatalogo();
    }

    private void cargarCatalogo() {
        vista.mostrarEstado("Consultando el catálogo del servidor...", false);
        new SwingWorker<java.util.List<Categoria>, Void>() {
            @Override protected java.util.List<Categoria> doInBackground() throws Exception {
                return servicio.consultarCatalogo(vista.getSesion().getToken());
            }
            @Override protected void done() {
                try {
                    categorias = get();
                    vista.mostrarCategorias(categorias, CatalogoController.this::seleccionarCategoria);
                    vista.mostrarEstado("Se cargaron " + categorias.size() + " categorías desde el servidor.", false);
                } catch (Exception ex) {
                    mostrarError(ex);
                }
            }
        }.execute();
    }

    private void seleccionarCategoria(ActionEvent evento) {
        String codigo = evento.getActionCommand();
        Categoria categoria = categorias.stream()
                .filter(c -> java.util.Objects.equals(c.getCodigo(), codigo))
                .findFirst().orElse(null);
        if (categoria == null) return;
        vista.seleccionarCategoria(categoria);
        vista.mostrarEstado("Consultando unidades de " + categoria.getNombre() + "...", false);
        new SwingWorker<java.util.List<Unidad>, Void>() {
            @Override protected java.util.List<Unidad> doInBackground() throws Exception {
                return servicio.consultarUnidades(vista.getSesion().getToken(), categoria.getCodigo());
            }
            @Override protected void done() {
                try {
                    vista.mostrarUnidades(get());
                    vista.mostrarEstado("Unidades consultadas: " + categoria.getNombre() + ".", false);
                } catch (Exception ex) {
                    mostrarError(ex);
                }
            }
        }.execute();
    }

    private void volverAlInicio() {
        ec.edu.monster.vista.MenuView menu = new ec.edu.monster.vista.MenuView(vista.getSesion());
        new MenuController(menu);
        vista.dispose();
        menu.setVisible(true);
    }

    private void mostrarError(Exception error) {
        Throwable causa = error instanceof ExecutionException && error.getCause() != null
                ? error.getCause() : error;
        String mensaje = causa.getMessage();
        if (causa instanceof ec.edu.monster.servicio.ExcepcionServicio soapError
                && soapError.getFaultInfo() != null && soapError.getFaultInfo().getMensaje() != null) {
            mensaje = soapError.getFaultInfo().getMensaje();
        }
        if (mensaje == null || mensaje.isBlank()) mensaje = "No se pudo consultar el catálogo SOAP.";
        vista.mostrarEstado(mensaje, true);
    }
}
