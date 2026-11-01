#:package Microsoft.Data.SqlClient@7.1.1
#:package System.Security.Cryptography.Pkcs@10.0.14
using System;
using System.IO;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Xml.Linq;
using Microsoft.Data.SqlClient;

/// <summary>
/// Prueba de que el hash de C# (el mismo cálculo que UtilHash del servidor) coincide con
/// HASHBYTES de SQL Server y con la clave guardada de MONSTER. Si no coincidieran, ningún login
/// funcionaría. Se ejecuta desde esta carpeta: dotnet run PruebaHash.cs
/// </summary>
public class PruebaHash
{
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaHash ===");
        string hash = Sha256("MONSTER9");
        Comprobar("SHA-256 de 'abc' coincide con el vector conocido",
            Sha256("abc") == "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        Comprobar("el hash tiene 64 caracteres en minúsculas", hash.Length == 64 && hash == hash.ToLowerInvariant());

        using (SqlConnection cn = new SqlConnection(CadenaConexion()))
        {
            cn.Open();
            Comprobar("coincide con HASHBYTES de SQL Server",
                Escalar(cn, "SELECT LOWER(CONVERT(CHAR(64), HASHBYTES('SHA2_256', 'MONSTER9'), 2))") == hash);
            Comprobar("coincide con la clave guardada de MONSTER",
                Escalar(cn, "SELECT vch_emplclave FROM usuario WHERE vch_emplusuario = 'MONSTER'") == hash);
        }

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    /// <summary>Mismo cálculo que ec.edu.monster.seguridad.UtilHash.</summary>
    private static string Sha256(string texto)
    {
        byte[] hash = SHA256.HashData(Encoding.UTF8.GetBytes(texto));
        return BitConverter.ToString(hash).Replace("-", "").ToLowerInvariant();
    }

    private static string? Escalar(SqlConnection cn, string sql)
    {
        using (SqlCommand comando = new SqlCommand(sql, cn))
        {
            return (string?)comando.ExecuteScalar();
        }
    }

    /// <summary>La misma cadena que usa el servidor: busca conexion.config subiendo desde la carpeta actual.</summary>
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
