package ec.edu.monster.servicios;

import ec.edu.monster.datos.PasarelaBanquito;
import ec.edu.monster.modelo.ClienteBanco;
import ec.edu.monster.modelo.CreditoBanco;
import ec.edu.monster.modelo.CuotaBanco;
import ec.edu.monster.modelo.EvaluacionBanco;
import ec.edu.monster.modelo.EvaluacionCliente;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

/**
 * Consultas al banco que necesitan los clientes de la comercializadora antes
 * y después de vender: evaluación del comprador, sus créditos, la tabla de
 * amortización y la simulación. Todo pasa por {@link PasarelaBanquito}.
 *
 * @author carlo
 */
@ApplicationScoped
public class ServicioCliente {

    @Inject
    private ServicioValidacion validacion;

    @Inject
    private PasarelaBanquito pasarela;

    /**
     * Servicios 1 y 2 del Core en una sola respuesta. El monto máximo solo se
     * consulta si la cédula es de un cliente.
     */
    public EvaluacionCliente evaluar(String cedula) throws ExcepcionServicio {
        EvaluacionBanco evaluacion = pasarela.evaluar(validacion.validarCedula(cedula));
        BigDecimal maximo = evaluacion.esCliente() ? pasarela.montoMaximo(evaluacion.codCliente).montoMaximo : null;
        return new EvaluacionCliente(evaluacion, maximo);
    }

    public List<CreditoBanco> creditos(String cedula) throws ExcepcionServicio {
        ClienteBanco cliente = pasarela.buscarCliente(validacion.validarCedula(cedula));
        return pasarela.creditos(cliente.codCliente);
    }

    /**
     * Servicio 4 del Core.
     */
    public List<CuotaBanco> cuotas(Integer codCredito) throws ExcepcionServicio {
        return pasarela.cuotas(validacion.validarCodigo(codCredito));
    }

    /**
     * El Core valida el monto y las cuotas (CRE-005, CRE-003).
     */
    public List<CuotaBanco> simular(String monto, String numCuotas) throws ExcepcionServicio {
        return pasarela.simular(monto, numCuotas);
    }
}
