using System;
using System.Data;
using System.Data.SqlClient;

namespace ec.edu.monster.datos
{
    /// <summary>Registro de ingreso (RQF-001) y salida (RQF-002) de cada sesión en la tabla logsession.</summary>
    public class LogSesionDao
    {
        private const string SqlIngreso = @"
            INSERT INTO logsession (chr_emplcodigo, dtt_logingreso, vch_logip, vch_loghostname)
            OUTPUT INSERTED.int_logid
            VALUES (@empleado, GETDATE(), @ip, @equipo)";

        private const string SqlSalida = @"
            UPDATE logsession SET dtt_logsalida = @salida
            WHERE int_logid = @id AND dtt_logsalida IS NULL";

        /// <summary>Devuelve el int_logid generado, que la sesión guarda para el cierre.</summary>
        public int RegistrarIngreso(SqlConnection cn, string codigoEmpleado, string ip, string equipo)
        {
            using (SqlCommand comando = new SqlCommand(SqlIngreso, cn))
            {
                UtilSql.Texto(comando, "@empleado", SqlDbType.Char, 4, codigoEmpleado);
                UtilSql.Texto(comando, "@ip", SqlDbType.VarChar, 45, Recortar(ip, 45));
                UtilSql.Texto(comando, "@equipo", SqlDbType.VarChar, 100, Recortar(equipo, 100));
                return (int)comando.ExecuteScalar();
            }
        }

        /// <summary>salida en hora UTC; se guarda en la hora local del servidor, como GETDATE().</summary>
        public void RegistrarSalida(SqlConnection cn, int logId, DateTime salida)
        {
            using (SqlCommand comando = new SqlCommand(SqlSalida, cn))
            {
                comando.Parameters.Add("@salida", SqlDbType.DateTime).Value = salida.ToLocalTime();
                UtilSql.Entero(comando, "@id", logId);
                comando.ExecuteNonQuery();
            }
        }

        private static string Recortar(string texto, int maximo)
        {
            if (string.IsNullOrWhiteSpace(texto))
            {
                return "NONE";
            }
            string limpio = texto.Trim();
            return limpio.Length <= maximo ? limpio : limpio.Substring(0, maximo);
        }
    }
}
