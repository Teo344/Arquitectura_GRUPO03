package ec.edu.monster.vista;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Formatos de pantalla, los mismos de los clientes de EurekaBank. Las JSP los
 * usan como funciones EL declaradas en WEB-INF/monstercell.tld:
 * ${m:importe(total)}.
 */
public final class Formato {

    private static final DecimalFormatSymbols SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.US);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Formato() {
    }

    /** Importe con separador de miles y dos decimales: 6,900.00 */
    public static String importe(BigDecimal importe) {
        return importe == null ? "-" : new DecimalFormat("#,##0.00", SIMBOLOS)
                .format(importe.setScale(2, RoundingMode.HALF_UP));
    }

    /** Hora local (HH:mm) del instante en que caduca la sesión. */
    public static String hora(Instant instante) {
        return instante == null ? "-" : FORMATO_HORA.format(instante);
    }

    /**
     * Fecha ISO-8601 de la API para mostrar: "2026-11-03T10:15:30" da
     * "03/11/2026 10:15" y "2026-11-03" da "03/11/2026".
     */
    public static String fecha(String iso) {
        if (iso == null || iso.isBlank()) {
            return "-";
        }
        try {
            return iso.contains("T") ? LocalDateTime.parse(iso).format(FORMATO_FECHA_HORA)
                    : LocalDate.parse(iso).format(FORMATO_FECHA);
        } catch (DateTimeParseException ex) {
            return iso;
        }
    }

    public static String valorO(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    /** Iniciales del nombre para el avatar: "Operador Monster Grupo03" da "OM". */
    public static String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        StringBuilder iniciales = new StringBuilder();
        for (String palabra : nombre.trim().split("\\s+")) {
            if (iniciales.length() < 2) {
                iniciales.append(palabra.charAt(0));
            }
        }
        return iniciales.toString().toUpperCase(Locale.ROOT);
    }
}
