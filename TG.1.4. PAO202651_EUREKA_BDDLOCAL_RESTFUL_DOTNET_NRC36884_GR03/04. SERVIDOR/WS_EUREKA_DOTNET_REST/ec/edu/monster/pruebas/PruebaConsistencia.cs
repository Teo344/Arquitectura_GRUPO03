#:project ../../../../WS_EUREKA_DOTNET_REST.csproj

using System;
using System.IO;
using System.Text.Json;
using Microsoft.Data.SqlClient;
using ec.edu.monster.datos;

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
        ConexionBD.Instancia.Configurar(CadenaConexion());
        using (SqlConnection cn = ConexionBD.Instancia.ObtenerConexion())
        using (SqlCommand comando = new SqlCommand(Sql, cn))
        using (SqlDataReader lector = comando.ExecuteReader())
        {
            while (lector.Read())
            {
                Comprobar(lector.GetString(0) + ": saldo igual a la suma de sus movimientos", lector.GetInt32(1) == 1);
                Comprobar(lector.GetString(0) + ": contador igual al último movimiento", lector.GetInt32(2) == 1);
            }
        }

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    /// <summary>La misma cadena que usa el servidor: busca conexion.json subiendo desde la carpeta actual.</summary>
    private static string CadenaConexion()
    {
        for (DirectoryInfo? carpeta = new DirectoryInfo(Directory.GetCurrentDirectory()); carpeta != null; carpeta = carpeta.Parent)
        {
            string archivo = Path.Combine(carpeta.FullName, "conexion.json");
            if (File.Exists(archivo))
            {
                using (JsonDocument documento = JsonDocument.Parse(File.ReadAllText(archivo)))
                {
                    return documento.RootElement.GetProperty("ConnectionStrings").GetProperty("eurekabank").GetString()!;
                }
            }
        }
        throw new FileNotFoundException("No se encontró conexion.json; ejecutar desde la carpeta del servidor o sus subcarpetas");
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
