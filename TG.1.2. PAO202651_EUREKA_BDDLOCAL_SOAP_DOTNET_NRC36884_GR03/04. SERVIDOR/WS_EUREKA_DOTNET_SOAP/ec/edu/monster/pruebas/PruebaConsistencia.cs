#:package Microsoft.Data.SqlClient@7.1.1
#:package System.Security.Cryptography.Pkcs@10.0.14
using System;
using System.IO;
using System.Linq;
using System.Xml.Linq;
using Microsoft.Data.SqlClient;

/// <summary>
/// Prueba de integridad (RNF-008) con la consulta 5 de 03_consultas.sql: en cada cuenta el saldo
/// es la suma de sus movimientos y el contador es el número del último movimiento. Se ejecuta al
/// final de las demás pruebas, desde esta carpeta: dotnet run PruebaConsistencia.cs
/// </summary>
public class PruebaConsistencia
{
    private const string Sql = @"
        SELECT c.chr_cuencodigo,
               CASE WHEN c.dec_cuensaldo = COALESCE(SUM(CASE t.vch_tipoaccion
                            WHEN 'INGRESO' THEN mv.dec_moviimporte
                            ELSE -mv.dec_moviimporte END), 0) THEN 1 ELSE 0 END,
               CASE WHEN c.int_cuencontmov = COALESCE(MAX(mv.int_movinumero), 0) THEN 1 ELSE 0 END
        FROM cuenta c
        LEFT JOIN movimiento mv    ON mv.chr_cuencodigo = c.chr_cuencodigo
        LEFT JOIN tipomovimiento t ON t.chr_tipocodigo  = mv.chr_tipocodigo
        GROUP BY c.chr_cuencodigo, c.dec_cuensaldo, c.int_cuencontmov
        ORDER BY c.chr_cuencodigo";

    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConsistencia ===");
        using (SqlConnection cn = new SqlConnection(CadenaConexion()))
        {
            cn.Open();
            using (SqlCommand comando = new SqlCommand(Sql, cn))
            using (SqlDataReader lector = comando.ExecuteReader())
            {
                while (lector.Read())
                {
                    Comprobar(lector.GetString(0) + ": saldo igual a la suma de sus movimientos", lector.GetInt32(1) == 1);
                    Comprobar(lector.GetString(0) + ": contador igual al último movimiento", lector.GetInt32(2) == 1);
                }
            }
        }

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    /// <summary>
    /// La misma cadena que usa el servidor: busca conexion.config subiendo desde la carpeta actual.
    /// Microsoft.Data.SqlClient cifra por defecto; SQL Server Express local usa un certificado propio.
    /// </summary>
    private static string CadenaConexion()
    {
        for (DirectoryInfo? carpeta = new DirectoryInfo(Directory.GetCurrentDirectory()); carpeta != null; carpeta = carpeta.Parent)
        {
            string archivo = Path.Combine(carpeta.FullName, "conexion.config");
            if (File.Exists(archivo))
            {
                string cadena = XDocument.Load(archivo).Descendants("add")
                    .First(e => (string?)e.Attribute("name") == "eurekabank").Attribute("connectionString")!.Value;
                return cadena + ";TrustServerCertificate=True";
            }
        }
        throw new FileNotFoundException("No se encontró conexion.config; ejecutar desde la carpeta del servidor o sus subcarpetas");
    }

    private static void Comprobar(string descripcion, bool correcto)
    {
        if (!correcto)
        {
            fallidas++;
        }
        Console.WriteLine((correcto ? "[OK]    " : "[FALLA] ") + descripcion);
    }
}
