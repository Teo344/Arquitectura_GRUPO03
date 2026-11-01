package ec.edu.monster.servicios;

import ec.edu.monster.modelo.CodigoError;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Valida el formato de las cuentas (DATA-001) y los importes (RN-01, MOV-001)
 * antes de tocar la base de datos.
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioValidacion {

    private static final Pattern FORMATO_CUENTA = Pattern.compile("\\d{8}");

    /**
     * Mayor valor que admite la columna DECIMAL(12,2) de saldos e importes.
     */
    public static final BigDecimal MAXIMO = new BigDecimal("9999999999.99");

    /**
     * Devuelve el código sin espacios si tiene 8 dígitos.
     */
    public String validarCuenta(String cuenta) throws ExcepcionServicio {
        String codigo = cuenta == null ? "" : cuenta.trim();
        if (!FORMATO_CUENTA.matcher(codigo).matches()) {
            throw new ExcepcionServicio(CodigoError.DATA_001);
        }
        return codigo;
    }

    /**
     * Importe mayor que cero, con máximo dos decimales y dentro de la columna.
     */
    public void validarImporte(BigDecimal importe) throws ExcepcionServicio {
        if (importe == null || importe.signum() <= 0 || importe.stripTrailingZeros().scale() > 2) {
            throw new ExcepcionServicio(CodigoError.MOV_001);
        }
        validarMaximo(importe);
    }

    /**
     * Un saldo que no cabe en DECIMAL(12,2) se rechaza como importe inválido.
     */
    public void validarMaximo(BigDecimal valor) throws ExcepcionServicio {
        if (valor.compareTo(MAXIMO) > 0) {
            throw new ExcepcionServicio(CodigoError.MOV_001);
        }
    }
}
