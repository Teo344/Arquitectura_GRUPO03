#:project ../../../../WS_EUREKA_DOTNET_REST.csproj

using System;
using System.Collections.Generic;
using System.IO;
using System.Text.Json;
using Microsoft.Data.SqlClient;
using ec.edu.monster.datos;

/// <summary>
/// Prueba del singleton de conexión contra SQL Server. Los conteos son los de la consulta 1 de
/// 03_consultas.sql (solo tablas que el sistema no modifica). Se ejecuta desde esta carpeta:
/// dotnet run PruebaConexion.cs
/// </summary>
public class PruebaConexion
{
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConexion ===");
        Comprobar("Instancia es siempre la misma", ReferenceEquals(ConexionBD.Instancia, ConexionBD.Instancia));

        ConexionBD.Instancia.Configurar(CadenaConexion());
        Dictionary<string, int> esperados = new Dictionary<string, int>
        {
            ["moneda"] = 2, ["tipomovimiento"] = 10, ["sucursal"] = 7, ["empleado"] = 16,
            ["usuario"] = 13, ["cliente"] = 20, ["cuenta"] = 6
        };
        using (SqlConnection a = ConexionBD.Instancia.ObtenerConexion())
        using (SqlConnection b = ConexionBD.Instancia.ObtenerConexion())
        {
            Comprobar("cada llamada entrega una conexión distinta", !ReferenceEquals(a, b));
            foreach (KeyValuePair<string, int> tabla in esperados)
            {
                using (SqlCommand comando = new SqlCommand("SELECT COUNT(*) FROM " + tabla.Key, a))
                {
                    Comprobar(tabla.Key + " tiene " + tabla.Value + " registros", (int)comando.ExecuteScalar() == tabla.Value);
                }
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
