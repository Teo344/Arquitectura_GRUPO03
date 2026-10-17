package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CategoriaModelo;
import ec.edu.monster.modelo.UnidadModelo;
import ec.edu.monster.servicio.ClienteRESTConversor;
import java.util.List;

public class CatalogoControlador {

    private final ClienteRESTConversor clienteRest;

    public CatalogoControlador(ClienteRESTConversor clienteRest) {
        this.clienteRest = clienteRest;
    }

    public List<CategoriaModelo> consultarCategorias() {
        return clienteRest.consultarCatalogo();
    }

    public List<UnidadModelo> consultarUnidades(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }
        return clienteRest.consultarUnidades(categoria);
    }
}
