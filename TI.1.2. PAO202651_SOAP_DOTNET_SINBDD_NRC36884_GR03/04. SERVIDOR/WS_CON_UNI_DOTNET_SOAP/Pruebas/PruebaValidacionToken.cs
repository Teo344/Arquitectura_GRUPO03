using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba de que las operaciones protegidas exigen un token válido en la cabecera SOAP. Se ejecuta con: dotnet run PruebaValidacionToken.cs</summary>
public class PruebaValidacionToken
{
    private const string Url = "http://localhost:8095/";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaValidacionToken ===");
        string login = Enviar("AutenticacionWS", "login", null, "<usuario>MONSTER</usuario><clave>MONSTER9</clave>");
        int inicio = login.IndexOf("<token>") + 7;
        string token = login.Substring(inicio, login.IndexOf("</token>") - inicio);

        Comprobar("sin token da AUTH-001",
            Enviar("ConversionUnidadesWS", "consultarCatalogo", null, "").Contains("AUTH-001"));
        Comprobar("token inventado da AUTH-001",
            Enviar("ConversionUnidadesWS", "consultarCatalogo", "token-inventado", "").Contains("AUTH-001"));
        Comprobar("token válido permite consultar",
            Enviar("ConversionUnidadesWS", "consultarCatalogo", token, "").Contains("<estado>OK</estado>"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Enviar(string servicio, string operacion, string token, string parametros)
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
