#:project ../../../../WS_EUREKA_DOTNET_REST.csproj

using System;
using System.IO;
using System.Text.Json;
using Microsoft.Data.SqlClient;
using ec.edu.monster.datos;
using ec.edu.monster.seguridad;

/// <summary>
/// Prueba de que UtilHash coincide con HASHBYTES de SQL Server y con la clave guardada de MONSTER.
/// Si no coincidieran, ningún login funcionaría. Se ejecuta desde esta carpeta: dotnet run PruebaHash.cs
/// </summary>
public class PruebaHash
{
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaHash ===");
        string hash = UtilHash.Sha256("MONSTER9");
        Comprobar("SHA-256 de 'abc' coincide con el vector conocido",
            UtilHash.Sha256("abc") == "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        Comprobar("el hash tiene 64 caracteres en minúsculas", hash.Length == 64 && hash == hash.ToLowerInvariant());

        ConexionBD.Instancia.Configurar(CadenaConexion());
        using (SqlConnection cn = ConexionBD.Instancia.ObtenerConexion())
        {
            Comprobar("coincide con HASHBYTES de SQL Server",
                Escalar(cn, "SELECT LOWER(CONVERT(CHAR(64), HASHBYTES('SHA2_256', 'MONSTER9'), 2))") == hash);
            Comprobar("coincide con la clave guardada de MONSTER",
                Escalar(cn, "SELECT vch_emplclave FROM usuario WHERE vch_emplusuario = 'MONSTER'") == hash);
        }

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string? Escalar(SqlConnection cn, string sql)
    {
        using (SqlCommand comando = new SqlCommand(sql, cn))
        {
            return (string?)comando.ExecuteScalar();
        }
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
