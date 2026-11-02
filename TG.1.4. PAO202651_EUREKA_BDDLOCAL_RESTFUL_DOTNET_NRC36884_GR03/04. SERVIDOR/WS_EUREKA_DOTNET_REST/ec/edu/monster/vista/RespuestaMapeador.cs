using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using ec.edu.monster.modelo;
using ec.edu.monster.servicios;

namespace ec.edu.monster.vista
{
    /// <summary>Convierte objetos del modelo en <see cref="RespuestaDto"/> (sección 8.2.7 del ERS).</summary>
    public static class RespuestaMapeador
    {
        public const string EstadoOk = "OK";
        public const string CodigoOk = "OK-000";
        private const string FormatoFecha = "yyyy-MM-dd";

        public static RespuestaDto DeSesion(SesionIniciada iniciada)
        {
            RespuestaDto respuesta = Exito("Sesión iniciada");
            respuesta.Token = iniciada.Token.Token;
            respuesta.ExpiraEn = iniciada.Token.ExpiraEn.ToString("o", CultureInfo.InvariantCulture);
            respuesta.CodigoEmpleado = iniciada.Sesion.CodigoEmpleado;
            respuesta.NombreEmpleado = iniciada.Sesion.Nombre;
            respuesta.CodigoSucursal = iniciada.Sesion.CodigoSucursal;
            respuesta.Sucursal = iniciada.Sesion.Sucursal;
            return respuesta;
        }

        public static RespuestaDto DeCierreSesion() => Exito("Sesión cerrada");

        public static RespuestaDto DeCuenta(Cuenta cuenta)
        {
            RespuestaDto respuesta = Exito("Cuenta consultada");
            respuesta.Cuenta = new CuentaDto
            {
                Codigo = cuenta.Codigo,
                Titular = cuenta.Titular,
                Moneda = cuenta.Moneda,
                Saldo = cuenta.Saldo,
                Estado = cuenta.Estado,
                Sucursal = cuenta.Sucursal,
                FechaCreacion = cuenta.FechaCreacion?.ToString(FormatoFecha, CultureInfo.InvariantCulture)
            };
            return respuesta;
        }

        public static RespuestaDto DeMovimientos(List<Movimiento> movimientos)
        {
            RespuestaDto respuesta = Exito(movimientos.Count == 0 ? "La cuenta no registra movimientos" : "Movimientos consultados");
            respuesta.Movimientos = movimientos
                .Select(m => new MovimientoDto
                {
                    Numero = m.Numero,
                    Fecha = m.Fecha.ToString(FormatoFecha, CultureInfo.InvariantCulture),
                    Tipo = m.Tipo,
                    Accion = m.Accion,
                    Importe = m.Importe,
                    CuentaReferencia = m.CuentaReferencia
                })
                .ToList();
            return respuesta;
        }

        public static RespuestaDto DeDeposito(ResultadoTransaccion resultado) => DeTransaccion("Depósito registrado", resultado);

        public static RespuestaDto DeRetiro(ResultadoTransaccion resultado) => DeTransaccion("Retiro registrado", resultado);

        public static RespuestaDto DeTransferencia(ResultadoTransaccion resultado) => DeTransaccion("Transferencia registrada", resultado);

        private static RespuestaDto DeTransaccion(string mensaje, ResultadoTransaccion resultado)
        {
            RespuestaDto respuesta = Exito(mensaje);
            respuesta.NuevoSaldo = resultado.NuevoSaldo;
            respuesta.NumeroMovimiento = resultado.NumeroMovimiento;
            respuesta.NumeroMovimientoDestino = resultado.NumeroMovimientoDestino;
            return respuesta;
        }

        private static RespuestaDto Exito(string mensaje)
        {
            return new RespuestaDto { Estado = EstadoOk, Codigo = CodigoOk, Mensaje = mensaje };
        }
    }
}
