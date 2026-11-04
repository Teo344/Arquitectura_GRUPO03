package ec.edu.monster.util;

import java.io.Console;
import java.util.Scanner;

/** Lectura de datos desde el teclado; solo convierte tipos, las reglas las valida el controlador. */
public final class EntradaUtil {

    private static final Scanner ENTRADA = new Scanner(System.in);

    private EntradaUtil() {
    }

    /** Texto que puede quedar vacío: filtros opcionales y "Enter conserva el valor". */
    public static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return ENTRADA.nextLine().trim();
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

    /**
     * La clave no se muestra cuando el programa corre en una terminal (cmd o
     * PowerShell). En la ventana Output de NetBeans no hay consola y se lee
     * como texto normal.
     */
    public static String leerClave(String mensaje) {
        Console consola = System.console();
        while (true) {
            String clave;
            if (consola != null) {
                char[] caracteres = consola.readPassword(mensaje);
                clave = caracteres == null ? "" : new String(caracteres);
            } else {
                System.out.print(mensaje);
                clave = ENTRADA.nextLine();
            }
            if (!clave.isEmpty()) {
                return clave;
            }
            System.err.println("Entrada invalida. La clave no puede quedar vacia.");
        }
    }

    public static boolean leerConfirmacion(String mensaje) {
        while (true) {
            System.out.print(mensaje + " (S/N): ");
            String valor = ENTRADA.nextLine().trim();
            if (valor.equalsIgnoreCase("S")) {
                return true;
            }
            if (valor.equalsIgnoreCase("N")) {
                return false;
            }
            System.err.println("Entrada invalida. Responda S o N.");
        }
    }
}
