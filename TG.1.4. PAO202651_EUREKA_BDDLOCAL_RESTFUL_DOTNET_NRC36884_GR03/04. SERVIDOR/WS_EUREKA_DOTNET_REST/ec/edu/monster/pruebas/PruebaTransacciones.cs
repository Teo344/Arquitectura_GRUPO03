using System;
using System.Globalization;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>
/// Prueba de depósitos, retiros y transferencias (RQF-005 a RQF-007) y de las reglas RN-01 a
/// RN-05. Los saldos esperados se calculan a partir del saldo consultado antes de cada operación,
/// así que la prueba puede repetirse. Se ejecuta con: dotnet run PruebaTransacciones.cs
/// </summary>
public class PruebaTransacciones
{
    private const string Url = "http://localhost:8097/api/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;
    private static int estadoHttp;

    public static void Main()
    {
        Console.WriteLine("=== PruebaTransacciones ===");
        string login = Enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.IndexOf("\"token\":\"") + 9;
        string token = login.Substring(inicio, login.IndexOf("\"", inicio) - inicio);

        decimal antes = Saldo(token, "00200002");
        string deposito = Operar(token, "depositos", "{\"cuenta\":\"00200002\",\"importe\":100.00}");
        Comprobar("depositar 100 en 00200002 suma al saldo", deposito.Contains("\"nuevoSaldo\":" + Texto(antes + 100.00m)));
        Comprobar("el saldo consultado coincide con el del depósito", Saldo(token, "00200002") == antes + 100.00m);

        antes = Saldo(token, "00200002");
        string retiro = Operar(token, "retiros", "{\"cuenta\":\"00200002\",\"importe\":50.25}");
        Comprobar("retirar 50.25 de 00200002 resta del saldo", retiro.Contains("\"nuevoSaldo\":" + Texto(antes - 50.25m)));
        Comprobar("retirar más que el saldo da 400 y MOV-002",
            Operar(token, "retiros", "{\"cuenta\":\"00200002\",\"importe\":99999999.00}").Contains("MOV-002") && estadoHttp == 400);
        Comprobar("el retiro rechazado no cambia el saldo", Saldo(token, "00200002") == antes - 50.25m);

        decimal origen = Saldo(token, "00200001");
        decimal destino = Saldo(token, "00200002");
        string transferencia = Operar(token, "transferencias",
            "{\"cuentaOrigen\":\"00200001\",\"cuentaDestino\":\"00200002\",\"importe\":500.00}");
        Comprobar("transferir 500 de 00200001 a 00200002 da OK-000", transferencia.Contains("\"codigo\":\"OK-000\""));
        Comprobar("la transferencia devuelve el nuevo saldo del origen",
            transferencia.Contains("\"nuevoSaldo\":" + Texto(origen - 500.00m)));
        Comprobar("el destino recibe el importe", Saldo(token, "00200002") == destino + 500.00m);
        Comprobar("el movimiento del destino referencia al origen",
            Enviar("GET", "cuentas/00200002/movimientos", token, null).Contains("\"cuentaReferencia\":\"00200001\""));

        Comprobar("transferir entre monedas distintas da MOV-004", Operar(token, "transferencias",
            "{\"cuentaOrigen\":\"00200001\",\"cuentaDestino\":\"00200003\",\"importe\":1.00}").Contains("MOV-004"));
        Comprobar("transferir a la misma cuenta da MOV-003", Operar(token, "transferencias",
            "{\"cuentaOrigen\":\"00200001\",\"cuentaDestino\":\"00200001\",\"importe\":1.00}").Contains("MOV-003"));
        Comprobar("depositar en una cuenta CANCELADA da CTA-002",
            Operar(token, "depositos", "{\"cuenta\":\"00300001\",\"importe\":10.00}").Contains("CTA-002"));
        Comprobar("depositar en una cuenta inexistente da CTA-001",
            Operar(token, "depositos", "{\"cuenta\":\"99999999\",\"importe\":10.00}").Contains("CTA-001"));
        Comprobar("importe con tres decimales da MOV-001",
            Operar(token, "depositos", "{\"cuenta\":\"00200002\",\"importe\":1.234}").Contains("MOV-001"));
        Comprobar("importe cero da MOV-001",
            Operar(token, "depositos", "{\"cuenta\":\"00200002\",\"importe\":0}").Contains("MOV-001"));
        Comprobar("importe no numérico da 400 y DATA-001",
            Operar(token, "depositos", "{\"cuenta\":\"00200002\",\"importe\":\"abc\"}").Contains("DATA-001") && estadoHttp == 400);

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Operar(string token, string operacion, string json)
    {
        return Enviar("POST", "transacciones/" + operacion, token, json);
    }

    private static decimal Saldo(string token, string cuenta)
    {
        string respuesta = Enviar("GET", "cuentas/" + cuenta, token, null);
        int inicio = respuesta.IndexOf("\"saldo\":") + 8;
        return decimal.Parse(respuesta.Substring(inicio, respuesta.IndexOf(",", inicio) - inicio), CultureInfo.InvariantCulture);
    }

    private static string Texto(decimal valor) => valor.ToString(CultureInfo.InvariantCulture);

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
