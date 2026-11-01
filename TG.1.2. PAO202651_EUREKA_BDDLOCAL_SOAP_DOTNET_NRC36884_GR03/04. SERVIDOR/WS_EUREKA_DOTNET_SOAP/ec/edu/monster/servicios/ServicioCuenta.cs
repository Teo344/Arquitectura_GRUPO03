using System.Collections.Generic;
using System.Data.SqlClient;
using System.Diagnostics;
using ec.edu.monster.datos;
using ec.edu.monster.modelo;

namespace ec.edu.monster.servicios
{
    /// <summary>
    /// Consultas de solo lectura: datos de la cuenta (RQF-003) y movimientos (RQF-004). Una cuenta
    /// no activa se consulta igual y muestra su estado.
    /// </summary>
    public class ServicioCuenta
    {
        private readonly ServicioValidacion validacion = new ServicioValidacion();
        private readonly CuentaDao cuentaDao = new CuentaDao();
        private readonly MovimientoDao movimientoDao = new MovimientoDao();

        public Cuenta ConsultarCuenta(string cuenta)
        {
            string codigo = validacion.ValidarCuenta(cuenta);
            try
            {
                using (SqlConnection cn = ConexionBD.Instancia.ObtenerConexion())
                {
                    return Buscar(cn, codigo);
                }
            }
            catch (SqlException e)
            {
                Trace.TraceError("Error de base de datos al consultar la cuenta: {0}", e.Message);
                throw new ExcepcionServicio(CodigoError.Sys001);
            }
        }

        /// <summary>Una cuenta sin movimientos devuelve una lista vacía, no un error.</summary>
        public List<Movimiento> ConsultarMovimientos(string cuenta)
        {
            string codigo = validacion.ValidarCuenta(cuenta);
            try
            {
                using (SqlConnection cn = ConexionBD.Instancia.ObtenerConexion())
                {
                    Buscar(cn, codigo);
                    return movimientoDao.Listar(cn, codigo);
                }
            }
            catch (SqlException e)
            {
                Trace.TraceError("Error de base de datos al consultar movimientos: {0}", e.Message);
                throw new ExcepcionServicio(CodigoError.Sys001);
            }
        }

        private Cuenta Buscar(SqlConnection cn, string codigo)
        {
            return cuentaDao.Buscar(cn, codigo) ?? throw new ExcepcionServicio(CodigoError.Cta001);
        }
    }
}
