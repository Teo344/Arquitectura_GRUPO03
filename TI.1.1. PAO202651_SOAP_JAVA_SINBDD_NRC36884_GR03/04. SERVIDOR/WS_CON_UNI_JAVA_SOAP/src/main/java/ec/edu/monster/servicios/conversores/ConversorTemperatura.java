package ec.edu.monster.servicios.conversores;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.Unidad;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Conversión de temperatura con fórmulas, pasando por Celsius (RQF-006).
 *
 * @author carlo
 */
@ApplicationScoped
public class ConversorTemperatura implements Conversor {

    @Override
    public boolean soporta(Categoria categoria) {
        return categoria == Categoria.TEMPERATURA;
    }

    @Override
    public double convertir(double valor, Unidad origen, Unidad destino) {
        return desdeCelsius(aCelsius(valor, origen), destino);
    }

    private static double aCelsius(double v, Unidad unidad) {
        return switch (unidad) {
            case C -> v;
            case K -> v - 273.15;
            case F -> (v - 32) * 5 / 9;
            case N -> v * 100 / 33;
            case RE -> v * 5 / 4;
            default -> throw new IllegalArgumentException("No es unidad de temperatura: " + unidad);
        };
    }

    private static double desdeCelsius(double c, Unidad unidad) {
        return switch (unidad) {
            case C -> c;
            case K -> c + 273.15;
            case F -> c * 9 / 5 + 32;
            case N -> c * 33 / 100;
            case RE -> c * 4 / 5;
            default -> throw new IllegalArgumentException("No es unidad de temperatura: " + unidad);
        };
    }
}
