package ec.edu.monster.controlador;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validaciones de formato que el cliente hace antes de llamar a la API, con los
 * mismos límites del servidor y los mismos mensajes del cliente web, para
 * orientar al vendedor. El servidor vuelve a validar todo.
 */
final class Validacion {

    private static final Pattern FORMATO_CEDULA = Pattern.compile("\\d{10}");
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

    private Validacion() {
    }

    /** Devuelve la cédula sin espacios si tiene 10 dígitos. */
    static String cedula(String cedula) {
        String valor = cedula == null ? "" : cedula.trim();
        if (!FORMATO_CEDULA.matcher(valor).matches()) {
            throw new IllegalArgumentException("La cédula debe tener exactamente 10 dígitos (por ejemplo 1712345678).");
        }
        return valor;
    }

    static String texto(String texto, String campo, int maximo) {
        String valor = texto == null ? "" : texto.trim();
        if (valor.isEmpty() || valor.length() > maximo) {
            throw new IllegalArgumentException("Ingresa " + campo + " (máximo " + maximo + " caracteres).");
        }
        return valor;
    }

    /**
     * Monto o precio mayor que cero con máximo dos decimales; acepta coma o
     * punto. campo es el nombre en minúsculas: "precio", "monto".
     */
    static BigDecimal importe(String texto, String campo) {
        String valor = texto == null ? "" : texto.trim().replace(',', '.');
        if (valor.isEmpty()) {
            throw new IllegalArgumentException("Ingresa el " + campo + ".");
        }
        BigDecimal numero;
        try {
            numero = new BigDecimal(valor);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Ingresa un " + campo + " numérico válido, por ejemplo 199.99");
        }
        if (numero.signum() <= 0 || numero.stripTrailingZeros().scale() > 2 || numero.compareTo(PRECIO_MAXIMO) > 0) {
            throw new IllegalArgumentException("El " + campo + " debe ser mayor que cero y tener como máximo dos decimales.");
        }
        return numero.setScale(2);
    }

    /** Código o número de un registro: el servidor solo acepta valores mayores que cero. */
    static int codigo(int valor, String campo) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Ingresa " + campo + " mayor que cero.");
        }
        return valor;
    }

    /** Entero o null si el texto no lo es (para parámetros de la URL). */
    static Integer entero(String texto) {
        try {
            return texto == null ? null : Integer.valueOf(texto.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
