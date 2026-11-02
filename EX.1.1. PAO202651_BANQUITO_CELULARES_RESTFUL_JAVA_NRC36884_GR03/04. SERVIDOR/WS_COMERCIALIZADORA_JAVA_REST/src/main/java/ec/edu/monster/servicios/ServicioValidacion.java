package ec.edu.monster.servicios;

import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.FormaPago;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Valida el formato de los datos de entrada antes de tocar la base de datos
 * o llamar al Core.
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioValidacion {

    private static final Pattern FORMATO_CEDULA = Pattern.compile("\\d{10}");

    public static final int CUOTAS_MINIMO = 3;
    public static final int CUOTAS_MAXIMO = 18;
    public static final int CANTIDAD_MAXIMA = 100;

    /**
     * Mayor valor que admiten las columnas NUMERIC(10,2) de precios y totales.
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
     * Texto obligatorio de hasta maximo caracteres; devuelve el texto sin
     * espacios a los lados.
     */
    public String validarTexto(String texto, String campo, int maximo) throws ExcepcionServicio {
        String valor = texto == null ? "" : texto.trim();
        if (valor.isEmpty() || valor.length() > maximo) {
            throw new ExcepcionServicio(CodigoError.DATA_001,
                    "El campo " + campo + " es obligatorio y admite hasta " + maximo + " caracteres");
        }
        return valor;
    }

    public BigDecimal validarPrecio(BigDecimal precio) throws ExcepcionServicio {
        if (precio == null || precio.signum() <= 0 || precio.stripTrailingZeros().scale() > 2
                || precio.compareTo(MAXIMO) > 0) {
            throw new ExcepcionServicio(CodigoError.TEL_003);
        }
        return precio.setScale(2);
    }

    public int validarCantidad(Integer cantidad) throws ExcepcionServicio {
        if (cantidad == null || cantidad <= 0 || cantidad > CANTIDAD_MAXIMA) {
            throw new ExcepcionServicio(CodigoError.DATA_001,
                    "La cantidad debe estar entre 1 y " + CANTIDAD_MAXIMA);
        }
        return cantidad;
    }

    public FormaPago validarFormaPago(String formaPago) throws ExcepcionServicio {
        try {
            return FormaPago.valueOf(formaPago == null ? "" : formaPago.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ExcepcionServicio(CodigoError.DATA_001, "La forma de pago debe ser EFECTIVO o CREDITO");
        }
    }

    public int validarCuotas(Integer cuotas) throws ExcepcionServicio {
        if (cuotas == null || cuotas < CUOTAS_MINIMO || cuotas > CUOTAS_MAXIMO) {
            throw new ExcepcionServicio(CodigoError.FAC_002);
        }
        return cuotas;
    }
}
