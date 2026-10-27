package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioValidacion;

/**
 * Prueba de la validación de categorías, unidades y valores.
 *
 * @author carlo
 */
public class PruebaValidacion {

    private static int fallidas = 0;

    public static void main(String[] args) {
        System.out.println("=== PruebaValidacion ===");
        ServicioValidacion validacion = new ServicioValidacion();

        try {
            comprobar("'longitud' es una categoría válida", validacion.validarCategoria("longitud") == Categoria.LONGITUD);
        } catch (ExcepcionServicio e) {
            comprobar("'longitud' es una categoría válida", false);
        }

        try {
            validacion.validarCategoria("xyz");
            comprobar("categoría 'xyz' da DATA-001", false);
        } catch (ExcepcionServicio e) {
            comprobar("categoría 'xyz' da DATA-001", e.getFaultInfo().codigo.equals("DATA-001"));
        }

        try {
            validacion.validarConversion(Categoria.LONGITUD, -1.0, "KM", "M");
            comprobar("valor negativo en longitud da DATA-001", false);
        } catch (ExcepcionServicio e) {
            comprobar("valor negativo en longitud da DATA-001", e.getFaultInfo().codigo.equals("DATA-001"));
        }

        try {
            validacion.validarConversion(Categoria.LONGITUD, 1.0, "KG", "M");
            comprobar("KG en longitud da UNIT-001", false);
        } catch (ExcepcionServicio e) {
            comprobar("KG en longitud da UNIT-001", e.getFaultInfo().codigo.equals("UNIT-001"));
        }

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
