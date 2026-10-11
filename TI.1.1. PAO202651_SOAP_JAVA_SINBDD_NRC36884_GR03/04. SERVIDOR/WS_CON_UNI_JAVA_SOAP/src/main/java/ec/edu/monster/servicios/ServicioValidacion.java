package ec.edu.monster.servicios;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.Unidad;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Valida categoría, unidades y valor de una solicitud (RQF-008).
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioValidacion {

    public Categoria validarCategoria(String codigo) throws ExcepcionServicio {
        return Categoria.desdeCodigo(codigo)
                .orElseThrow(() -> new ExcepcionServicio(CodigoError.DATA_001));
    }

    /**
     * Devuelve {origen, destino} si la solicitud es válida para la categoría.
     */
    public Unidad[] validarConversion(Categoria categoria, Double valor, String origen, String destino)
            throws ExcepcionServicio {
        if (categoria == null || valor == null || valor.isNaN() || valor.isInfinite()) {
            throw new ExcepcionServicio(CodigoError.DATA_001);
        }
        Unidad unidadOrigen = validarUnidad(origen, categoria);
        Unidad unidadDestino = validarUnidad(destino, categoria);
        if (!categoria.isConvertible()) {
            throw new ExcepcionServicio(CodigoError.DATA_001);
        }
        boolean admiteNegativos = categoria == Categoria.TEMPERATURA;
        if (!admiteNegativos && valor < 0) {
            throw new ExcepcionServicio(CodigoError.DATA_001);
        }
        return new Unidad[]{unidadOrigen, unidadDestino};
    }

    /**
     * Una unidad desconocida o de otra categoría es UNIT-001.
     */
    private Unidad validarUnidad(String codigo, Categoria categoria) throws ExcepcionServicio {
        return Unidad.desdeCodigo(codigo)
                .filter(unidad -> unidad.getCategoria() == categoria)
                .orElseThrow(() -> new ExcepcionServicio(CodigoError.UNIT_001));
    }
}
