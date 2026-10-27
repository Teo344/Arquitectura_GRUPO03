using System;
using System.IO;
using System.Net.Http;
using System.Text;

/// <summary>Prueba de que los recursos protegidos exigen un token válido en la cabecera Authorization. Se ejecuta con: dotnet run PruebaValidacionToken.cs</summary>
public class PruebaValidacionToken
{
    private const string Url = "http://localhost:8096/api/";
    private static readonly HttpClient cliente = new HttpClient();
    private static int fallidas = 0;

    public static void Main()
    {
        Console.WriteLine("=== PruebaValidacionToken ===");
        string login = Enviar("POST", "autenticacion/login", null, "{\"usuario\":\"MONSTER\",\"clave\":\"MONSTER9\"}");
        int inicio = login.IndexOf("\"token\":\"") + 9;
        string token = login.Substring(inicio, login.IndexOf("\"", inicio) - inicio);

        Comprobar("sin token da AUTH-001",
            Enviar("GET", "catalogo/categorias", null, null).Contains("AUTH-001"));
        Comprobar("token inventado da AUTH-001",
            Enviar("GET", "catalogo/categorias", "token-inventado", null).Contains("AUTH-001"));
        Comprobar("token válido permite consultar",
            Enviar("GET", "catalogo/categorias", token, null).Contains("\"estado\":\"OK\""));

        Console.WriteLine(fallidas == 0 ? "PRUEBA APROBADA" : "PRUEBA REPROBADA (" + fallidas + " fallas)");
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
