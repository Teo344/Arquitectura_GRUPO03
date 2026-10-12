package ec.edu.monster.controlador;

import client.ws.ClienteWSConversor;
import ec.edu.monster.modelo.CategoriaModelo;
import ec.edu.monster.modelo.UnidadModelo;
import java.util.List;

public class CatalogoControlador {

    private final ClienteWSConversor clienteWs;

    public CatalogoControlador(ClienteWSConversor clienteWs) {
        this.clienteWs = clienteWs;
    }

    public List<CategoriaModelo> consultarCategorias() {
        return clienteWs.consultarCatalogo().stream()
                .map(categoria -> new CategoriaModelo(
                        categoria.codigo,
                        categoria.nombre,
                        Boolean.TRUE.equals(categoria.convertible)))
                .toList();
    }

    public List<UnidadModelo> consultarUnidades(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una categoria.");
        }

        return clienteWs.consultarUnidades(categoria).stream()
                .map(unidad -> new UnidadModelo(unidad.codigo, unidad.nombre, unidad.simbolo))
                .toList();
    }
}
