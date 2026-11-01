package ec.edu.monster.vista;

import ec.edu.monster.modelo.Cuenta;
import ec.edu.monster.modelo.Movimiento;
import ec.edu.monster.servicios.ServicioAutenticacion.SesionIniciada;
import ec.edu.monster.servicios.ServicioTransaccion.ResultadoTransaccion;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Convierte objetos del modelo en {@link RespuestaDto} (sección 8.2.7 del
 * ERS).
 *
 * @author carlo
 */
@ApplicationScoped
public class RespuestaMapeador {

    public static final String ESTADO_OK = "OK";
    public static final String CODIGO_OK = "OK-000";
    public static final String MENSAJE_INICIO_SESION = "Sesión iniciada";
    public static final String MENSAJE_CIERRE_SESION = "Sesión cerrada";
    public static final String MENSAJE_CUENTA = "Cuenta consultada";
    public static final String MENSAJE_MOVIMIENTOS = "Movimientos consultados";
    public static final String MENSAJE_SIN_MOVIMIENTOS = "La cuenta no registra movimientos";
    public static final String MENSAJE_DEPOSITO = "Depósito registrado";
    public static final String MENSAJE_RETIRO = "Retiro registrado";
    public static final String MENSAJE_TRANSFERENCIA = "Transferencia registrada";

    public RespuestaDto deSesion(SesionIniciada iniciada) {
        RespuestaDto respuesta = exito(MENSAJE_INICIO_SESION);
        respuesta.token = iniciada.token().token();
        respuesta.expiraEn = iniciada.token().expiraEn().toString();
        respuesta.codigoEmpleado = iniciada.sesion().codigoEmpleado();
        respuesta.nombreEmpleado = iniciada.sesion().nombre();
        respuesta.codigoSucursal = iniciada.sesion().codigoSucursal();
        respuesta.sucursal = iniciada.sesion().sucursal();
        return respuesta;
    }

    public RespuestaDto deCierreSesion() {
        return exito(MENSAJE_CIERRE_SESION);
    }

    public RespuestaDto deCuenta(Cuenta cuenta) {
        RespuestaDto respuesta = exito(MENSAJE_CUENTA);
        respuesta.cuenta = new CuentaDto(cuenta.codigo(), cuenta.titular(), cuenta.moneda(), cuenta.saldo(),
                cuenta.estado(), cuenta.sucursal(), cuenta.fechaCreacion().toString());
        return respuesta;
    }

    public RespuestaDto deMovimientos(List<Movimiento> movimientos) {
        RespuestaDto respuesta = exito(movimientos.isEmpty() ? MENSAJE_SIN_MOVIMIENTOS : MENSAJE_MOVIMIENTOS);
        respuesta.movimientos = movimientos.stream()
                .map(m -> new MovimientoDto(m.numero(), m.fecha().toString(), m.tipo(), m.accion(),
                        m.importe(), m.cuentaReferencia()))
                .toList();
        return respuesta;
    }

    public RespuestaDto deDeposito(ResultadoTransaccion resultado) {
        return deTransaccion(MENSAJE_DEPOSITO, resultado);
    }

    public RespuestaDto deRetiro(ResultadoTransaccion resultado) {
        return deTransaccion(MENSAJE_RETIRO, resultado);
    }

    public RespuestaDto deTransferencia(ResultadoTransaccion resultado) {
        return deTransaccion(MENSAJE_TRANSFERENCIA, resultado);
    }

    private RespuestaDto deTransaccion(String mensaje, ResultadoTransaccion resultado) {
        RespuestaDto respuesta = exito(mensaje);
        respuesta.nuevoSaldo = resultado.nuevoSaldo();
        respuesta.numeroMovimiento = resultado.numeroMovimiento();
        respuesta.numeroMovimientoDestino = resultado.numeroMovimientoDestino();
        return respuesta;
    }

    private RespuestaDto exito(String mensaje) {
        RespuestaDto respuesta = new RespuestaDto();
        respuesta.estado = ESTADO_OK;
        respuesta.codigo = CODIGO_OK;
        respuesta.mensaje = mensaje;
        return respuesta;
    }
}
