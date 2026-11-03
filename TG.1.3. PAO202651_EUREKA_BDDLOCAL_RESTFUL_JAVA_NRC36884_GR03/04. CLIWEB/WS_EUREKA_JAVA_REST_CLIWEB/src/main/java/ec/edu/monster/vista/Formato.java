package ec.edu.monster.vista;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatos de pantalla del cliente de escritorio (Componentes.formatoImporte,
 * horaLocal y valorO). Las JSP los usan como funciones EL declaradas en
 * WEB-INF/eureka.tld: ${e:importe(saldo)}.
 */
public final class Formato {

    private static final DecimalFormatSymbols SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.US);
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());

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

    public static String valorO(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }
}
