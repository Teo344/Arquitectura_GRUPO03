package ec.edu.monster.servicios.conversores;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.Unidad;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Conversión por factor a la unidad base: longitud (RQF-004) y masa (RQF-005).
 *
 * @author carlo
 */
@ApplicationScoped
public class ConversorLineal implements Conversor {

    @Override
    public boolean soporta(Categoria categoria) {
        return categoria == Categoria.LONGITUD || categoria == Categoria.MASA;
    }

    @Override
    public double convertir(double valor, Unidad origen, Unidad destino) {
        return valor * origen.getFactor() / destino.getFactor();
    }
}
