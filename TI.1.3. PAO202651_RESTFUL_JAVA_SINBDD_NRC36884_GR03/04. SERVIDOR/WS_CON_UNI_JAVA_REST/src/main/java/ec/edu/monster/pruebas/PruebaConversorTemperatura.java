package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.Unidad;
import ec.edu.monster.servicios.conversores.ConversorTemperatura;

/**
 * Prueba de la conversión de temperatura con fórmulas.
 *
 * @author carlo
 */
public class PruebaConversorTemperatura {

    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== PruebaConversorTemperatura ===");
        ConversorTemperatura conversor = new ConversorTemperatura();

        comprobar("0 C -> K = 273.15", conversor.convertir(0, Unidad.C, Unidad.K), 273.15);
        comprobar("100 C -> F = 212", conversor.convertir(100, Unidad.C, Unidad.F), 212);
        comprobar("-40 C -> F = -40", conversor.convertir(-40, Unidad.C, Unidad.F), -40);
        comprobar("212 F -> RE = 80", conversor.convertir(212, Unidad.F, Unidad.RE), 80);

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
