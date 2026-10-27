using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba de las conversiones de longitud, masa y temperatura en el servidor SOAP (WCF). Se ejecuta con: dotnet run PruebaConversion.cs</summary>
public class PruebaConversion
{
    private const string Url = "http://localhost:8095/";
    private const string Ns = "http://wservicios.controladores.monster.edu.ec/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConversion ===");
        string login = Enviar("AutenticacionWS", "login", null, "<usuario>MONSTER</usuario><clave>MONSTER9</clave>");
        int inicio = login.IndexOf("<token>") + 7;
        string token = login.Substring(inicio, login.IndexOf("</token>") - inicio);

        Comprobar("1 KM -> M = 1000", Convertir(token, "Longitud", "1", "KM", "M").Contains("<resultado>1000</resultado>"));
        Comprobar("1 QQ -> LB = 100", Convertir(token, "Masa", "1", "QQ", "LB").Contains("<resultado>100</resultado>"));
        Comprobar("0 C -> K = 273.15", Convertir(token, "Temperatura", "0", "C", "K").Contains("<resultado>273.15</resultado>"));
        Comprobar("KG en longitud da UNIT-001", Convertir(token, "Longitud", "1", "KG", "M").Contains("UNIT-001"));
        Comprobar("valor negativo en masa da DATA-001", Convertir(token, "Masa", "-5", "KG", "G").Contains("DATA-001"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Convertir(string token, string categoria, string valor, string origen, string destino)
    {
        return Enviar("ConversionUnidadesWS", "convertir" + categoria, token, "<solicitud><valor>" + valor + "</valor>"
            + "<unidadOrigen>" + origen + "</unidadOrigen><unidadDestino>" + destino + "</unidadDestino></solicitud>");
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
