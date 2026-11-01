using System.Data;
using System.Data.SqlClient;
using ec.edu.monster.modelo;

namespace ec.edu.monster.datos
{
    /// <summary>
    /// Acceso a la tabla cuenta: consulta (RQF-003) y bloqueo y actualización de saldo dentro de
    /// una transacción (RN-07).
    /// </summary>
    public class CuentaDao
    {
        private const string SqlBuscar = @"
            SELECT c.chr_cuencodigo,
                   CONCAT(cl.vch_clienombre, ' ', cl.vch_cliepaterno, ' ', cl.vch_cliematerno),
                   c.chr_monecodigo, m.vch_monedescripcion, c.dec_cuensaldo, c.vch_cuenestado,
                   s.vch_sucunombre, c.dtt_cuenfechacreacion, c.int_cuencontmov
            FROM cuenta c
            JOIN cliente  cl ON cl.chr_cliecodigo = c.chr_cliecodigo
            JOIN moneda   m  ON m.chr_monecodigo  = c.chr_monecodigo
            JOIN sucursal s  ON s.chr_sucucodigo  = c.chr_sucucodigo
            WHERE c.chr_cuencodigo = @codigo";

        /// <summary>UPDLOCK + ROWLOCK es el equivalente de SELECT … FOR UPDATE de MySQL.</summary>
        private const string SqlBloquear = @"
            SELECT chr_cuencodigo, chr_monecodigo, dec_cuensaldo, vch_cuenestado, int_cuencontmov
            FROM cuenta WITH (UPDLOCK, ROWLOCK)
            WHERE chr_cuencodigo = @codigo";

        private const string SqlActualizar = @"
            UPDATE cuenta SET dec_cuensaldo = @saldo, int_cuencontmov = @contador
            WHERE chr_cuencodigo = @codigo";

        /// <summary>Devuelve la cuenta o null si no existe.</summary>
        public Cuenta Buscar(SqlConnection cn, string codigo)
        {
            using (SqlCommand comando = new SqlCommand(SqlBuscar, cn))
            {
                UtilSql.Texto(comando, "@codigo", SqlDbType.Char, 8, codigo);
                using (SqlDataReader lector = comando.ExecuteReader())
                {
                    if (!lector.Read())
                    {
                        return null;
                    }
                    return new Cuenta
                    {
                        Codigo = lector.GetString(0),
                        Titular = lector.GetString(1),
                        CodigoMoneda = lector.GetString(2),
                        Moneda = lector.GetString(3),
                        Saldo = lector.GetDecimal(4),
                        Estado = lector.GetString(5),
                        Sucursal = lector.GetString(6),
                        FechaCreacion = lector.GetDateTime(7),
                        Contador = lector.GetInt32(8)
                    };
                }
            }
        }

        /// <summary>
        /// La fila queda bloqueada hasta el commit o rollback de la transacción. Devuelve null si
        /// la cuenta no existe.
        /// </summary>
        public Cuenta Bloquear(SqlConnection cn, SqlTransaction tx, string codigo)
        {
            using (SqlCommand comando = new SqlCommand(SqlBloquear, cn, tx))
            {
                UtilSql.Texto(comando, "@codigo", SqlDbType.Char, 8, codigo);
                using (SqlDataReader lector = comando.ExecuteReader())
                {
                    if (!lector.Read())
                    {
                        return null;
                    }
                    return new Cuenta
                    {
                        Codigo = lector.GetString(0),
                        CodigoMoneda = lector.GetString(1),
                        Saldo = lector.GetDecimal(2),
                        Estado = lector.GetString(3),
                        Contador = lector.GetInt32(4)
                    };
                }
            }
        }

        public void Actualizar(SqlConnection cn, SqlTransaction tx, string codigo, decimal saldo, int contador)
        {
            using (SqlCommand comando = new SqlCommand(SqlActualizar, cn, tx))
            {
                UtilSql.Importe(comando, "@saldo", saldo);
                UtilSql.Entero(comando, "@contador", contador);
                UtilSql.Texto(comando, "@codigo", SqlDbType.Char, 8, codigo);
                comando.ExecuteNonQuery();
            }
        }
    }
}
