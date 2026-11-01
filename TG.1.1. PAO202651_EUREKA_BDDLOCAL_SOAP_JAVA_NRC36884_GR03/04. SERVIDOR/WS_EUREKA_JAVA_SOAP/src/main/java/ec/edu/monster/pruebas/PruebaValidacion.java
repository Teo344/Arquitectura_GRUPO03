package ec.edu.monster.pruebas;

import ec.edu.monster.servicios.ExcepcionServicio;
import ec.edu.monster.servicios.ServicioValidacion;
import java.math.BigDecimal;

/**
 * Prueba de la validación de cuentas (DATA-001) e importes (MOV-001).
 *
 * @author carlo
 */
public class PruebaValidacion {

    private static int fallidas = 0;
    private static final ServicioValidacion VALIDACION = new ServicioValidacion();

    public static void main(String[] args) {
        System.out.println("=== PruebaValidacion ===");
        try {
            comprobar("' 00100001 ' es una cuenta válida y se recorta",
                    VALIDACION.validarCuenta(" 00100001 ").equals("00100001"));
        } catch (ExcepcionServicio e) {
            comprobar("' 00100001 ' es una cuenta válida y se recorta", false);
        }
        cuentaInvalida("1234");
        cuentaInvalida("0010000A");
        cuentaInvalida(null);

        importeValido("10.50");
        importeValido("10.500");
        importeInvalido("0");
        importeInvalido("-1");
        importeInvalido("1.234");
        importeInvalido(null);
        importeInvalido("10000000000.00");

        System.out.println(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static void cuentaInvalida(String cuenta) {
        try {
            VALIDACION.validarCuenta(cuenta);
            comprobar("cuenta '" + cuenta + "' da DATA-001", false);
        } catch (ExcepcionServicio e) {
            comprobar("cuenta '" + cuenta + "' da DATA-001", e.getFaultInfo().codigo.equals("DATA-001"));
        }
    }

    private static void importeValido(String importe) {
        try {
            VALIDACION.validarImporte(new BigDecimal(importe));
            comprobar("importe " + importe + " es válido", true);
        } catch (ExcepcionServicio e) {
            comprobar("importe " + importe + " es válido", false);
        }
    }

    private static void importeInvalido(String importe) {
        try {
            VALIDACION.validarImporte(importe == null ? null : new BigDecimal(importe));
            comprobar("importe " + importe + " da MOV-001", false);
        } catch (ExcepcionServicio e) {
            comprobar("importe " + importe + " da MOV-001", e.getFaultInfo().codigo.equals("MOV-001"));
        }
    }

    private static void comprobar(String descripcion, boolean correcto) {
        if (!correcto) {
            fallidas++;
        }
        System.out.println((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
