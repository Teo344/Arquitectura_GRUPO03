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
    private const string Url = "http://localhost:8096/";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private const int Hilos = 10;
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConcurrencia ===");
        string login = Enviar("AutenticacionWS", "iniciarSesion", null, "<usuario>MONSTER</usuario><clave>MONSTER9</clave>").Result;
        int inicio = login.IndexOf("<token>") + 7;
        string token = login.Substring(inicio, login.IndexOf("</token>") - inicio);

        decimal antes = Saldo(token, "00100001");
        string[] depositos = Task.WhenAll(Enumerable.Range(0, Hilos).Select(i => Enviar("TransaccionWS", "depositar", token,
            "<cuenta>00100001</cuenta><importe>1.00</importe>"))).Result;
        Comprobar(Hilos + " depósitos simultáneos dan OK-000", depositos.All(r => r.Contains("<codigo>OK-000</codigo>")));
        Comprobar("el saldo sube exactamente " + Hilos + ".00", Saldo(token, "00100001") == antes + Hilos);

        decimal suma = Saldo(token, "00200001") + Saldo(token, "00200002");
        string[] transferencias = Task.WhenAll(Enumerable.Range(0, Hilos).Select(i =>
        {
            string origen = i % 2 == 0 ? "00200001" : "00200002";
            string destino = i % 2 == 0 ? "00200002" : "00200001";
            return Enviar("TransaccionWS", "transferir", token, "<cuentaOrigen>" + origen + "</cuentaOrigen><cuentaDestino>"
                + destino + "</cuentaDestino><importe>1.00</importe>");
        })).Result;
        Comprobar(Hilos + " transferencias cruzadas dan OK-000 (sin deadlock)",
            transferencias.All(r => r.Contains("<codigo>OK-000</codigo>")));
        Comprobar("la suma de los saldos no cambia", Saldo(token, "00200001") + Saldo(token, "00200002") == suma);

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static decimal Saldo(string token, string cuenta)
    {
        string respuesta = Enviar("CuentaWS", "consultarCuenta", token, "<cuenta>" + cuenta + "</cuenta>").Result;
        int inicio = respuesta.IndexOf("<saldo>") + 7;
        return decimal.Parse(respuesta.Substring(inicio, respuesta.IndexOf("</saldo>") - inicio), CultureInfo.InvariantCulture);
    }

    private static async Task<string> Enviar(string servicio, string operacion, string? token, string parametros)
    {
        string sobre = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Header>"
            + (token == null ? "" : "<token xmlns=\"" + Ns + "\">" + token + "</token>")
            + "</s:Header><s:Body><" + operacion + " xmlns=\"" + Ns + "\">" + parametros + "</" + operacion + ">"
            + "</s:Body></s:Envelope>";
        HttpRequestMessage solicitud = new HttpRequestMessage(HttpMethod.Post, Url + servicio + ".svc");
        solicitud.Headers.Add("SOAPAction", "\"" + Ns + servicio + "/" + operacion + "\"");
        solicitud.Content = new StringContent(sobre, Encoding.UTF8, "text/xml");
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
