package ec.edu.monster.util;

import java.util.Scanner;

public final class EntradaUtil {

    private static final Scanner ENTRADA = new Scanner(System.in);

    private EntradaUtil() {
    }

    public static String leerTextoNoVacio(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String texto = ENTRADA.nextLine().trim();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.err.println("Entrada invalida. El valor no puede quedar vacio.");
        }
    }

    public static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = ENTRADA.nextLine().trim();
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException ex) {
                System.err.println("Entrada invalida. Debe ingresar un numero entero.");
            }
        }
    }

    public static double leerDecimal(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = ENTRADA.nextLine().trim().replace(',', '.');
            try {
                return Double.parseDouble(valor);
            } catch (NumberFormatException ex) {
                System.err.println("Entrada invalida. Debe ingresar un numero decimal.");
            }
        }
    }
}
