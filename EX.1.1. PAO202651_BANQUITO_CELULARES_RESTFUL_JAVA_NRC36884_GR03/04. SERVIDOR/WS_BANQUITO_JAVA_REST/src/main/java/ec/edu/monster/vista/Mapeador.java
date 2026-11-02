package ec.edu.monster.vista;

import ec.edu.monster.modelo.Cliente;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.Cuota;
import ec.edu.monster.modelo.EvaluacionCredito;
import ec.edu.monster.modelo.MontoMaximo;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Convierte objetos del modelo en los DTO que devuelven los recursos.
 *
 * @author carlo
 */
@ApplicationScoped
public class Mapeador {

    public ClienteDto deCliente(Cliente cliente) {
        ClienteDto dto = new ClienteDto();
        dto.codCliente = cliente.codigo();
        dto.cedula = cliente.cedula();
        dto.nombre = cliente.nombre();
        return dto;
    }

    public EvaluacionCreditoDto deEvaluacion(EvaluacionCredito evaluacion) {
        EvaluacionCreditoDto dto = new EvaluacionCreditoDto();
        dto.cedula = evaluacion.cedula();
        if (evaluacion.cliente() != null) {
            dto.codCliente = evaluacion.cliente().codigo();
            dto.nombre = evaluacion.cliente().nombre();
        }
        dto.sujetoCredito = evaluacion.sujetoCredito();
        dto.motivos = evaluacion.motivos().stream().map(m -> {
            EvaluacionCreditoDto.MotivoDto motivo = new EvaluacionCreditoDto.MotivoDto();
            motivo.codigo = m.name();
            motivo.descripcion = m.getDescripcion();
            return motivo;
        }).toList();
        return dto;
    }

    public MontoMaximoDto deMontoMaximo(MontoMaximo monto) {
        MontoMaximoDto dto = new MontoMaximoDto();
        dto.codCliente = monto.codCliente();
        dto.promedioDepositos = monto.promedioDepositos();
        dto.promedioRetiros = monto.promedioRetiros();
        dto.porcentaje = monto.porcentaje();
        dto.plazoMeses = monto.plazoMeses();
        dto.montoMaximo = monto.montoMaximo();
        return dto;
    }

    /**
     * Con conCuotas en false la tabla no se incluye (lista de créditos).
     */
    public CreditoDto deCredito(Credito credito, boolean conCuotas) {
        CreditoDto dto = new CreditoDto();
        dto.codCredito = credito.codigo();
        dto.codCliente = credito.codCliente();
        dto.fecha = credito.fecha();
        dto.monto = credito.monto();
        dto.numCuotas = credito.numCuotas();
        dto.tasaAnual = credito.tasaAnual();
        dto.valorCuota = credito.valorCuota();
        dto.estado = credito.estado();
        dto.cuotas = conCuotas ? deCuotas(credito.cuotas()) : null;
        return dto;
    }

    public List<CreditoDto> deCreditos(List<Credito> creditos) {
        return creditos.stream().map(c -> deCredito(c, false)).toList();
    }

    public List<CuotaDto> deCuotas(List<Cuota> cuotas) {
        return cuotas.stream().map(c -> {
            CuotaDto dto = new CuotaDto();
            dto.numero = c.numero();
            dto.valorCuota = c.valorCuota();
            dto.interes = c.interes();
            dto.capital = c.capital();
            dto.saldo = c.saldo();
            return dto;
        }).toList();
    }
}
