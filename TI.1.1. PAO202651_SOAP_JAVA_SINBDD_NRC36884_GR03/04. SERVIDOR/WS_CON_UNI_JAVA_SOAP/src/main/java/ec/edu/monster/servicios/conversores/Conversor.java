package ec.edu.monster.servicios.conversores;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.Unidad;

/**
 * Estrategia de cálculo para una o más categorías.
 *
 * @author carlo
 */
public interface Conversor {

    boolean soporta(Categoria categoria);

    double convertir(double valor, Unidad origen, Unidad destino);
}
