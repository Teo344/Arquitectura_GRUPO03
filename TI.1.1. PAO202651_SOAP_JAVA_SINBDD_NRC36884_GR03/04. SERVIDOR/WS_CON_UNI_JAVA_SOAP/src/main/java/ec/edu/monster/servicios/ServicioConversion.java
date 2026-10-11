package ec.edu.monster.servicios;

import ec.edu.monster.modelo.Categoria;
import ec.edu.monster.modelo.CodigoError;
import ec.edu.monster.modelo.Unidad;
import ec.edu.monster.servicios.conversores.Conversor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Orquesta validación, elección del conversor y redondeo (RQF-004 a RQF-006).
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioConversion {

    /**
     * Resultado de una conversión, ya redondeado.
     */
    public record ResultadoConversion(double resultado, Unidad origen, Unidad destino) {
    }

    private static final int DECIMALES = 4;

    @Inject
    private ServicioValidacion validacion;

    @Inject
    private Instance<Conversor> conversores;

    public ResultadoConversion convertir(Categoria categoria, Double valor, String origen, String destino)
            throws ExcepcionServicio {
        Unidad[] unidades = validacion.validarConversion(categoria, valor, origen, destino);
        Conversor conversor = conversores.stream()
                .filter(c -> c.soporta(categoria))
                .findFirst()
                .orElseThrow(() -> new ExcepcionServicio(CodigoError.DATA_001));
        double resultado = conversor.convertir(valor, unidades[0], unidades[1]);
        return new ResultadoConversion(redondear(resultado), unidades[0], unidades[1]);
    }

    private static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(DECIMALES, RoundingMode.HALF_UP).doubleValue();
    }
}
