using System;
using System.Globalization;
using System.Linq;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;

/// <summary>
/// Prueba del bloqueo de filas (RN-07): depósitos simultáneos en una cuenta no pierden ninguna
/// actualización, y transferencias cruzadas A→B y B→A al mismo tiempo terminan sin deadlock y sin
/// alterar la suma de los saldos. Se ejecuta con: dotnet run PruebaConcurrencia.cs
/// </summary>
public class PruebaConcurrencia
{
    private const string Url = "http://localhost:8097/api/";
    private const int Hilos = 10;
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConcurrencia ===");
        string login = Enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}").Result;
        int inicio = login.IndexOf("\"token\":\"") + 9;
        string token = login.Substring(inicio, login.IndexOf("\"", inicio) - inicio);

        decimal antes = Saldo(token, "00100001");
        string[] depositos = Task.WhenAll(Enumerable.Range(0, Hilos).Select(i => Enviar("POST", "transacciones/depositos", token,
            "{\"cuenta\":\"00100001\",\"importe\":1.00}"))).Result;
        Comprobar(Hilos + " depósitos simultáneos dan OK-000", depositos.All(r => r.Contains("\"codigo\":\"OK-000\"")));
        Comprobar("el saldo sube exactamente " + Hilos + ".00", Saldo(token, "00100001") == antes + Hilos);

        decimal suma = Saldo(token, "00200001") + Saldo(token, "00200002");
        string[] transferencias = Task.WhenAll(Enumerable.Range(0, Hilos).Select(i =>
        {
            string origen = i % 2 == 0 ? "00200001" : "00200002";
            string destino = i % 2 == 0 ? "00200002" : "00200001";
            return Enviar("POST", "transacciones/transferencias", token,
                "{\"cuentaOrigen\":\"" + origen + "\",\"cuentaDestino\":\"" + destino + "\",\"importe\":1.00}");
        })).Result;
        Comprobar(Hilos + " transferencias cruzadas dan OK-000 (sin deadlock)",
            transferencias.All(r => r.Contains("\"codigo\":\"OK-000\"")));
        Comprobar("la suma de los saldos no cambia", Saldo(token, "00200001") + Saldo(token, "00200002") == suma);

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static decimal Saldo(string token, string cuenta)
    {
        string respuesta = Enviar("GET", "cuentas/" + cuenta, token, null).Result;
        int inicio = respuesta.IndexOf("\"saldo\":") + 8;
        return decimal.Parse(respuesta.Substring(inicio, respuesta.IndexOf(",", inicio) - inicio), CultureInfo.InvariantCulture);
    }

    private static async Task<string> Enviar(string metodo, string ruta, string? token, string? json)
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
        HttpResponseMessage respuesta = await cliente.SendAsync(solicitud);
        return await respuesta.Content.ReadAsStringAsync();
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
