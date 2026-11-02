package ec.edu.monster.vista;

import ec.edu.monster.modelo.CreditoBanco;
import ec.edu.monster.modelo.CuotaBanco;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.EvaluacionBanco;
import ec.edu.monster.modelo.EvaluacionCliente;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.Telefono;
import ec.edu.monster.servicios.CalculadoraFactura;
import ec.edu.monster.servicios.ServicioAutenticacion.SesionIniciada;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Convierte objetos del modelo (y los que devuelve el Core) en los DTO que
 * devuelven los recursos.
 *
 * @author carlo
 */
@ApplicationScoped
public class Mapeador {

    @Inject
    @ConfigProperty(name = "factura.descuentoEfectivo")
    private BigDecimal porcentajeDescuento;

    public SesionDto deSesion(SesionIniciada iniciada) {
        SesionDto dto = new SesionDto();
        dto.token = iniciada.token().token();
        dto.expiraEn = iniciada.token().expiraEn().toString();
        dto.codUsuario = iniciada.sesion().codUsuario();
        dto.usuario = iniciada.sesion().usuario();
        dto.nombre = iniciada.sesion().nombre();
        return dto;
    }

    public TelefonoDto deTelefono(Telefono telefono) {
        TelefonoDto dto = new TelefonoDto();
        dto.codTelefono = telefono.codigo();
        dto.marca = telefono.marca();
        dto.modelo = telefono.modelo();
        dto.precio = telefono.precio();
        dto.precioEfectivo = CalculadoraFactura.precioEfectivo(telefono.precio(), porcentajeDescuento);
        dto.estado = telefono.estado();
        return dto;
    }

    public List<TelefonoDto> deTelefonos(List<Telefono> telefonos) {
        return telefonos.stream().map(this::deTelefono).toList();
    }

    public EvaluacionClienteDto deEvaluacion(EvaluacionCliente evaluacionCliente) {
        EvaluacionBanco evaluacion = evaluacionCliente.evaluacion();
        EvaluacionClienteDto dto = new EvaluacionClienteDto();
        dto.cedula = evaluacion.cedula;
        dto.codCliente = evaluacion.codCliente;
        dto.nombre = evaluacion.nombre;
        dto.sujetoCredito = evaluacion.sujetoCredito;
        dto.motivos = evaluacion.motivos == null ? List.of() : evaluacion.motivos.stream().map(m -> {
            EvaluacionClienteDto.MotivoDto motivo = new EvaluacionClienteDto.MotivoDto();
            motivo.codigo = m.codigo;
            motivo.descripcion = m.descripcion;
            return motivo;
        }).toList();
        dto.montoMaximo = evaluacionCliente.montoMaximo();
        return dto;
    }

    public List<CreditoDto> deCreditos(List<CreditoBanco> creditos) {
        return creditos.stream().map(c -> {
            CreditoDto dto = new CreditoDto();
            dto.codCredito = c.codCredito;
            dto.codCliente = c.codCliente;
            dto.fecha = c.fecha;
            dto.monto = c.monto;
            dto.numCuotas = c.numCuotas;
            dto.tasaAnual = c.tasaAnual;
            dto.valorCuota = c.valorCuota;
            dto.estado = c.estado;
            return dto;
        }).toList();
    }

    public List<CuotaDto> deCuotas(List<CuotaBanco> cuotas) {
        return cuotas.stream().map(c -> {
            CuotaDto dto = new CuotaDto();
            dto.numero = c.numero;
            dto.valorCuota = c.valorCuota;
            dto.interes = c.interes;
            dto.capital = c.capital;
            dto.saldo = c.saldo;
            return dto;
        }).toList();
    }

    /**
     * Con detalle en false las líneas no se incluyen (lista de facturas).
     */
    public FacturaDto deFactura(Factura factura, boolean conDetalle) {
        FacturaDto dto = new FacturaDto();
        dto.numFactura = factura.numero();
        dto.fecha = factura.fecha();
        dto.usuario = factura.usuario();
        dto.cedula = factura.cedula();
        dto.codClienteBanco = factura.codClienteBanco();
        dto.nombreCliente = factura.nombreCliente();
        dto.formaPago = factura.formaPago().name();
        dto.subtotal = factura.subtotal();
        dto.descuento = factura.descuento();
        dto.total = factura.total();
        dto.codCreditoBanco = factura.codCreditoBanco();
        dto.numCuotas = factura.numCuotas();
        dto.detalles = conDetalle ? factura.detalles().stream().map(Mapeador::deDetalle).toList() : null;
        return dto;
    }

    public List<FacturaDto> deFacturas(List<Factura> facturas) {
        return facturas.stream().map(f -> deFactura(f, false)).toList();
    }

    private static FacturaDto.DetalleDto deDetalle(DetalleFactura detalle) {
        FacturaDto.DetalleDto dto = new FacturaDto.DetalleDto();
        dto.codTelefono = detalle.codTelefono();
        dto.marca = detalle.marca();
        dto.modelo = detalle.modelo();
        dto.cantidad = detalle.cantidad();
        dto.precioUnitario = detalle.precioUnitario();
        dto.subtotal = detalle.subtotal();
        return dto;
    }
}
