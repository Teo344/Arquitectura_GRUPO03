package ec.edu.monster.controlador;

import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.servicio.Categoria;
import ec.edu.monster.servicio.ConversionSoapService;
import ec.edu.monster.servicio.Respuesta;
import ec.edu.monster.servicio.Unidad;
import ec.edu.monster.vista.ConversionView;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/** Controla la carga de catálogo y las conversiones solicitadas desde la vista. */
public class ConversionController {
    private final ConversionView vista;
    private final ConversionSoapService servicio;
    private boolean cargandoCategorias;

    public ConversionController(ConversionView vista, ConversionSoapService servicio) {
        this.vista = vista;
        this.servicio = servicio;
        vista.getBotonConvertir().addActionListener(this::convertir);
        vista.getBotonLimpiar().addActionListener(e -> limpiar());
        vista.getBotonVolver().addActionListener(e -> volverAlInicio());
        vista.getComboCategoria().addActionListener(e -> cargarUnidades());
        cargarCategorias();
    }

    private void cargarCategorias() {
        cargandoCategorias = true;
        vista.setProcesando(true);
        new SwingWorker<java.util.List<Categoria>, Void>() {
            @Override protected java.util.List<Categoria> doInBackground() throws Exception {
                return servicio.consultarCategorias(vista.getSesion().getToken());
            }
            @Override protected void done() {
                try {
                    vista.mostrarCategorias(get());
                    vista.mostrarEstado("Selecciona una categoría para comenzar.", false);
                } catch (Exception ex) {
                    mostrarError(ex);
                } finally {
                    cargandoCategorias = false;
                    vista.setProcesando(false);
                    cargarUnidades();
                }
            }
        }.execute();
    }

    private void cargarUnidades() {
        if (cargandoCategorias) return;
        Categoria categoria = (Categoria) vista.getComboCategoria().getSelectedItem();
        if (categoria == null) return;
        vista.getComboOrigen().removeAllItems();
        vista.getComboDestino().removeAllItems();
        new SwingWorker<java.util.List<Unidad>, Void>() {
            @Override protected java.util.List<Unidad> doInBackground() throws Exception {
                return servicio.consultarUnidades(vista.getSesion().getToken(), categoria.getCodigo());
            }
            @Override protected void done() {
                try {
                    if (vista.getComboCategoria().getSelectedItem() == categoria) {
                        vista.mostrarUnidades(get());
                    }
                } catch (Exception ex) {
                    mostrarError(ex);
                }
            }
        }.execute();
    }

    private void convertir(ActionEvent evento) {
        Categoria categoria = (Categoria) vista.getComboCategoria().getSelectedItem();
        Unidad origen = (Unidad) vista.getComboOrigen().getSelectedItem();
        Unidad destino = (Unidad) vista.getComboDestino().getSelectedItem();
        if (categoria == null || origen == null || destino == null) {
            vista.mostrarEstado("No hay categoría o unidades disponibles.", true);
            return;
        }

        final double valor;
        try {
            valor = Double.parseDouble(vista.getCampoValor().getText().trim().replace(',', '.'));
            if (!Double.isFinite(valor)) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            vista.mostrarEstado("Ingresa un valor numérico válido.", true);
            vista.getCampoValor().requestFocusInWindow();
            return;
        }

        vista.setProcesando(true);
        new SwingWorker<Respuesta, Void>() {
            @Override protected Respuesta doInBackground() throws Exception {
                return servicio.convertir(vista.getSesion().getToken(), categoria.getCodigo(),
                        valor, origen.getCodigo(), destino.getCodigo());
            }
            @Override protected void done() {
                vista.setProcesando(false);
                try {
                    Respuesta respuesta = get();
                    if (respuesta.getResultado() == null) {
                        vista.mostrarEstado("El servidor no devolvió un resultado.", true);
                        return;
                    }
                    String resultado = formatear(respuesta.getResultado());
                    String fecha = java.time.LocalDateTime.now().format(
                            java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
                    String linea = "<span style='color:#4A5A78;font-size:11px'>" + fecha + "</span><br>"
                            + formatear(valor) + " " + origen.getSimbolo() + " → " + resultado
                            + " " + destino.getSimbolo();
                    vista.mostrarEstado("Resultado: " + resultado + " " + destino.getSimbolo(), false);
                    vista.agregarAlHistorial(linea);
                } catch (Exception ex) {
                    mostrarError(ex);
                }
            }
        }.execute();
    }

    private void limpiar() {
        vista.getCampoValor().setText("0");
        vista.getComboOrigen().setSelectedIndex(vista.getComboOrigen().getItemCount() > 0 ? 0 : -1);
        vista.getComboDestino().setSelectedIndex(vista.getComboDestino().getItemCount() > 1 ? 1 : 0);
        vista.mostrarEstado("Listo para convertir", false);
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
                && soapError.getFaultInfo() != null
                && soapError.getFaultInfo().getMensaje() != null) {
            mensaje = soapError.getFaultInfo().getMensaje();
        }
        if (mensaje == null || mensaje.isBlank()) mensaje = "No se pudo completar la operación SOAP.";
        vista.mostrarEstado(mensaje, true);
    }

    private String formatear(double valor) {
        DecimalFormat formato = new DecimalFormat("0.########", DecimalFormatSymbols.getInstance(Locale.US));
        return formato.format(valor);
    }
}
