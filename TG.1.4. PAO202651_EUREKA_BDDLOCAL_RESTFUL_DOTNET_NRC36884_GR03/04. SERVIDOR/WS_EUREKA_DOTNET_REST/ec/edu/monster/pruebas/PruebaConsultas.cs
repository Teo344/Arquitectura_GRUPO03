using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>
/// Prueba de la consulta de cuenta (RQF-003) y de movimientos (RQF-004). Requiere la BD recién
/// cargada con los datos semilla. Se ejecuta con: dotnet run PruebaConsultas.cs
/// </summary>
public class PruebaConsultas
{
    private const string Url = "http://localhost:8097/api/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConsultas ===");
        string login = Enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.IndexOf("\"token\":\"") + 9;
        string token = login.Substring(inicio, login.IndexOf("\"", inicio) - inicio);

        string cuenta = Enviar("GET", "cuentas/00100001", token, null);
        Comprobar("00100001 tiene saldo 6900.00", cuenta.Contains("\"saldo\":6900.00"));
        Comprobar("00100001 es de ALAN ALBERTO ARANDA LUNA", cuenta.Contains("\"titular\":\"ALAN ALBERTO ARANDA LUNA\""));
        Comprobar("00100001 está ACTIVO", cuenta.Contains("\"estado\":\"ACTIVO\""));

        string cancelada = Enviar("GET", "cuentas/00300001", token, null);
        Comprobar("00300001 se consulta con OK-000", cancelada.Contains("\"codigo\":\"OK-000\""));
        Comprobar("00300001 muestra el estado CANCELADO", cancelada.Contains("\"estado\":\"CANCELADO\""));

        Comprobar("cuenta '1234' da 400 y DATA-001", Enviar("GET", "cuentas/1234", token, null).Contains("DATA-001") && estadoHttp == 400);
        Comprobar("cuenta 99999999 da CTA-001", Enviar("GET", "cuentas/99999999", token, null).Contains("CTA-001"));

        string movimientos = Enviar("GET", "cuentas/00100001/movimientos", token, null);
        Comprobar("00100001 tiene 7 movimientos", movimientos.Split("\"numero\":").Length - 1 == 7);
        Comprobar("el primer movimiento es la apertura", movimientos.IndexOf("\"numero\":1,")
            < movimientos.IndexOf("\"numero\":2,") && movimientos.Contains("Apertura de Cuenta"));
        Comprobar("movimientos de 99999999 da CTA-001",
            Enviar("GET", "cuentas/99999999/movimientos", token, null).Contains("CTA-001"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Enviar(string metodo, string ruta, string? token, string? json)
    {
        HttpRequestMessage solicitud = new HttpRequestMessage(new HttpMethod(metodo), Url + ruta);
        if (token != null)
        {
            solicitud.Headers.Add("Authorization", "Bearer " + token);
        }
        if (json != null)
        {
            solicitud.Content = new StringContent(json, Encoding.UTF8, "application/json");
        }
        HttpResponseMessage respuesta = cliente.Send(solicitud);
        estadoHttp = (int)respuesta.StatusCode;
        return new StreamReader(respuesta.Content.ReadAsStream()).ReadToEnd();
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
