package ec.edu.monster.controlador;

import android.os.Handler;
import android.os.Looper;
import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.ResultadoConversion;
import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.modelo.Unidad;
import ec.edu.monster.servicio.ConversionRestService;
import ec.edu.monster.servicio.ExcepcionServicio;
import ec.edu.monster.vista.ConversionActivity;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Controla la carga de catálogo y las conversiones solicitadas desde la vista. */
public class ConversionControlador {

    private final ConversionActivity vista;
    private final ConversionRestService servicio;
    private final SesionUsuario sesion;
    private final Handler hiloPrincipal = new Handler(Looper.getMainLooper());
    private volatile boolean activo = true;
    private Categoria categoriaActual;

    public ConversionControlador(ConversionActivity vista, ConversionRestService servicio, SesionUsuario sesion) {
        this.vista = vista;
        this.servicio = servicio;
        this.sesion = sesion;
    }

    public void cargarCategorias() {
        vista.setProcesando(true);

        new Thread(() -> {
            try {
                List<Categoria> categorias = servicio.consultarCategorias(sesion.getToken());
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.mostrarCategorias(categorias);
                    vista.mostrarEstado("Selecciona una categoría para comenzar.", false);
                    if (!categorias.isEmpty()) {
                        seleccionarCategoria(categorias.get(0));
                    }
                });
            } catch (Exception ex) {
                mostrarError(ex);
            }
        }).start();
    }

    public void seleccionarCategoria(Categoria categoria) {
        if (categoria == null) {
            return;
        }
        categoriaActual = categoria;
        vista.mostrarUnidades(Collections.emptyList());
        vista.setProcesando(true);

        new Thread(() -> {
            try {
                List<Unidad> unidades = servicio.consultarUnidades(sesion.getToken(), categoria.getCodigo());
                enPantalla(() -> {
                    // Si el usuario ya eligió otra categoría se descarta esta respuesta
                    if (categoria == categoriaActual) {
                        vista.setProcesando(false);
                        vista.mostrarUnidades(unidades);
                    }
                });
            } catch (Exception ex) {
                mostrarError(ex);
            }
        }).start();
    }

    public void convertir(Categoria categoria, Unidad origen, Unidad destino, String textoValor) {
        if (categoria == null || origen == null || destino == null) {
            vista.mostrarEstado("No hay categoría o unidades disponibles.", true);
            return;
        }

        final double valor;
        try {
            valor = Double.parseDouble(textoValor.trim().replace(',', '.'));
            if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            vista.mostrarEstado("Ingresa un valor numérico válido.", true);
            return;
        }

        // Mientras procesa, la vista bloquea el formulario: la respuesta corresponde a estos datos
        vista.setProcesando(true);

        new Thread(() -> {
            try {
                ResultadoConversion respuesta = servicio.convertir(sesion.getToken(), categoria.getCodigo(),
                        valor, origen.getCodigo(), destino.getCodigo());
                enPantalla(() -> {
                    vista.setProcesando(false);
                    String resultado = formatear(respuesta.getResultado()) + " " + destino.getSimbolo();
                    String detalle = formatear(valor) + " " + origen.getSimbolo() + " → " + resultado;
                    String fecha = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US).format(new Date());
                    vista.mostrarResultado(resultado);
                    vista.agregarAlHistorial(fecha, detalle);
                    vista.mostrarEstado("Resultado: " + resultado, false);
                });
            } catch (Exception ex) {
                mostrarError(ex);
            }
        }).start();
    }

    public void limpiar() {
        vista.limpiarFormulario();
        vista.mostrarEstado("Listo para convertir", false);
    }

    /** La vista se cerró: las respuestas que lleguen después se descartan. */
    public void detener() {
        activo = false;
        hiloPrincipal.removeCallbacksAndMessages(null);
    }

    private void mostrarError(Exception error) {
        enPantalla(() -> {
            vista.setProcesando(false);
            if (ExcepcionServicio.esSesionInvalida(error)) {
                vista.volverAlLogin(ExcepcionServicio.mensajeAmigable(error) + ". Inicia sesión nuevamente.");
                return;
            }
            vista.mostrarEstado(ExcepcionServicio.mensajeAmigable(error), true);
        });
    }

    private void enPantalla(Runnable accion) {
        hiloPrincipal.post(() -> {
            if (activo) {
                accion.run();
            }
        });
    }

    private String formatear(double valor) {
        DecimalFormat formato = new DecimalFormat("0.########", DecimalFormatSymbols.getInstance(Locale.US));
        return formato.format(valor);
    }
}
