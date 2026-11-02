package ec.edu.monster.servicios;

import ec.edu.monster.modelo.CodigoError;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Valida el formato de los datos de entrada antes de tocar la base de datos.
 * Los números llegan como texto desde los parámetros de consulta para que un
 * valor mal escrito responda DATA-001 y no un 404 de JAX-RS.
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioValidacion {

    private static final Pattern FORMATO_CEDULA = Pattern.compile("\\d{10}");

    public static final int CUOTAS_MINIMO = 3;
    public static final int CUOTAS_MAXIMO = 18;

    /**
     * Mayor valor que admite la columna NUMERIC(10,2) de los montos.
     */
    public static final BigDecimal MAXIMO = new BigDecimal("99999999.99");

    /**
     * Devuelve la cédula sin espacios si tiene 10 dígitos.
     */
    public String validarCedula(String cedula) throws ExcepcionServicio {
        String valor = cedula == null ? "" : cedula.trim();
        if (!FORMATO_CEDULA.matcher(valor).matches()) {
            throw new ExcepcionServicio(CodigoError.DATA_001, "La cédula debe tener 10 dígitos");
        }
        return valor;
    }

    public int validarCodigo(Integer codigo) throws ExcepcionServicio {
        if (codigo == null || codigo <= 0) {
            throw new ExcepcionServicio(CodigoError.DATA_001, "El código debe ser un número mayor que cero");
        }
        return codigo;
    }

    /**
     * Monto mayor que cero, con máximo dos decimales y dentro de la columna.
     */
    public BigDecimal validarMonto(BigDecimal monto) throws ExcepcionServicio {
        if (monto == null || monto.signum() <= 0 || monto.stripTrailingZeros().scale() > 2
                || monto.compareTo(MAXIMO) > 0) {
            throw new ExcepcionServicio(CodigoError.CRE_005);
        }
        return monto.setScale(2);
    }

    public BigDecimal validarMonto(String monto) throws ExcepcionServicio {
        try {
            return validarMonto(monto == null ? null : new BigDecimal(monto.trim()));
        } catch (NumberFormatException e) {
            throw new ExcepcionServicio(CodigoError.CRE_005);
        }
    }

    public int validarCuotas(Integer cuotas) throws ExcepcionServicio {
        if (cuotas == null || cuotas < CUOTAS_MINIMO || cuotas > CUOTAS_MAXIMO) {
            throw new ExcepcionServicio(CodigoError.CRE_003);
        }
        return cuotas;
    }

    public int validarCuotas(String cuotas) throws ExcepcionServicio {
        try {
            return validarCuotas(cuotas == null ? null : Integer.valueOf(cuotas.trim()));
        } catch (NumberFormatException e) {
            throw new ExcepcionServicio(CodigoError.CRE_003);
        }
    }
}
