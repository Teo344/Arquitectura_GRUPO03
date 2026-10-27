using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba de las conversiones de longitud, masa y temperatura en el servidor REST. Se ejecuta con: dotnet run PruebaConversion.cs</summary>
public class PruebaConversion
{
    private const string Url = "http://localhost:8096/api/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaConversion ===");
        string login = Enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.IndexOf("\"token\":\"") + 9;
        string token = login.Substring(inicio, login.IndexOf("\"", inicio) - inicio);

        Comprobar("1 KM -> M = 1000", Convertir(token, "longitud", "1", "KM", "M").Contains("\"resultado\":1000,"));
        Comprobar("1 QQ -> LB = 100", Convertir(token, "masa", "1", "QQ", "LB").Contains("\"resultado\":100,"));
        Comprobar("0 C -> K = 273.15", Convertir(token, "temperatura", "0", "C", "K").Contains("\"resultado\":273.15,"));
        Comprobar("KG en longitud da UNIT-001", Convertir(token, "longitud", "1", "KG", "M").Contains("UNIT-001"));
        Comprobar("valor negativo en masa da DATA-001", Convertir(token, "masa", "-5", "KG", "G").Contains("DATA-001"));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
    }

    private static string Convertir(string token, string categoria, string valor, string origen, string destino)
    {
        return Enviar("POST", "conversiones/" + categoria, token, "{\"valor\":" + valor
            + ",\"unidadOrigen\":\"" + origen + "\",\"unidadDestino\":\"" + destino + "\"}");
    }

    private static string Enviar(string metodo, string ruta, string token, string json)
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
