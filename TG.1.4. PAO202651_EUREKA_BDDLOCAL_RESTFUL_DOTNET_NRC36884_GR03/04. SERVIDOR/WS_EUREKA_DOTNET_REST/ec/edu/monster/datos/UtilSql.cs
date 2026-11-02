using System;
using System.Data;
using Microsoft.Data.SqlClient;

namespace ec.edu.monster.datos
{
    /// <summary>
    /// Parámetros con el tipo exacto de la columna. Con AddWithValue un texto se enviaría como
    /// NVARCHAR y SQL Server convertiría la columna CHAR/VARCHAR, recorriendo la tabla en vez de
    /// usar la clave primaria (y bloqueando más filas de las necesarias).
    /// </summary>
    internal static class UtilSql
    {
        public static void Texto(SqlCommand comando, string nombre, SqlDbType tipo, int tamano, string valor)
        {
            comando.Parameters.Add(nombre, tipo, tamano).Value = (object)valor ?? DBNull.Value;
        }

        public static void Entero(SqlCommand comando, string nombre, int valor)
        {
            comando.Parameters.Add(nombre, SqlDbType.Int).Value = valor;
        }

        /// <summary>Importes y saldos: DECIMAL(12,2).</summary>
        public static void Importe(SqlCommand comando, string nombre, decimal valor)
        {
            SqlParameter parametro = comando.Parameters.Add(nombre, SqlDbType.Decimal);
            parametro.Precision = 12;
            parametro.Scale = 2;
            parametro.Value = valor;
        }

        public static string TextoONull(SqlDataReader lector, int columna)
        {
            return lector.IsDBNull(columna) ? null : lector.GetString(columna);
        }
    }
}
