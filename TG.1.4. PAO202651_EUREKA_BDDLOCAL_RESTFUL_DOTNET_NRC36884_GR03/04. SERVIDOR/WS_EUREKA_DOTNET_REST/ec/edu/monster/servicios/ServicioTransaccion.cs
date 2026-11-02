using System;
using System.Data;
using Microsoft.Data.SqlClient;
using System.Diagnostics;
using ec.edu.monster.datos;
using ec.edu.monster.modelo;
using ec.edu.monster.seguridad;

namespace ec.edu.monster.servicios
{
    /// <summary>Resultado de una transacción; NumeroMovimientoDestino solo aplica a las transferencias.</summary>
    public class ResultadoTransaccion
    {
        public decimal NuevoSaldo { get; set; }
        public int NumeroMovimiento { get; set; }
        public int? NumeroMovimientoDestino { get; set; }
    }

    /// <summary>
    /// Depósitos (RQF-005), retiros (RQF-006) y transferencias (RQF-007). Cada operación es una
    /// transacción: bloquea las cuentas, actualiza saldo y contador, registra los movimientos y
    /// confirma; ante cualquier error revierte todo (RN-07, RNF-008).
    /// </summary>
    public class ServicioTransaccion
    {
        private readonly ServicioValidacion validacion = new ServicioValidacion();
        private readonly CuentaDao cuentaDao = new CuentaDao();
        private readonly MovimientoDao movimientoDao = new MovimientoDao();

        public ResultadoTransaccion Depositar(Sesion sesion, string cuenta, decimal? importe)
        {
            string codigo = validacion.ValidarCuenta(cuenta);
            decimal monto = validacion.ValidarImporte(importe);
            return EnTransaccion((cn, tx) =>
            {
                Cuenta actual = BloquearActiva(cn, tx, codigo);
                decimal nuevoSaldo = actual.Saldo + monto;
                validacion.ValidarMaximo(nuevoSaldo);
                int numero = Registrar(cn, tx, actual, nuevoSaldo, sesion, TipoMovimiento.Deposito, monto, null);
                return new ResultadoTransaccion { NuevoSaldo = nuevoSaldo, NumeroMovimiento = numero };
            });
        }

        public ResultadoTransaccion Retirar(Sesion sesion, string cuenta, decimal? importe)
        {
            string codigo = validacion.ValidarCuenta(cuenta);
            decimal monto = validacion.ValidarImporte(importe);
            return EnTransaccion((cn, tx) =>
            {
                Cuenta actual = BloquearActiva(cn, tx, codigo);
                decimal nuevoSaldo = Restar(actual.Saldo, monto);
                int numero = Registrar(cn, tx, actual, nuevoSaldo, sesion, TipoMovimiento.Retiro, monto, null);
                return new ResultadoTransaccion { NuevoSaldo = nuevoSaldo, NumeroMovimiento = numero };
            });
        }

        /// <summary>
        /// Las dos cuentas se bloquean siempre en orden de código: así dos transferencias cruzadas
        /// (A→B y B→A) no se bloquean mutuamente.
        /// </summary>
        public ResultadoTransaccion Transferir(Sesion sesion, string cuentaOrigen, string cuentaDestino, decimal? importe)
        {
            string origen = validacion.ValidarCuenta(cuentaOrigen);
            string destino = validacion.ValidarCuenta(cuentaDestino);
            if (origen == destino)
            {
                throw new ExcepcionServicio(CodigoError.Mov003);
            }
            decimal monto = validacion.ValidarImporte(importe);
            return EnTransaccion((cn, tx) =>
            {
                bool origenPrimero = string.CompareOrdinal(origen, destino) < 0;
                Cuenta primera = Bloquear(cn, tx, origenPrimero ? origen : destino);
                Cuenta segunda = Bloquear(cn, tx, origenPrimero ? destino : origen);
                Cuenta actualOrigen = origenPrimero ? primera : segunda;
                Cuenta actualDestino = origenPrimero ? segunda : primera;

                ExigirActiva(actualOrigen);
                ExigirActiva(actualDestino);
                if (actualOrigen.CodigoMoneda != actualDestino.CodigoMoneda)
                {
                    throw new ExcepcionServicio(CodigoError.Mov004);
                }
                decimal saldoOrigen = Restar(actualOrigen.Saldo, monto);
                decimal saldoDestino = actualDestino.Saldo + monto;
                validacion.ValidarMaximo(saldoDestino);

                int numeroOrigen = Registrar(cn, tx, actualOrigen, saldoOrigen, sesion,
                    TipoMovimiento.TransferenciaSalida, monto, destino);
                int numeroDestino = Registrar(cn, tx, actualDestino, saldoDestino, sesion,
                    TipoMovimiento.TransferenciaIngreso, monto, origen);
                return new ResultadoTransaccion
                {
                    NuevoSaldo = saldoOrigen,
                    NumeroMovimiento = numeroOrigen,
                    NumeroMovimientoDestino = numeroDestino
                };
            });
        }

        private static T EnTransaccion<T>(Func<SqlConnection, SqlTransaction, T> operacion)
        {
            using (SqlConnection cn = ConexionBD.Instancia.ObtenerConexion())
            {
                SqlTransaction tx = null;
                try
                {
                    tx = cn.BeginTransaction(IsolationLevel.ReadCommitted);
                    T resultado = operacion(cn, tx);
                    tx.Commit();
                    return resultado;
                }
                catch (ExcepcionServicio)
                {
                    Revertir(tx);
                    throw;
                }
                catch (Exception e)
                {
                    Revertir(tx);
                    Trace.TraceError("Transacción revertida por un error del sistema: {0}", e);
                    throw new ExcepcionServicio(CodigoError.Sys001);
                }
            }
        }

        private static void Revertir(SqlTransaction tx)
        {
            try
            {
                tx?.Rollback();
            }
            catch (Exception e) when (e is SqlException || e is InvalidOperationException)
            {
                Trace.TraceError("No se pudo revertir la transacción: {0}", e.Message);
            }
        }

        private Cuenta Bloquear(SqlConnection cn, SqlTransaction tx, string codigo)
        {
            return cuentaDao.Bloquear(cn, tx, codigo) ?? throw new ExcepcionServicio(CodigoError.Cta001);
        }

        private Cuenta BloquearActiva(SqlConnection cn, SqlTransaction tx, string codigo)
        {
            Cuenta cuenta = Bloquear(cn, tx, codigo);
            ExigirActiva(cuenta);
            return cuenta;
        }

        private static void ExigirActiva(Cuenta cuenta)
        {
            if (!Estado.EsActivo(cuenta.Estado))
            {
                throw new ExcepcionServicio(CodigoError.Cta002);
            }
        }

        private static decimal Restar(decimal saldo, decimal importe)
        {
            if (saldo < importe)
            {
                throw new ExcepcionServicio(CodigoError.Mov002);
            }
            return saldo - importe;
        }

        /// <summary>Actualiza saldo y contador e inserta el movimiento con el número siguiente. Devuelve ese número.</summary>
        private int Registrar(SqlConnection cn, SqlTransaction tx, Cuenta cuenta, decimal nuevoSaldo, Sesion sesion,
            TipoMovimiento tipo, decimal importe, string referencia)
        {
            int numero = cuenta.Contador + 1;
            cuentaDao.Actualizar(cn, tx, cuenta.Codigo, nuevoSaldo, numero);
            movimientoDao.Insertar(cn, tx, cuenta.Codigo, numero, sesion.CodigoEmpleado, tipo, importe, referencia);
            return numero;
        }
    }
}
