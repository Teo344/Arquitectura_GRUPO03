package ec.edu.monster.vista;

import ec.edu.monster.controlador.CatalogoControlador;
import ec.edu.monster.modelo.CategoriaModelo;
import ec.edu.monster.modelo.UnidadModelo;
import ec.edu.monster.util.EntradaUtil;
import java.util.List;

public class CatalogoVista {

    private final MensajesVista mensajes = new MensajesVista();
    private final CatalogoControlador catalogoControlador;

    public CatalogoVista(CatalogoControlador catalogoControlador) {
        this.catalogoControlador = catalogoControlador;
    }

    public void mostrarCatalogo() {
        List<CategoriaModelo> categorias = catalogoControlador.consultarCategorias();
        if (categorias.isEmpty()) {
            mensajes.mostrarMensaje("El servidor no devolvio categorias.");
            return;
        }

        int opcion;
        do {
            mensajes.mostrarTitulo("CATALOGO DE UNIDADES");
            for (int indice = 0; indice < categorias.size(); indice++) {
                CategoriaModelo categoria = categorias.get(indice);
                String detalle = categoria.isConvertible() ? "" : " (solo consulta)";
                mensajes.mostrarMensaje((indice + 1) + ". "
                        + categoria.getNombre() + detalle);
            }
            mensajes.mostrarMensaje("0. Volver");
            opcion = EntradaUtil.leerEntero("Seleccione una categoria: ");
            if (opcion >= 1 && opcion <= categorias.size()) {
                mostrarUnidades(categorias.get(opcion - 1));
            } else if (opcion != 0) {
                mensajes.mostrarError("Categoria no valida. Elija una opcion de la lista.");
            }
        } while (opcion != 0);
    }

    private void mostrarUnidades(CategoriaModelo categoria) {
        List<UnidadModelo> unidades = catalogoControlador
                .consultarUnidades(categoria.getCodigo());
        mensajes.mostrarTitulo("UNIDADES DE " + categoria.getNombre().toUpperCase());
        for (UnidadModelo unidad : unidades) {
            mensajes.mostrarMensaje(String.format("%-5s %s (%s)",
                    unidad.getCodigo(), unidad.getNombre(), unidad.getSimbolo()));
        }
    }
}
