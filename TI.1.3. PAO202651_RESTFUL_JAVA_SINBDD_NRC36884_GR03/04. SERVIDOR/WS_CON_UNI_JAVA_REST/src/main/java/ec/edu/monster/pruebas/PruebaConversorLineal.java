package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.Unidad;
import ec.edu.monster.servicios.conversores.ConversorLineal;

/**
 * Prueba de la conversión de longitud y masa por factor.
 *
 * @author carlo
 */
public class PruebaConversorLineal {

    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== PruebaConversorLineal ===");
        ConversorLineal conversor = new ConversorLineal();

        comprobar("1 KM -> M = 1000", conversor.convertir(1, Unidad.KM, Unidad.M), 1000);
        comprobar("1 MI -> M = 1609.344", conversor.convertir(1, Unidad.MI, Unidad.M), 1609.344);
        comprobar("1 QQ -> LB = 100", conversor.convertir(1, Unidad.QQ, Unidad.LB), 100);
        comprobar("1000 G -> KG = 1", conversor.convertir(1000, Unidad.G, Unidad.KG), 1);

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static void comprobar(String descripcion, double obtenido, double esperado) {
        boolean correcto = Math.abs(obtenido - esperado) < 0.000001;
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion + " -> " + obtenido);
    }
}
