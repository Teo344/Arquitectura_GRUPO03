using System.Data.Common;
using ec.edu.monster.modelo.excepcion;
using Microsoft.Data.SqlClient;
using Microsoft.EntityFrameworkCore;

namespace ec.edu.monster.modelo.dao
{
    /// <summary>
    /// Traduce las excepciones de SQL Server y de Entity Framework a las excepciones de la aplicación,
    /// con un mensaje listo para el usuario. El detalle técnico queda en el log.
    /// </summary>
    internal static class ErroresBD
    {
        /// <summary>Violación de índice único (2601) o de restricción UNIQUE (2627).</summary>
        private static readonly int[] Duplicado = { 2601, 2627 };

        /// <summary>Errores de red, servidor apagado, base inexistente o credenciales.</summary>
        private static readonly int[] SinConexion = { -2, -1, 2, 53, 233, 258, 4060, 10054, 10060, 10061, 18456 };

        public static async Task<T> EjecutarAsync<T>(ILogger log, string operacion, Func<Task<T>> accion,
            Func<ExcepcionAplicacion>? siDuplicado = null)
        {
            try
            {
                return await accion();
            }
            catch (ExcepcionAplicacion)
            {
                throw;
            }
            catch (Exception e) when (e is DbException || e is DbUpdateException || e is InvalidOperationException)
            {
                SqlException? sql = BuscarSql(e);
                if (sql != null && Duplicado.Contains(sql.Number) && siDuplicado != null)
                {
                    throw siDuplicado();
                }
                log.LogError(e, "Error de base de datos al {Operacion}", operacion);
                if (sql != null && SinConexion.Contains(sql.Number))
                {
                    throw new ExcepcionPersistencia(
                        "No se pudo conectar a la base de datos. Verifique que SQL Server esté en ejecución "
                        + "y que la base ApiPaisesDB exista (Update-Database).", e);
                }
                throw new ExcepcionPersistencia("No se pudo " + operacion + ". Intente nuevamente.", e);
            }
        }

        private static SqlException? BuscarSql(Exception? e)
        {
            while (e != null)
            {
                if (e is SqlException sql)
                {
                    return sql;
                }
                e = e.InnerException;
            }
            return null;
        }
    }
}
