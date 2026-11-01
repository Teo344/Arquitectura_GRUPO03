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
    private const string Url = "http://localhost:8096/";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConsultas ===");
        string login = Enviar("AutenticacionWS", "iniciarSesion", null, "<usuario>MONSTER</usuario><clave>MONSTER9</clave>");
        int inicio = login.IndexOf("<token>") + 7;
        string token = login.Substring(inicio, login.IndexOf("</token>") - inicio);

        string cuenta = Cuenta(token, "00100001");
        Comprobar("00100001 tiene saldo 6900.00", cuenta.Contains("<saldo>6900.00</saldo>"));
        Comprobar("00100001 es de ALAN ALBERTO ARANDA LUNA", cuenta.Contains("<titular>ALAN ALBERTO ARANDA LUNA</titular>"));
        Comprobar("00100001 está ACTIVO", cuenta.Contains("<estado>ACTIVO</estado>"));

        string cancelada = Cuenta(token, "00300001");
        Comprobar("00300001 se consulta con OK-000", cancelada.Contains("<codigo>OK-000</codigo>"));
        Comprobar("00300001 muestra el estado CANCELADO", cancelada.Contains("<estado>CANCELADO</estado>"));

        Comprobar("cuenta '1234' da DATA-001", Cuenta(token, "1234").Contains("DATA-001"));
        Comprobar("cuenta 99999999 da CTA-001", Cuenta(token, "99999999").Contains("CTA-001"));

        string movimientos = Enviar("CuentaWS", "consultarMovimientos", token, "<cuenta>00100001</cuenta>");
        Comprobar("00100001 tiene 7 movimientos", movimientos.Split("<movimiento>").Length - 1 == 7);
        Comprobar("el primer movimiento es la apertura", movimientos.IndexOf("<numero>1</numero>")
            < movimientos.IndexOf("<numero>2</numero>") && movimientos.Contains("Apertura de Cuenta"));
        Comprobar("movimientos de 99999999 da CTA-001", Enviar("CuentaWS", "consultarMovimientos", token,
            "<cuenta>99999999</cuenta>").Contains("CTA-001"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Cuenta(string token, string cuenta)
    {
        return Enviar("CuentaWS", "consultarCuenta", token, "<cuenta>" + cuenta + "</cuenta>");
    }

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
