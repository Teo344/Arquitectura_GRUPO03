using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using ec.edu.monster.modelo;

namespace ec.edu.monster.datos
{
    /// <summary>
    /// Acceso a la tabla movimiento: historial (RQF-004) y registro de cada transacción
    /// (RQF-005 a RQF-007).
    /// </summary>
    public class MovimientoDao
    {
        private const string SqlListar = @"
            SELECT mv.int_movinumero, mv.dtt_movifecha, t.vch_tipodescripcion, t.vch_tipoaccion,
                   mv.dec_moviimporte, mv.chr_cuenreferencia
            FROM movimiento mv
            JOIN tipomovimiento t ON t.chr_tipocodigo = mv.chr_tipocodigo
            WHERE mv.chr_cuencodigo = @cuenta
            ORDER BY mv.int_movinumero";

        private const string SqlInsertar = @"
            INSERT INTO movimiento (chr_cuencodigo, int_movinumero, dtt_movifecha, chr_emplcodigo,
                                    chr_tipocodigo, dec_moviimporte, chr_cuenreferencia)
            VALUES (@cuenta, @numero, CAST(GETDATE() AS DATE), @empleado, @tipo, @importe, @referencia)";

        public List<Movimiento> Listar(SqlConnection cn, string cuenta)
        {
            List<Movimiento> movimientos = new List<Movimiento>();
            using (SqlCommand comando = new SqlCommand(SqlListar, cn))
            {
                UtilSql.Texto(comando, "@cuenta", SqlDbType.Char, 8, cuenta);
                using (SqlDataReader lector = comando.ExecuteReader())
                {
                    while (lector.Read())
                    {
                        movimientos.Add(new Movimiento
                        {
                            Numero = lector.GetInt32(0),
                            Fecha = lector.GetDateTime(1),
                            Tipo = lector.GetString(2),
                            Accion = lector.GetString(3),
                            Importe = lector.GetDecimal(4),
                            CuentaReferencia = UtilSql.TextoONull(lector, 5)
                        });
                    }
                }
            }
            return movimientos;
        }

        /// <summary>
        /// El empleado es siempre el de la sesión (RN-06); referencia es null salvo en las
        /// transferencias.
        /// </summary>
        public void Insertar(SqlConnection cn, SqlTransaction tx, string cuenta, int numero, string empleado,
            TipoMovimiento tipo, decimal importe, string referencia)
        {
            using (SqlCommand comando = new SqlCommand(SqlInsertar, cn, tx))
            {
                UtilSql.Texto(comando, "@cuenta", SqlDbType.Char, 8, cuenta);
                UtilSql.Entero(comando, "@numero", numero);
                UtilSql.Texto(comando, "@empleado", SqlDbType.Char, 4, empleado);
                UtilSql.Texto(comando, "@tipo", SqlDbType.Char, 3, tipo.Codigo);
                UtilSql.Importe(comando, "@importe", importe);
                UtilSql.Texto(comando, "@referencia", SqlDbType.Char, 8, referencia);
                comando.ExecuteNonQuery();
            }
        }
    }
}
