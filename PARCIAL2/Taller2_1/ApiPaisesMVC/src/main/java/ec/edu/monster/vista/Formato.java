package ec.edu.monster.vista;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formato de datos para mostrar al usuario (convenciones de Ecuador: punto
 * como separador de miles y coma decimal).
 *
 * @author carlo
 */
public final class Formato {

    private static final Locale ECUADOR = Locale.of("es", "EC");
    private static final String SIN_DATO = "—";

    private Formato() {
    }

    /** 18001000 → "18.001.000". */
    public static String entero(Object valor) {
        if (!(valor instanceof Number numero)) {
            return SIN_DATO;
        }
        NumberFormat formato = NumberFormat.getIntegerInstance(ECUADOR);
        formato.setGroupingUsed(true);
        return formato.format(numero.longValue());
    }

    /** 0.236 → "23,6 %". */
    public static String porcentaje(double fraccion) {
        NumberFormat formato = NumberFormat.getNumberInstance(ECUADOR);
        formato.setMaximumFractionDigits(1);
        formato.setMinimumFractionDigits(1);
        return formato.format(fraccion * 100) + " %";
    }

    /** Texto o una raya larga si no hay dato (así un vacío no parece un error). */
    public static String texto(Object valor) {
        if (valor == null) {
            return SIN_DATO;
        }
        String texto = valor.toString();
        return texto.isBlank() ? SIN_DATO : texto;
    }

    /** "1 país" / "250 países". */
    public static String paises(long cantidad) {
        return entero(cantidad) + (cantidad == 1 ? " país" : " países");
    }
}
