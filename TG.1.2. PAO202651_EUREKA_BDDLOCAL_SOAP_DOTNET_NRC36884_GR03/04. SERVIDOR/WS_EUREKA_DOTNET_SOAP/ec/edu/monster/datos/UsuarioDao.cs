using System.Data;
using System.Data.SqlClient;
using ec.edu.monster.modelo;

namespace ec.edu.monster.datos
{
    /// <summary>
    /// Consulta de credenciales (RQF-001). No filtra por estado para que el servicio distinga
    /// AUTH-001 (no existe) de AUTH-002 (no activo).
    /// </summary>
    public class UsuarioDao
    {
        private const string SqlBuscar = @"
            SELECT TOP 1 u.chr_emplcodigo, u.vch_emplusuario,
                   CONCAT(e.vch_emplnombre, ' ', e.vch_emplpaterno, ' ', e.vch_emplmaterno),
                   u.vch_emplestado, s.chr_sucucodigo, s.vch_sucunombre
            FROM usuario u
            JOIN empleado e ON e.chr_emplcodigo = u.chr_emplcodigo
            LEFT JOIN asignado a ON a.chr_emplcodigo = u.chr_emplcodigo AND a.dtt_asigfechabaja IS NULL
            LEFT JOIN sucursal s ON s.chr_sucucodigo = a.chr_sucucodigo
            WHERE u.vch_emplusuario = @usuario AND u.vch_emplclave = @clave
            ORDER BY a.dtt_asigfechaalta DESC";

        /// <summary>Devuelve el empleado o null si el usuario o la clave no coinciden.</summary>
        public Empleado Buscar(SqlConnection cn, string usuario, string hashClave)
        {
            using (SqlCommand comando = new SqlCommand(SqlBuscar, cn))
            {
                UtilSql.Texto(comando, "@usuario", SqlDbType.VarChar, 20, usuario);
                UtilSql.Texto(comando, "@clave", SqlDbType.Char, 64, hashClave);
                using (SqlDataReader lector = comando.ExecuteReader())
                {
                    if (!lector.Read())
                    {
                        return null;
                    }
                    return new Empleado
                    {
                        Codigo = lector.GetString(0),
                        Usuario = lector.GetString(1),
                        Nombre = lector.GetString(2),
                        Estado = lector.GetString(3),
                        CodigoSucursal = UtilSql.TextoONull(lector, 4),
                        Sucursal = UtilSql.TextoONull(lector, 5)
                    };
                }
            }
        }
    }
}
