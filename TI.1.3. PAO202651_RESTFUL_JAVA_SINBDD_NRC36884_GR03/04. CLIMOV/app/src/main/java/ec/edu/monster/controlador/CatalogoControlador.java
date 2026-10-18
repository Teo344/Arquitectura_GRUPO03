package ec.edu.monster.controlador;

import android.os.Handler;
import android.os.Looper;
import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.SesionUsuario;
import ec.edu.monster.modelo.Unidad;
import ec.edu.monster.servicio.ConversionRestService;
import ec.edu.monster.servicio.ExcepcionServicio;
import ec.edu.monster.vista.CatalogoActivity;
import java.util.List;

/** Controla la consulta del catálogo y de unidades mediante el servicio REST protegido. */
public class CatalogoControlador {

    private final CatalogoActivity vista;
    private final ConversionRestService servicio;
    private final SesionUsuario sesion;
    private final Handler hiloPrincipal = new Handler(Looper.getMainLooper());
    private volatile boolean activo = true;
    private Categoria categoriaActual;

    public CatalogoControlador(CatalogoActivity vista, ConversionRestService servicio, SesionUsuario sesion) {
        this.vista = vista;
        this.servicio = servicio;
        this.sesion = sesion;
    }

    public void cargarCatalogo() {
        vista.setProcesando(true);
        vista.mostrarEstado("Consultando el catálogo del servidor...", false);

        new Thread(() -> {
            try {
                List<Categoria> categorias = servicio.consultarCatalogo(sesion.getToken());
                enPantalla(() -> {
                    vista.setProcesando(false);
                    vista.mostrarCategorias(categorias);
                    vista.mostrarEstado("Se cargaron " + categorias.size() + " categorías desde el servidor.", false);
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
        vista.seleccionarCategoria(categoria);
        vista.setProcesando(true);
        vista.mostrarEstado("Consultando unidades de " + categoria.getNombre() + "...", false);

        new Thread(() -> {
            try {
                List<Unidad> unidades = servicio.consultarUnidades(sesion.getToken(), categoria.getCodigo());
                enPantalla(() -> {
                    // Si el usuario ya eligió otra categoría se descarta esta respuesta
                    if (categoria != categoriaActual) {
                        return;
                    }
                    vista.setProcesando(false);
                    vista.mostrarUnidades(unidades);
                    vista.mostrarEstado("Unidades consultadas: " + categoria.getNombre() + ".", false);
                });
            } catch (Exception ex) {
                mostrarError(ex);
            }
        }).start();
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
}
