package ec.edu.monster.servicios;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.Unidad;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Catálogo de categorías (RQF-002) y unidades por categoría (RQF-003).
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioCatalogo {

    @Inject
    private ServicioValidacion validacion;

    public List<Categoria> categorias() {
        return List.of(Categoria.values());
    }

    public List<Unidad> unidades(String categoria) throws ExcepcionServicio {
        return Unidad.deCategoria(validacion.validarCategoria(categoria));
    }
}
