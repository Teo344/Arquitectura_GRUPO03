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
    private const string Url = "http://localhost:8096/";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaTransacciones ===");
        string login = Enviar("AutenticacionWS", "iniciarSesion", null, "<usuario>MONSTER</usuario><clave>MONSTER9</clave>");
        int inicio = login.IndexOf("<token>") + 7;
        string token = login.Substring(inicio, login.IndexOf("</token>") - inicio);

        decimal antes = Saldo(token, "00200002");
        string deposito = Operar(token, "depositar", "<cuenta>00200002</cuenta><importe>100.00</importe>");
        Comprobar("depositar 100 en 00200002 suma al saldo", deposito.Contains("<nuevoSaldo>" + Texto(antes + 100.00m) + "</nuevoSaldo>"));
        Comprobar("el saldo consultado coincide con el del depósito", Saldo(token, "00200002") == antes + 100.00m);

        antes = Saldo(token, "00200002");
        string retiro = Operar(token, "retirar", "<cuenta>00200002</cuenta><importe>50.25</importe>");
        Comprobar("retirar 50.25 de 00200002 resta del saldo", retiro.Contains("<nuevoSaldo>" + Texto(antes - 50.25m) + "</nuevoSaldo>"));
        Comprobar("retirar más que el saldo da MOV-002",
            Operar(token, "retirar", "<cuenta>00200002</cuenta><importe>99999999.00</importe>").Contains("MOV-002"));
        Comprobar("el retiro rechazado no cambia el saldo", Saldo(token, "00200002") == antes - 50.25m);

        decimal origen = Saldo(token, "00200001");
        decimal destino = Saldo(token, "00200002");
        string transferencia = Operar(token, "transferir",
            "<cuentaOrigen>00200001</cuentaOrigen><cuentaDestino>00200002</cuentaDestino><importe>500.00</importe>");
        Comprobar("transferir 500 de 00200001 a 00200002 da OK-000", transferencia.Contains("<codigo>OK-000</codigo>"));
        Comprobar("la transferencia devuelve el nuevo saldo del origen",
            transferencia.Contains("<nuevoSaldo>" + Texto(origen - 500.00m) + "</nuevoSaldo>"));
        Comprobar("el destino recibe el importe", Saldo(token, "00200002") == destino + 500.00m);
        Comprobar("el movimiento del destino referencia al origen", Enviar("CuentaWS", "consultarMovimientos", token,
            "<cuenta>00200002</cuenta>").Contains("<cuentaReferencia>00200001</cuentaReferencia>"));

        Comprobar("transferir entre monedas distintas da MOV-004", Operar(token, "transferir",
            "<cuentaOrigen>00200001</cuentaOrigen><cuentaDestino>00200003</cuentaDestino><importe>1.00</importe>").Contains("MOV-004"));
        Comprobar("transferir a la misma cuenta da MOV-003", Operar(token, "transferir",
            "<cuentaOrigen>00200001</cuentaOrigen><cuentaDestino>00200001</cuentaDestino><importe>1.00</importe>").Contains("MOV-003"));
        Comprobar("depositar en una cuenta CANCELADA da CTA-002",
            Operar(token, "depositar", "<cuenta>00300001</cuenta><importe>10.00</importe>").Contains("CTA-002"));
        Comprobar("depositar en una cuenta inexistente da CTA-001",
            Operar(token, "depositar", "<cuenta>99999999</cuenta><importe>10.00</importe>").Contains("CTA-001"));
        Comprobar("importe con tres decimales da MOV-001",
            Operar(token, "depositar", "<cuenta>00200002</cuenta><importe>1.234</importe>").Contains("MOV-001"));
        Comprobar("importe cero da MOV-001",
            Operar(token, "depositar", "<cuenta>00200002</cuenta><importe>0</importe>").Contains("MOV-001"));
        Comprobar("sin importe da MOV-001",
            Operar(token, "depositar", "<cuenta>00200002</cuenta>").Contains("MOV-001"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Operar(string token, string operacion, string parametros)
    {
        return Enviar("TransaccionWS", operacion, token, parametros);
    }

    private static decimal Saldo(string token, string cuenta)
    {
        string respuesta = Enviar("CuentaWS", "consultarCuenta", token, "<cuenta>" + cuenta + "</cuenta>");
        int inicio = respuesta.IndexOf("<saldo>") + 7;
        return decimal.Parse(respuesta.Substring(inicio, respuesta.IndexOf("</saldo>") - inicio), CultureInfo.InvariantCulture);
    }

    private static string Texto(decimal valor) => valor.ToString(CultureInfo.InvariantCulture);

    private static string Enviar(string servicio, string operacion, string? token, string parametros)
    {
        string sobre = "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Header>"
            + (token == null ? "" : "<token xmlns=\"" + Ns + "\">" + token + "</token>")
            + "</s:Header><s:Body><" + operacion + " xmlns=\"" + Ns + "\">" + parametros + "</" + operacion + ">"
            + "</s:Body></s:Envelope>";
        HttpRequestMessage solicitud = new HttpRequestMessage(HttpMethod.Post, Url + servicio + ".svc");
        solicitud.Headers.Add("SOAPAction", "\"" + Ns + servicio + "/" + operacion + "\"");
        solicitud.Content = new StringContent(sobre, Encoding.UTF8, "text/xml");
        HttpResponseMessage respuesta = cliente.Send(solicitud);
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
